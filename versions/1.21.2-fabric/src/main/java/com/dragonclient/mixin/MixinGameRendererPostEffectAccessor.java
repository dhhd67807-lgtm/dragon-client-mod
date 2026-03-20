package com.dragonclient.mixin;

import net.minecraft.client.render.GameRenderer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRenderer.class)
public interface MixinGameRendererPostEffectAccessor {
    @Accessor("postProcessorId")
    void dragonclient$setPostProcessorId(Identifier id);

    @Accessor("postProcessorEnabled")
    void dragonclient$setPostProcessorEnabled(boolean enabled);
}
