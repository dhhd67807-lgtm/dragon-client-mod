package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ArmorStatusHud extends HudModule {
    
    public ArmorStatusHud() {
        super("Armor Status", "Displays armor durability");
        this.x = 5;  // Top left, below hunger
        this.y = 105;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        
        // 1.21.11: getArmorItems() removed - use EquipmentSlot
        net.minecraft.entity.EquipmentSlot[] armorSlots = {
            net.minecraft.entity.EquipmentSlot.FEET,
            net.minecraft.entity.EquipmentSlot.LEGS,
            net.minecraft.entity.EquipmentSlot.CHEST,
            net.minecraft.entity.EquipmentSlot.HEAD
        };
        
        int yOffset = 0;
        for (net.minecraft.entity.EquipmentSlot slot : armorSlots) {
            ItemStack armor = client.player.getEquippedStack(slot);
            if (!armor.isEmpty() && armor.isDamageable()) {
                int durability = armor.getMaxDamage() - armor.getDamage();
                int maxDurability = armor.getMaxDamage();
                String text = durability + "/" + maxDurability;
                
                // Draw background - #1D1C1C at 50% opacity
                int textWidth = client.textRenderer.getWidth(text);
                int textHeight = client.textRenderer.fontHeight;
                context.fill(x - 6, y + yOffset - 6, x + textWidth + 6, y + yOffset + textHeight + 6, 0x801D1C1C);
                
                // Draw outer border - #161616 at 100% opacity
                context.fill(x - 6, y + yOffset - 6, x + textWidth + 6, y + yOffset - 5, 0xFF161616); // Top
                context.fill(x - 6, y + yOffset + textHeight + 5, x + textWidth + 6, y + yOffset + textHeight + 6, 0xFF161616); // Bottom
                context.fill(x - 6, y + yOffset - 6, x - 5, y + yOffset + textHeight + 6, 0xFF161616); // Left
                context.fill(x + textWidth + 5, y + yOffset - 6, x + textWidth + 6, y + yOffset + textHeight + 6, 0xFF161616); // Right
                
                // Draw inset shadow - Dark gray for depth
                context.fill(x - 5, y + yOffset - 5, x + textWidth + 5, y + yOffset - 4, 0x80000000); // Top inner shadow
                context.fill(x - 5, y + yOffset - 5, x - 4, y + yOffset + textHeight + 5, 0x80000000); // Left inner shadow
                
                // Draw text - White at 100% opacity without shadow
                context.drawText(client.textRenderer, text, x, y + yOffset, 0xFFFFFFFF, false);
                yOffset += textHeight + 2;
            }
        }
        
        this.height = yOffset;
    }
}
