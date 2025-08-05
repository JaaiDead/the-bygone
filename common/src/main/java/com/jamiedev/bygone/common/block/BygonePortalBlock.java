package com.jamiedev.bygone.common.block;

import com.jamiedev.bygone.common.block.entity.BygonePortalBlockEntity;
import com.jamiedev.bygone.common.util.PortalUtils;
import com.jamiedev.bygone.core.registry.BGBlocks;
import com.jamiedev.bygone.core.registry.BGDimensions;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BygonePortalBlock extends Block implements Portal
{
    //public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final MapCodec<BygonePortalBlock> CODEC = BlockBehaviour.simpleCodec(BygonePortalBlock::new);

    @Override
    public MapCodec<BygonePortalBlock> codec() {
        return CODEC;
    }
    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    public BygonePortalBlock(BlockBehaviour.Properties settings) {
        super(settings);
        //this.registerDefaultState(this.stateDefinition.any().setValue(AXIS, Direction.Axis.X));
    }

    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BygonePortalBlockEntity(pos, state);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    protected boolean canBeReplaced(BlockState state, Fluid fluid) {
        return false;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        Direction.Axis direction$axis = facing.getAxis();
        //Direction.Axis direction$axis1 = state.getValue(AXIS);
        return !facingState.is(this) && !new PortalShape(level, currentPos, direction$axis).isComplete()
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public @Nullable DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        ResourceKey<Level> resourcekey = level.dimension() == BGDimensions.BYGONE_LEVEL_KEY ? Level.OVERWORLD : BGDimensions.BYGONE_LEVEL_KEY;
        ServerLevel serverlevel = level.getServer().getLevel(resourcekey);
        if (serverlevel == null) {
            return null;
        } else {
            boolean flag = serverlevel.dimension() == BGDimensions.BYGONE_LEVEL_KEY;
            WorldBorder worldborder = serverlevel.getWorldBorder();
            double d0 = DimensionType.getTeleportationScale(level.dimensionType(), serverlevel.dimensionType());
            BlockPos blockpos = worldborder.clampToBounds(entity.getX() * d0, entity.getY(), entity.getZ() * d0);
            return this.getExitPortal(serverlevel, entity, pos, blockpos, flag, worldborder);
        }
    }

    public static Pair<BlockPos, Boolean> findClosestPortalPosition(BlockPos exitPos, ServerLevel level) {
        var height = level.getBlockFloorHeight(exitPos);
        exitPos = exitPos.atY((int)height);
        var valid = true;
            for (var pos : BlockPos.betweenClosed(exitPos, exitPos.relative(Direction.Axis.X, 4).relative(Direction.Axis.Z, 4))) {
                if (level.getBlockState(pos).is(BGBlocks.BYGONE_PORTAL.get()) || level.getBlockState(pos).is(BGBlocks.BYGONE_PORTAL_FRAME.get())) {
                    valid = false;
                    break;
                }
            }
        if (valid) {
            return Pair.of(exitPos, true);
        } else {
            return Pair.of(exitPos, false);
        }
    }

    private @NotNull DimensionTransition getExitPortal(
            ServerLevel level, Entity entity, BlockPos pos, BlockPos exitPos, boolean isNether, WorldBorder worldBorder
    ) {
        Pair<BlockPos, Boolean> alreadyHasPortal = findClosestPortalPosition(exitPos, level);
        exitPos = alreadyHasPortal.getFirst();
        DimensionTransition.PostDimensionTransition dimensiontransition$postdimensiontransition;

        if (alreadyHasPortal.getSecond()) {
            dimensiontransition$postdimensiontransition = DimensionTransition.PLAY_PORTAL_SOUND;
            PortalUtils.createPortal(level, exitPos);
        } else {
            dimensiontransition$postdimensiontransition = DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET);
        }

        return getDimensionTransitionFromExit(entity, pos, level, dimensiontransition$postdimensiontransition);
    }

    private static DimensionTransition getDimensionTransitionFromExit(
            Entity entity, BlockPos pos, ServerLevel level, DimensionTransition.PostDimensionTransition postDimensionTransition
    ) {
        BlockState blockstate = entity.level().getBlockState(pos);
        Direction.Axis direction$axis;
        Vec3 vec3;
        if (blockstate.hasProperty(BlockStateProperties.HORIZONTAL_AXIS)) {
            direction$axis = blockstate.getValue(BlockStateProperties.HORIZONTAL_AXIS);
            BlockUtil.FoundRectangle blockutil$foundrectangle = BlockUtil.getLargestRectangleAround(
                    pos, direction$axis, 21, Direction.Axis.Y, 21, p_351016_ -> entity.level().getBlockState(p_351016_) == blockstate
            );
            vec3 = entity.getRelativePortalPosition(direction$axis, blockutil$foundrectangle);
        } else {
            direction$axis = Direction.Axis.X;
            vec3 = new Vec3(0.5, 0.0, 0.0);
        }

        return createDimensionTransition(
                level, direction$axis, pos ,vec3, entity, entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), postDimensionTransition
        );
    }

    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity.canUsePortal(false)) {
            entity.setAsInsidePortal(this, pos);
        }
    }

    @Override
    public int getPortalTransitionTime(ServerLevel level, Entity entity) {
        return 40;
    }

    private static DimensionTransition createDimensionTransition(
            ServerLevel level,
            Direction.Axis axis,
            BlockPos exitPos,
            Vec3 offset,
            Entity entity,
            Vec3 speed,
            float yRot,
            float xRot,
            DimensionTransition.PostDimensionTransition postDimensionTransition
    ) {
        BlockState blockstate = level.getBlockState(exitPos);
        Direction.Axis direction$axis = blockstate.getOptionalValue(BlockStateProperties.HORIZONTAL_AXIS).orElse(Direction.Axis.X);
        double d0 = 3;
        double d1 = 3;
        EntityDimensions entitydimensions = entity.getDimensions(entity.getPose());
        int i = axis == direction$axis ? 0 : 90;
        Vec3 vec3 = axis == direction$axis ? speed : new Vec3(speed.z, speed.y, -speed.x);
        double d2 = (double)entitydimensions.width() / 2.0 + (d0 - (double)entitydimensions.width()) * offset.x();
        double d3 = (d1 - (double)entitydimensions.height()) * offset.y();
        double d4 = 0.5 + offset.z();
        boolean flag = direction$axis == Direction.Axis.X;
        Vec3 vec31 = new Vec3((double) exitPos.getX() + (flag ? d2 : d4), (double) exitPos.getY() + d3, (double) exitPos.getZ() + (flag ? d4 : d2));
        Vec3 vec32 = PortalShape.findCollisionFreePosition(vec31, level, entity, entitydimensions);
        return new DimensionTransition(level, vec32, vec3, yRot + (float)i, xRot, postDimensionTransition);
    }
}
