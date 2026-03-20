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
        
        int iconSize = 16;
        int iconSpacing = 4;
        int iconIndex = 0;
        for (net.minecraft.entity.EquipmentSlot slot : armorSlots) {
            ItemStack armor = client.player.getEquippedStack(slot);
            if (!armor.isEmpty()) {
                int iconX = x + iconIndex * (iconSize + iconSpacing);
                int iconY = y;

                // Draw card-style background around each armor piece icon.
                context.fill(iconX - 2, iconY - 2, iconX + iconSize + 2, iconY + iconSize + 2, 0x801D1C1C);
                context.fill(iconX - 2, iconY - 2, iconX + iconSize + 2, iconY - 1, 0xFF161616); // Top
                context.fill(iconX - 2, iconY + iconSize + 1, iconX + iconSize + 2, iconY + iconSize + 2, 0xFF161616); // Bottom
                context.fill(iconX - 2, iconY - 2, iconX - 1, iconY + iconSize + 2, 0xFF161616); // Left
                context.fill(iconX + iconSize + 1, iconY - 2, iconX + iconSize + 2, iconY + iconSize + 2, 0xFF161616); // Right

                // Draw the real equipped armor item icon.
                context.drawItem(armor, iconX, iconY);
                iconIndex++;
            }
        }

        if (iconIndex == 0) {
            String text = "No Armor";
            int textWidth = client.textRenderer.getWidth(text);
            int textHeight = client.textRenderer.fontHeight;

            context.fill(x - 6, y - 6, x + textWidth + 6, y + textHeight + 6, 0x801D1C1C);
            context.fill(x - 6, y - 6, x + textWidth + 6, y - 5, 0xFF161616); // Top
            context.fill(x - 6, y + textHeight + 5, x + textWidth + 6, y + textHeight + 6, 0xFF161616); // Bottom
            context.fill(x - 6, y - 6, x - 5, y + textHeight + 6, 0xFF161616); // Left
            context.fill(x + textWidth + 5, y - 6, x + textWidth + 6, y + textHeight + 6, 0xFF161616); // Right
            context.drawText(client.textRenderer, text, x, y, 0xFFFFFFFF, false);

            this.width = textWidth;
            this.height = textHeight;
            return;
        }

        this.width = (iconIndex * iconSize) + ((iconIndex - 1) * iconSpacing);
        this.height = iconSize;
    }
}
