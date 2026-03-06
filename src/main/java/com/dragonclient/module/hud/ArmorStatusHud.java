package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ArmorStatusHud extends HudModule {
    
    public ArmorStatusHud() {
        super("Armor Status", "Displays armor durability");
        this.x = 5;
        this.y = 80;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        int yOffset = 0;
        for (ItemStack armor : client.player.getArmorItems()) {
            if (!armor.isEmpty() && armor.isDamageable()) {
                int durability = armor.getMaxDamage() - armor.getDamage();
                int maxDurability = armor.getMaxDamage();
                String text = durability + "/" + maxDurability;
                
                context.drawText(client.textRenderer, text, x, y + yOffset, 0xFFFFFF, true);
                yOffset += client.textRenderer.fontHeight + 2;
            }
        }
        
        this.height = yOffset;
    }
}
