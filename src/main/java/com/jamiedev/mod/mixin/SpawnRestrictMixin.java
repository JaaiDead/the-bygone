package com.jamiedev.mod.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.level.levelgen.Heightmap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SpawnPlacements.class)
public interface SpawnRestrictMixin
{
    @Invoker
    static <T extends Mob> void callRegister(
            EntityType<T> type,
            SpawnPlacementType location,
            Heightmap.Types heightmapType,
            SpawnPlacements.SpawnPredicate<T> predicate
    ) {
        throw new AssertionError();
    }
}
