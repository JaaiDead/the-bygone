package com.jamiedev.bygone.common.util;

import com.jamiedev.bygone.core.registry.BGBlocks;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Optional;

public final class PortalUtils {

    public static void createPortal(Level world, BlockPos pos, BlockState frameBlock, Direction.Axis axis) {
        for(int i = -1; i < 3; ++i) {
            world.setBlockAndUpdate(pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, -1), frameBlock);
            world.setBlockAndUpdate(pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, 2), frameBlock);
            world.setBlockAndUpdate(pos.relative(Direction.Axis.Z, i).relative(Direction.Axis.X, -1), frameBlock);
            world.setBlockAndUpdate(pos.relative(Direction.Axis.Z, i).relative(Direction.Axis.X, 2), frameBlock);
        }

        for(int i = 0; i < 2; ++i) {
            placeLandingPad(world, pos.relative(Direction.Axis.X, i).below(), frameBlock);
            placeLandingPad(world, pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, 1).below(), frameBlock);
            fillAirAroundPortal(world, pos.relative(Direction.Axis.X, i).above());
            fillAirAroundPortal(world, pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, 1).above());
            fillAirAroundPortal(world, pos.relative(Direction.Axis.X, i).above(2));
            fillAirAroundPortal(world, pos.relative(Direction.Axis.X, i).relative(Direction.Axis.Z, 1).above(2));
        }

        BlockPos.betweenClosed(pos, pos.relative(Direction.Axis.X, 3).relative(Direction.Axis.Z, 3)).forEach((blockPos) -> world.setBlock(blockPos, BGBlocks.BYGONE_PORTAL.get().defaultBlockState(), 18));    }

    private static void placeLandingPad(Level world, BlockPos pos, BlockState frameBlock) {
        if (!world.getBlockState(pos).isSolid())
            world.setBlockAndUpdate(pos, frameBlock);
    }

    private static void fillAirAroundPortal(Level world, BlockPos pos) {
        if (world.getBlockState(pos).isSolid())
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 16);
    }

    private static boolean canPortalReplaceBlock(BlockPos.MutableBlockPos pos, ServerLevel level) {
        BlockState blockstate = level.getBlockState(pos);
        return blockstate.canBeReplaced() && blockstate.getFluidState().isEmpty();
    }
}
