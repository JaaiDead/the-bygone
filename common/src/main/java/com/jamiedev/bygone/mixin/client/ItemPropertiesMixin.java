package com.jamiedev.bygone.mixin.client;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ItemProperties.class)
public interface ItemPropertiesMixin {

    @Invoker
    static void invokeRegister(Item $$0, ResourceLocation $$1, ClampedItemPropertyFunction $$2) {
        throw new AssertionError();
    }
}
