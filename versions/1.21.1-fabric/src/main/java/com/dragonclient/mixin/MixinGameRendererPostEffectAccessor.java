package com.dragonclient.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRenderer.class)
public interface MixinGameRendererPostEffectAccessor {
    @Invoker("loadPostProcessor")
    void dragonclient$loadPostProcessor(Identifier id);

    @Invoker("disablePostProcessor")
    void dragonclient$disablePostProcessor();
}
