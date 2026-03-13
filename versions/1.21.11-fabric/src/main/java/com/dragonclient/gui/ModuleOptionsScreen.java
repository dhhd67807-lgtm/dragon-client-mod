package com.dragonclient.gui;

import com.dragonclient.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModuleOptionsScreen extends Screen {
    private final Screen parent;
    private final Module module;

    private static final int GUI_WIDTH = 420;
    private static final int GUI_HEIGHT = 240;

    private int guiLeft;
    private int guiTop;

    public ModuleOptionsScreen(Screen parent, Module module) {
        super(Text.literal(module.getName() + " Options"));
        this.parent = parent;
        this.module = module;
    }

    @Override
    protected void init() {
        this.guiLeft = (this.width - GUI_WIDTH) / 2;
        this.guiTop = (this.height - GUI_HEIGHT) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);

        int panelColor = 0xE0141110;
        int borderColor = 0xFF2A2622;
        int titleColor = 0xFFFFFFFF;
        int descColor = 0xFF9A948F;

        drawRect(context, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, panelColor);
        drawBorder(context, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, borderColor);

        String title = module.getName().toUpperCase();
        int titleWidth = this.textRenderer.getWidth(title);
        context.drawTextWithShadow(this.textRenderer, title, guiLeft + (GUI_WIDTH - titleWidth) / 2, guiTop + 18, titleColor);

        String desc = module.getDescription();
        int descWidth = this.textRenderer.getWidth(desc);
        context.drawTextWithShadow(this.textRenderer, desc, guiLeft + (GUI_WIDTH - descWidth) / 2, guiTop + 40, descColor);

        int toggleX = guiLeft + 30;
        int toggleY = guiTop + 90;
        int toggleW = GUI_WIDTH - 60;
        int toggleH = 34;
        boolean toggleHovered = inside(mouseX, mouseY, toggleX, toggleY, toggleW, toggleH);
        int toggleColor = module.isEnabled() ? (toggleHovered ? 0xFF5EC96A : 0xFF4CAF50) : (toggleHovered ? 0xFF3A3330 : 0xFF2A2622);

        drawRect(context, toggleX, toggleY, toggleW, toggleH, toggleColor);
        drawBorder(context, toggleX, toggleY, toggleW, toggleH, 0xFF47423E);

        String toggleText = module.isEnabled() ? "DISABLE MODULE" : "ENABLE MODULE";
        int toggleTextWidth = this.textRenderer.getWidth(toggleText);
        context.drawTextWithShadow(this.textRenderer, toggleText, toggleX + (toggleW - toggleTextWidth) / 2, toggleY + 12, 0xFFFFFFFF);

        int statusY = toggleY + toggleH + 18;
        String status = module.isEnabled() ? "Status: Enabled" : "Status: Disabled";
        int statusColor = module.isEnabled() ? 0xFF63D471 : 0xFFE46C6C;
        int statusWidth = this.textRenderer.getWidth(status);
        context.drawTextWithShadow(this.textRenderer, status, guiLeft + (GUI_WIDTH - statusWidth) / 2, statusY, statusColor);

        int backX = guiLeft + 30;
        int backY = guiTop + GUI_HEIGHT - 50;
        int backW = 110;
        int backH = 26;
        boolean backHovered = inside(mouseX, mouseY, backX, backY, backW, backH);
        drawRect(context, backX, backY, backW, backH, backHovered ? 0xFF3A3330 : 0xFF2A2622);
        drawBorder(context, backX, backY, backW, backH, 0xFF47423E);
        context.drawTextWithShadow(this.textRenderer, "< BACK", backX + 30, backY + 9, 0xFFFFFFFF);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return false;
        }

        int mx = (int) mouseX;
        int my = (int) mouseY;

        int toggleX = guiLeft + 30;
        int toggleY = guiTop + 90;
        int toggleW = GUI_WIDTH - 60;
        int toggleH = 34;

        if (inside(mx, my, toggleX, toggleY, toggleW, toggleH)) {
            module.toggle();
            return true;
        }

        int backX = guiLeft + 30;
        int backY = guiTop + GUI_HEIGHT - 50;
        int backW = 110;
        int backH = 26;

        if (inside(mx, my, backX, backY, backW, backH)) {
            MinecraftClient.getInstance().setScreen(parent);
            return true;
        }

        return false;
    }

    @Override
    public boolean mouseClicked(Click click, boolean dblClick) {
        return mouseClicked(click.x(), click.y(), click.button());
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseReleased(Click click) {
        return mouseReleased(click.x(), click.y(), click.button());
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        return mouseDragged(click.x(), click.y(), click.button(), deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private static boolean inside(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    private static void drawRect(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + h, color);
    }

    private static void drawBorder(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }
}
