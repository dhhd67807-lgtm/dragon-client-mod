package com.dragonclient.gui;

import com.dragonclient.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class ModuleOptionsScreen extends Screen {
    private final Screen parent;
    private final Module module;
    private static final int GUI_WIDTH = 700;
    private static final int GUI_HEIGHT = 450;
    private static final int FIXED_GUI_SCALE = 2;
    
    private static final Identifier STAR_ICON = Identifier.of("dragonclient", "textures/gui/cs_star_8.png");
    
    private int guiLeft;
    private int guiTop;
    private float scrollOffset = 0;
    
    // Settings data structures
    private final List<Setting> settings = new ArrayList<>();
    
    public ModuleOptionsScreen(Screen parent, Module module) {
        super(Text.literal(module.getName() + " Options"));
        this.parent = parent;
        this.module = module;
        initializeSettings();
    }
    
    private void initializeSettings() {
        // Add module-specific settings based on module type
        String moduleName = module.getName().toLowerCase();
        
        // Example settings for different modules
        if (moduleName.contains("motion blur")) {
            settings.add(new SliderSetting("Blur Amount", 0.5f, 0.0f, 1.0f));
        } else if (moduleName.contains("zoom")) {
            settings.add(new SliderSetting("Zoom Level", 3.0f, 1.0f, 10.0f));
            settings.add(new ToggleSetting("Smooth Zoom", true));
        } else if (moduleName.contains("fps") || moduleName.contains("cps")) {
            settings.add(new SliderSetting("Scale", 1.0f, 0.5f, 3.0f));
            settings.add(new ToggleSetting("Show Background", true));
            settings.add(new ColorSetting("Text Color", 0xFFFFFFFF));
            settings.add(new ColorSetting("Background Color", 0x80000000));
        } else if (moduleName.contains("keystroke")) {
            settings.add(new SliderSetting("Scale", 1.46f, 0.5f, 3.0f));
            settings.add(new ToggleSetting("Show Clicks", false));
            settings.add(new ToggleSetting("Show LMB CPS", false));
            settings.add(new ToggleSetting("Show RMB CPS", false));
            settings.add(new ToggleSetting("Show Movement Keys", true));
            settings.add(new ToggleSetting("Show Space Bar", false));
            settings.add(new ToggleSetting("Replace Names With Arrows", false));
            settings.add(new ToggleSetting("Text Shadow", false));
            settings.add(new ToggleSetting("Border", false));
            settings.add(new ToggleSetting("Inner Border", false));
            settings.add(new SliderSetting("Border Thickness", 0.5f, 0.0f, 5.0f));
            settings.add(new SliderSetting("Box Size", 19.49f, 10.0f, 50.0f));
            settings.add(new ColorSetting("Border Color", 0xFFFFFFFF));
            settings.add(new ColorSetting("Text Color", 0xFFFFFFFF));
            settings.add(new ColorSetting("Text Color (Pressed)", 0xFF000000));
            settings.add(new ColorSetting("Background Color", 0x6F000000));
            settings.add(new ColorSetting("Background Color (Pressed)", 0x6FFFFFFF));
            settings.add(new SliderSetting("Key Fade Delay", 75f, 0f, 200f));
        } else {
            // Default settings for other modules
            settings.add(new SliderSetting("Scale", 1.0f, 0.5f, 3.0f));
            settings.add(new ToggleSetting("Show Background", false));
        }
    }

    @Override
    protected void init() {
        super.init();
        MinecraftClient client = MinecraftClient.getInstance();
        int scaledWidth = client.getWindow().getFramebufferWidth() / FIXED_GUI_SCALE;
        int scaledHeight = client.getWindow().getFramebufferHeight() / FIXED_GUI_SCALE;
        this.guiLeft = (scaledWidth - GUI_WIDTH) / 2;
        this.guiTop = (scaledHeight - GUI_HEIGHT) / 2;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        double currentScale = client.getWindow().getScaleFactor();
        double targetScale = FIXED_GUI_SCALE;
        float scaleFactor = (float)(targetScale / currentScale);
        
        int transformedMouseX = (int)(mouseX / scaleFactor);
        int transformedMouseY = (int)(mouseY / scaleFactor);
        
        int fixedScaledWidth = (int)(this.width / scaleFactor);
        int fixedScaledHeight = (int)(this.height / scaleFactor);
        int fixedGuiLeft = (fixedScaledWidth - GUI_WIDTH) / 2;
        int fixedGuiTop = (fixedScaledHeight - GUI_HEIGHT) / 2;
        
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(scaleFactor, scaleFactor);
        
        // Draw main background
        drawRoundedRect(context, fixedGuiLeft, fixedGuiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF100C08);
        drawRoundedBorder(context, fixedGuiLeft, fixedGuiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF2A2622);
        
        // Draw header bar
        context.fill(fixedGuiLeft, fixedGuiTop, fixedGuiLeft + GUI_WIDTH, fixedGuiTop + 60, 0xFF1A1614);
        context.fill(fixedGuiLeft, fixedGuiTop + 59, fixedGuiLeft + GUI_WIDTH, fixedGuiTop + 60, 0xFF252220);
        
        // Draw back button
        int backX = fixedGuiLeft + 15;
        int backY = fixedGuiTop + 15;
        boolean isBackHovered = transformedMouseX >= backX && transformedMouseX <= backX + 80 && 
                               transformedMouseY >= backY && transformedMouseY <= backY + 30;
        drawRoundedButton(context, backX, backY, 80, 30, isBackHovered ? 0xFF252220 : 0xFF1A1614);
        context.drawTextWithShadow(this.textRenderer, "← BACK", backX + 10, backY + 11, 0xFFFEFEFE);
        
        // Draw module name with star icon
        String title = module.getName().toUpperCase();
        int titleWidth = this.textRenderer.getWidth(title);
        int starSize = 16;
        int totalWidth = starSize + 5 + titleWidth;
        int headerStartX = fixedGuiLeft + (GUI_WIDTH - totalWidth) / 2;
        
        drawTexture(context, STAR_ICON, headerStartX, fixedGuiTop + 22, starSize, starSize);
        context.drawTextWithShadow(this.textRenderer, title, headerStartX + starSize + 5, fixedGuiTop + 25, 0xFFFEFEFE);
        
        // Draw module description
        String desc = module.getDescription();
        int descWidth = this.textRenderer.getWidth(desc);
        context.drawTextWithShadow(this.textRenderer, desc, fixedGuiLeft + (GUI_WIDTH - descWidth) / 2, 
                                  fixedGuiTop + 80, 0xFF888888);
        
        // Draw toggle switch
        int toggleX = fixedGuiLeft + (GUI_WIDTH - 200) / 2;
        int toggleY = fixedGuiTop + 120;
        drawToggleSwitch(context, toggleX, toggleY, 200, 40, module.isEnabled(), 
                        transformedMouseX, transformedMouseY);
        
        // Draw status text
        String statusText = module.isEnabled() ? "ENABLED" : "DISABLED";
        int statusColor = module.isEnabled() ? 0xFF4CAF50 : 0xFFE63946;
        int statusWidth = this.textRenderer.getWidth(statusText);
        context.drawTextWithShadow(this.textRenderer, statusText, 
                                  fixedGuiLeft + (GUI_WIDTH - statusWidth) / 2, 
                                  fixedGuiTop + 180, statusColor);
        
        // Draw "OPTIONS" header
        context.drawTextWithShadow(this.textRenderer, "OPTIONS", fixedGuiLeft + 30, 
                                  fixedGuiTop + 220, 0xFFFEFEFE);
        
        // Enable scissor for scrollable settings
        context.enableScissor(fixedGuiLeft + 20, fixedGuiTop + 240, 
                            fixedGuiLeft + GUI_WIDTH - 20, fixedGuiTop + GUI_HEIGHT - 20);
        
        // Draw settings
        int settingY = fixedGuiTop + 250 - (int)scrollOffset;
        int settingSpacing = 50;
        
        for (Setting setting : settings) {
            if (settingY > fixedGuiTop + 230 && settingY < fixedGuiTop + GUI_HEIGHT) {
                setting.render(context, fixedGuiLeft + 40, settingY, GUI_WIDTH - 80, 
                             transformedMouseX, transformedMouseY, this.textRenderer);
            }
            settingY += settingSpacing;
        }
        
        context.disableScissor();
        
        matrices.popMatrix();
    }
    
    private void drawToggleSwitch(DrawContext context, int x, int y, int width, int height, 
                                  boolean enabled, int mouseX, int mouseY) {
        boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        
        // Draw background track
        int trackColor = enabled ? 0xFF4CAF50 : 0xFF252220;
        if (isHovered) {
            trackColor = enabled ? 0xFF66BB6A : 0xFF2A2622;
        }
        drawRoundedButton(context, x, y, width, height, trackColor);
        
        // Draw label
        String label = enabled ? "ON" : "OFF";
        int labelX = enabled ? x + 20 : x + width - 50;
        context.drawTextWithShadow(this.textRenderer, label, labelX, y + 15, 0xFFFEFEFE);
        
        // Draw toggle knob
        int knobSize = height - 8;
        int knobX = enabled ? x + width - knobSize - 4 : x + 4;
        int knobY = y + 4;
        drawRoundedButton(context, knobX, knobY, knobSize, knobSize, 0xFFFEFEFE);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MinecraftClient client = MinecraftClient.getInstance();
        double currentScale = client.getWindow().getScaleFactor();
        float scaleFactor = (float)(FIXED_GUI_SCALE / currentScale);
        
        int mx = (int)(mouseX / scaleFactor);
        int my = (int)(mouseY / scaleFactor);
        
        int fixedScaledWidth = (int)(this.width / scaleFactor);
        int fixedScaledHeight = (int)(this.height / scaleFactor);
        int fixedGuiLeft = (fixedScaledWidth - GUI_WIDTH) / 2;
        int fixedGuiTop = (fixedScaledHeight - GUI_HEIGHT) / 2;
        
        // Check back button
        int backX = fixedGuiLeft + 15;
        int backY = fixedGuiTop + 15;
        if (mx >= backX && mx <= backX + 80 && my >= backY && my <= backY + 30) {
            client.setScreen(parent);
            return true;
        }
        
        // Check toggle switch
        int toggleX = fixedGuiLeft + (GUI_WIDTH - 200) / 2;
        int toggleY = fixedGuiTop + 120;
        if (mx >= toggleX && mx <= toggleX + 200 && my >= toggleY && my <= toggleY + 40) {
            module.toggle();
            return true;
        }
        
        // Check settings clicks
        int settingY = fixedGuiTop + 250 - (int)scrollOffset;
        int settingSpacing = 50;
        
        for (Setting setting : settings) {
            if (my >= settingY && my < settingY + 40) {
                if (setting.onClick(mx, my, fixedGuiLeft + 40, settingY, GUI_WIDTH - 80)) {
                    return true;
                }
            }
            settingY += settingSpacing;
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        MinecraftClient client = MinecraftClient.getInstance();
        double currentScale = client.getWindow().getScaleFactor();
        float scaleFactor = (float)(FIXED_GUI_SCALE / currentScale);
        
        int mx = (int)(mouseX / scaleFactor);
        int my = (int)(mouseY / scaleFactor);
        
        int fixedScaledWidth = (int)(this.width / scaleFactor);
        int fixedScaledHeight = (int)(this.height / scaleFactor);
        int fixedGuiLeft = (fixedScaledWidth - GUI_WIDTH) / 2;
        int fixedGuiTop = (fixedScaledHeight - GUI_HEIGHT) / 2;
        
        // Handle slider dragging
        int settingY = fixedGuiTop + 250 - (int)scrollOffset;
        int settingSpacing = 50;
        
        for (Setting setting : settings) {
            if (setting instanceof SliderSetting) {
                ((SliderSetting)setting).onDrag(mx, my, fixedGuiLeft + 40, settingY, GUI_WIDTH - 80);
            }
            settingY += settingSpacing;
        }
        
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Release all slider drags
        for (Setting setting : settings) {
            if (setting instanceof SliderSetting) {
                ((SliderSetting)setting).onRelease();
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int totalHeight = settings.size() * 50;
        int visibleHeight = GUI_HEIGHT - 270;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        
        scrollOffset -= verticalAmount * 20;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        
        return true;
    }
    
    @Override
    public boolean shouldPause() {
        return false;
    }
    
    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x + 2, y, x + width - 2, y + height, color);
        context.fill(x, y + 2, x + width, y + height - 2, color);
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
        context.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
        context.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
        context.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
    }
    
    private void drawRoundedButton(DrawContext context, int x, int y, int width, int height, int color) {
        drawRoundedRect(context, x, y, width, height, color);
    }
    
    private void drawRoundedBorder(DrawContext context, int x, int y, int width, int height, int color) {
        context.fill(x + 2, y, x + width - 2, y + 1, color);
        context.fill(x + 2, y + height - 1, x + width - 2, y + height, color);
        context.fill(x, y + 2, x + 1, y + height - 2, color);
        context.fill(x + width - 1, y + 2, x + width, y + height - 2, color);
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
        context.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
        context.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
        context.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
    }
    
    private void drawTexture(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, 
                          texture, x, y, 0f, 0f, width, height, width, height);
    }
    
    // Setting base class and implementations
    private abstract static class Setting {
        protected String name;
        
        public Setting(String name) {
            this.name = name;
        }
        
        public abstract void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY, net.minecraft.client.font.TextRenderer textRenderer);
        public abstract boolean onClick(int mouseX, int mouseY, int x, int y, int width);
    }
    
    private static class ToggleSetting extends Setting {
        private boolean value;
        
        public ToggleSetting(String name, boolean defaultValue) {
            super(name);
            this.value = defaultValue;
        }
        
        @Override
        public void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY, net.minecraft.client.font.TextRenderer textRenderer) {
            // Draw label
            context.drawTextWithShadow(textRenderer, name, x, y + 5, 0xFFAAAAAA);
            
            // Draw toggle switch (smaller, on the right)
            int switchWidth = 60;
            int switchHeight = 24;
            int switchX = x + width - switchWidth;
            int switchY = y;
            
            boolean isHovered = mouseX >= switchX && mouseX <= switchX + switchWidth && 
                              mouseY >= switchY && mouseY <= switchY + switchHeight;
            
            // Background
            int bgColor = value ? (isHovered ? 0xFF66BB6A : 0xFF4CAF50) : (isHovered ? 0xFF2A2622 : 0xFF252220);
            drawRoundedRect(context, switchX, switchY, switchWidth, switchHeight, bgColor);
            
            // Label
            String label = value ? "ON" : "OFF";
            int labelX = value ? switchX + 10 : switchX + switchWidth - 30;
            context.drawTextWithShadow(textRenderer, label, labelX, switchY + 8, 0xFFFEFEFE);
            
            // Knob
            int knobSize = switchHeight - 6;
            int knobX = value ? switchX + switchWidth - knobSize - 3 : switchX + 3;
            drawRoundedRect(context, knobX, switchY + 3, knobSize, knobSize, 0xFFFEFEFE);
        }
        
        @Override
        public boolean onClick(int mouseX, int mouseY, int x, int y, int width) {
            int switchWidth = 60;
            int switchHeight = 24;
            int switchX = x + width - switchWidth;
            
            if (mouseX >= switchX && mouseX <= switchX + switchWidth && 
                mouseY >= y && mouseY <= y + switchHeight) {
                value = !value;
                return true;
            }
            return false;
        }
        
        private static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
            context.fill(x + 2, y, x + width - 2, y + height, color);
            context.fill(x, y + 2, x + width, y + height - 2, color);
            context.fill(x + 1, y + 1, x + 2, y + 2, color);
            context.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
            context.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
            context.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
        }
    }
    
    private static class SliderSetting extends Setting {
        private float value;
        private float min;
        private float max;
        private boolean isDragging = false;
        
        public SliderSetting(String name, float defaultValue, float min, float max) {
            super(name);
            this.value = defaultValue;
            this.min = min;
            this.max = max;
        }
        
        @Override
        public void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY, net.minecraft.client.font.TextRenderer textRenderer) {
            // Draw label
            context.drawTextWithShadow(textRenderer, name, x, y + 5, 0xFFAAAAAA);
            
            // Draw value
            String valueText = String.format("%.2f", value);
            int valueWidth = textRenderer.getWidth(valueText);
            context.drawTextWithShadow(textRenderer, valueText, x + width - valueWidth, y + 5, 0xFFFEFEFE);
            
            // Draw slider track
            int sliderY = y + 25;
            int sliderWidth = width;
            int trackHeight = 4;
            drawRoundedRect(context, x, sliderY, sliderWidth, trackHeight, 0xFF252220);
            
            // Draw filled portion
            float percentage = (value - min) / (max - min);
            int filledWidth = (int)(sliderWidth * percentage);
            if (filledWidth > 0) {
                drawRoundedRect(context, x, sliderY, filledWidth, trackHeight, 0xFF4CAF50);
            }
            
            // Draw slider knob
            int knobSize = 12;
            int knobX = x + filledWidth - knobSize / 2;
            int knobY = sliderY - (knobSize - trackHeight) / 2;
            
            boolean isKnobHovered = mouseX >= knobX && mouseX <= knobX + knobSize && 
                                   mouseY >= knobY && mouseY <= knobY + knobSize;
            
            int knobColor = isDragging ? 0xFF66BB6A : (isKnobHovered ? 0xFF5CB860 : 0xFFFEFEFE);
            drawRoundedRect(context, knobX, knobY, knobSize, knobSize, knobColor);
        }
        
        @Override
        public boolean onClick(int mouseX, int mouseY, int x, int y, int width) {
            int sliderY = y + 25;
            int trackHeight = 4;
            int knobSize = 12;
            
            // Check if clicked on slider area
            if (mouseY >= sliderY - knobSize / 2 && mouseY <= sliderY + trackHeight + knobSize / 2) {
                isDragging = true;
                updateValue(mouseX, x, width);
                return true;
            }
            return false;
        }
        
        public void onDrag(int mouseX, int mouseY, int x, int y, int width) {
            if (isDragging) {
                updateValue(mouseX, x, width);
            }
        }
        
        public void onRelease() {
            isDragging = false;
        }
        
        private void updateValue(int mouseX, int x, int width) {
            float percentage = Math.max(0, Math.min(1, (mouseX - x) / (float)width));
            value = min + (max - min) * percentage;
        }
        
        private static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
            context.fill(x + 2, y, x + width - 2, y + height, color);
            context.fill(x, y + 2, x + width, y + height - 2, color);
            context.fill(x + 1, y + 1, x + 2, y + 2, color);
            context.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
            context.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
            context.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
        }
    }
    
    private static class ColorSetting extends Setting {
        private int color;
        private boolean isPickerOpen = false;
        
        public ColorSetting(String name, int defaultColor) {
            super(name);
            this.color = defaultColor;
        }
        
        @Override
        public void render(DrawContext context, int x, int y, int width, int mouseX, int mouseY, net.minecraft.client.font.TextRenderer textRenderer) {
            // Draw label
            context.drawTextWithShadow(textRenderer, name, x, y + 5, 0xFFAAAAAA);
            
            // Draw color preview box
            int boxSize = 24;
            int boxX = x + width - boxSize;
            int boxY = y;
            
            boolean isHovered = mouseX >= boxX && mouseX <= boxX + boxSize && 
                              mouseY >= boxY && mouseY <= boxY + boxSize;
            
            // Draw border
            drawRoundedRect(context, boxX - 1, boxY - 1, boxSize + 2, boxSize + 2, 
                          isHovered ? 0xFFFFFFFF : 0xFF2A2622);
            
            // Draw color
            drawRoundedRect(context, boxX, boxY, boxSize, boxSize, color);
            
            // Draw hex value
            String hexText = String.format("#%08X", color);
            int hexWidth = textRenderer.getWidth(hexText);
            context.drawTextWithShadow(textRenderer, hexText, boxX - hexWidth - 10, y + 5, 0xFF888888);
        }
        
        @Override
        public boolean onClick(int mouseX, int mouseY, int x, int y, int width) {
            int boxSize = 24;
            int boxX = x + width - boxSize;
            
            if (mouseX >= boxX && mouseX <= boxX + boxSize && 
                mouseY >= y && mouseY <= y + boxSize) {
                isPickerOpen = !isPickerOpen;
                // TODO: Implement color picker popup
                return true;
            }
            return false;
        }
        
        private static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
            context.fill(x + 2, y, x + width - 2, y + height, color);
            context.fill(x, y + 2, x + width, y + height - 2, color);
            context.fill(x + 1, y + 1, x + 2, y + 2, color);
            context.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
            context.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
            context.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
        }
    }
}
