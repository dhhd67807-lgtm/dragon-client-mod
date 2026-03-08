package com.dragonclient.module.hud;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;
import net.minecraft.client.gui.DrawContext;

public abstract class HudModule extends Module {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean dragging;

    public HudModule(String name, String description) {
        super(name, description, ModuleCategory.HUD);
        this.x = 10;
        this.y = 10;
        this.width = 100;
        this.height = 20;
    }

    public abstract void render(DrawContext context, float tickDelta);

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isDragging() {
        return dragging;
    }

    public void setDragging(boolean dragging) {
        this.dragging = dragging;
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
