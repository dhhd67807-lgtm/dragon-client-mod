package com.dragonclient.module.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ArmorStatusHud extends HudModule {
    
    public ArmorStatusHud() {
        super("Armor Status", "Displays armor durability");
        this.x = 24;
        this.y = 262;
    }

    @Override
    public void render(DrawContext context, float tickDelta) {
        if (!isEnabled()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        applyDefaultTopLeft(client, 24, 262);
        
        // 1.21.11: getArmorItems() removed - use EquipmentSlot
        net.minecraft.entity.EquipmentSlot[] armorSlots = {
            net.minecraft.entity.EquipmentSlot.FEET,
            net.minecraft.entity.EquipmentSlot.LEGS,
            net.minecraft.entity.EquipmentSlot.CHEST,
            net.minecraft.entity.EquipmentSlot.HEAD
        };
        
        int iconSize = 16;
        int iconSpacing = 4;
        int iconIndex = 0;
        for (net.minecraft.entity.EquipmentSlot slot : armorSlots) {
            ItemStack armor = client.player.getEquippedStack(slot);
            if (!armor.isEmpty()) {
                int iconX = x + iconIndex * (iconSize + iconSpacing);
                int iconY = y;

                drawLiquidGlassPanel(context, iconX - 3, iconY - 3, iconX + iconSize + 3, iconY + iconSize + 3, false);

                // Draw the real equipped armor item icon.
                context.drawItem(armor, iconX, iconY);
                iconIndex++;
            }
        }

        if (iconIndex == 0) {
            String text = "No Armor";
            int textWidth = client.textRenderer.getWidth(text);
            int textHeight = client.textRenderer.fontHeight;

            drawLiquidGlassTextPanel(context, textWidth, textHeight);
            context.drawText(client.textRenderer, text, x, y, 0xFFFFFFFF, false);

            this.width = textWidth;
            this.height = textHeight;
            return;
        }

        this.width = (iconIndex * iconSize) + ((iconIndex - 1) * iconSpacing);
        this.height = iconSize;
    }
}
