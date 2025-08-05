package com.jamiedev.bygone;


import com.jamiedev.bygone.client.BygoneClientNeoForge;
import com.jamiedev.bygone.common.util.PortalUtils;
import com.jamiedev.bygone.core.datagen.BygoneDataGenerator;
import com.jamiedev.bygone.core.registry.AttachmentTypesNeoForge;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Cow;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(Bygone.MOD_ID)
public class BygoneNeoForge {

    IPayloadContext ctx;
    public BygoneNeoForge(IEventBus eventBus, Dist dist) {
        Bygone.init();

        eventBus.addListener(PacketHandlerNeoForge::register);
        if (dist.isClient()) {
            BygoneClientNeoForge.init(eventBus);
        }
        eventBus.addListener(this::registerEvent);
        eventBus.addListener(BygoneDataGenerator::onInitializeDataGenerator);
        eventBus.addListener(this::setup);
        eventBus.addListener(this::spawnPlacements);
        eventBus.addListener(this::createAttributes);
        eventBus.addListener(this::addValidBlocks);
        NeoForge.EVENT_BUS.addListener(this::entityTick);
        NeoForge.EVENT_BUS.addListener(this::damageEvent);
        NeoForge.EVENT_BUS.addListener(this::debugCommands);
    }

    void debugCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("debugportal").then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(args -> {
            PortalUtils.createPortal(args.getSource().getLevel(), BlockPosArgument.getBlockPos(args, "pos"));

            return 0;
        })));
    }

    //

    void entityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (entity instanceof Cow cow && !entity.level().isClientSide) {
            Bygone.tickCow(cow);
        }
    }

    void damageEvent(LivingDamageEvent.Pre event) {

    }

    void createAttributes(EntityAttributeCreationEvent event) {
        Bygone.initAttributes(event::put);
    }
    

    void spawnPlacements(RegisterSpawnPlacementsEvent event) {
        Bygone.registerSpawnPlacements((entityType, spawnPlacementType, types, spawnPredicate) -> event.register(entityType,spawnPlacementType,types,spawnPredicate, RegisterSpawnPlacementsEvent.Operation.REPLACE));
  }

    void setup(FMLCommonSetupEvent event) {
        Bygone.registerStrippables();
        Bygone.addFlammable();
    }

    void addValidBlocks(BlockEntityTypeAddBlocksEvent event) {
        Bygone.addValidBlocks(event::modify);
    }

    void registerEvent(RegisterEvent event) {
        Registry<?> registry = event.getRegistry();

        if (registry == BuiltInRegistries.BLOCK) {
            AttachmentTypesNeoForge.init();
            Bygone.registerBuiltIn();
        }
    }

}