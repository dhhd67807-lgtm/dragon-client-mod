package com.dragonclient.gui;

import com.dragonclient.DragonClientMod;
import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.client.font.TextRenderer;

import java.util.ArrayList;
import java.util.List;

public class DragonClientScreen extends Screen {
    private ModuleCategory selectedCategory = ModuleCategory.HUD;
    private final List<Module> modules;
    private static final int GUI_WIDTH = 700;
    private static final int GUI_HEIGHT = 450;
    private static final int CARD_WIDTH = 160;
    private static final int CARD_HEIGHT = 110;
    private static final int CARD_SPACING = 10;
    private static final int CARDS_PER_ROW = 4;
    private static final int FIXED_GUI_SCALE = 2; // Always render as if GUI scale is 2
    
    // Texture identifiers
    private static final Identifier STAR_ICON = Identifier.of("dragonclient", "textures/gui/cs_star_8.png");
    private static final Identifier CARD_TEXTURE = Identifier.of("dragonclient", "textures/gui/cards.png");
    private static final Identifier ICON_1 = Identifier.of("dragonclient", "textures/gui/1.png");
    private static final Identifier ICON_2 = Identifier.of("dragonclient", "textures/gui/2.png");
    private static final Identifier ICON_4 = Identifier.of("dragonclient", "textures/gui/4.png");
    private static final Identifier ULTRA_ICON = Identifier.of("dragonclient", "textures/gui/ultra.png");
    private int guiLeft;
    private int guiTop;
    private int scaledWidth;
    private int scaledHeight;
    private float scrollOffset = 0;
    
    public DragonClientScreen() {
        super(Text.literal("Dragon Client"));
        this.modules = new ArrayList<>(DragonClientMod.getInstance().getModuleManager().getModules());
    }

    @Override
    protected void init() {
        super.init();
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Use fixed scale for GUI independence
        this.scaledWidth = client.getWindow().getFramebufferWidth() / FIXED_GUI_SCALE;
        this.scaledHeight = client.getWindow().getFramebufferHeight() / FIXED_GUI_SCALE;
        this.guiLeft = (scaledWidth - GUI_WIDTH) / 2;
        this.guiTop = (scaledHeight - GUI_HEIGHT) / 2;
    }
    
    // Helper method to draw text with default Minecraft font
    private void drawStyledText(DrawContext context, String text, int x, int y, int color, boolean shadow) {
        if (shadow) {
            context.drawTextWithShadow(this.textRenderer, text, x, y, color);
        } else {
            context.drawText(this.textRenderer, text, x, y, color, false);
        }
    }

    // Helper method to draw centered text with default Minecraft font
    private void drawCenteredStyledText(DrawContext context, String text, int x, int y, int color, boolean shadow) {
        int textWidth = this.textRenderer.getWidth(text);
        if (shadow) {
            context.drawTextWithShadow(this.textRenderer, text, x - textWidth / 2, y, color);
        } else {
            context.drawText(this.textRenderer, text, x - textWidth / 2, y, color, false);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 1.21.11: renderBackground can only be called once per frame - skip it here
        // The parent Screen class handles background rendering
        
        MinecraftClient client = MinecraftClient.getInstance();
        double currentScale = client.getWindow().getScaleFactor();
        double targetScale = FIXED_GUI_SCALE;
        float scaleFactor = (float)(targetScale / currentScale);
        
        // Transform mouse coordinates to our fixed scale space
        int transformedMouseX = (int)(mouseX / scaleFactor);
        int transformedMouseY = (int)(mouseY / scaleFactor);
        
        // Calculate GUI dimensions at fixed scale
        int fixedScaledWidth = (int)(this.width / scaleFactor);
        int fixedScaledHeight = (int)(this.height / scaleFactor);
        int fixedGuiLeft = (fixedScaledWidth - GUI_WIDTH) / 2;
        int fixedGuiTop = (fixedScaledHeight - GUI_HEIGHT) / 2;
        
        var matrices = context.getMatrices();
        matrices.push();
        matrices.scale(scaleFactor, scaleFactor, 1.0f); // 3D scaling for 1.21.1-1.21.10
        
        // Draw main GUI background with rounded corners (Smoky Black)
        drawRoundedRect(context, fixedGuiLeft, fixedGuiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF100C08);
        
        // Draw border around the GUI
        drawRoundedBorder(context, fixedGuiLeft, fixedGuiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF2A2622);
        
        // Draw top bar with subtle gradient (default style)
        context.fill(fixedGuiLeft, fixedGuiTop, fixedGuiLeft + GUI_WIDTH, fixedGuiTop + 50, 0xFF1A1614);
        context.fill(fixedGuiLeft, fixedGuiTop + 49, fixedGuiLeft + GUI_WIDTH, fixedGuiTop + 50, 0xFF252220);
        
        // Draw "MODS" title with cs_star icon
        String title = "MODS";
        int titleWidth = this.textRenderer.getWidth(title);
        int starSize = 16;
        int totalWidth = starSize + 5 + titleWidth; // star + gap + text
        int headerStartX = fixedGuiLeft + (GUI_WIDTH - totalWidth) / 2;
        
        // Draw star icon before title (centered horizontally)
        int starX = headerStartX;
        int starY = fixedGuiTop + 17;
        
        // Draw star icon (blending automatic in 1.21.11)
        drawTexture(context, STAR_ICON, starX, starY, starSize, starSize);
        
        // Draw title text after star with custom dragon font
        context.drawTextWithShadow(this.textRenderer, title, starX + starSize + 5, fixedGuiTop + 20, 0xFFFEFEFE);
        
        // Draw close button (X) with hover effect
        int closeX = fixedGuiLeft + GUI_WIDTH - 35;
        int closeY = fixedGuiTop + 15;
        boolean isCloseHovered = transformedMouseX >= closeX && transformedMouseX <= closeX + 25 && transformedMouseY >= closeY && transformedMouseY <= closeY + 25;
        drawRoundedButton(context, closeX, closeY, 25, 25, isCloseHovered ? 0xFFE63946 : 0xFF252220);
        context.drawCenteredTextWithShadow(this.textRenderer, "✕", closeX + 12, closeY + 9, 0xFFFEFEFE);
        
        // Draw category tabs with modern styling
        int tabX = fixedGuiLeft + 15;
        int tabY = fixedGuiTop + 65;
        int tabIndex = 0;
        int tabWidth = 104; // Increased from 70
        int tabSpacing = 110; // Increased spacing between tabs
        
        for (ModuleCategory category : ModuleCategory.values()) {
            boolean isSelected = category == selectedCategory;
            boolean isTabHovered = transformedMouseX >= tabX + (tabIndex * tabSpacing) && transformedMouseX <= tabX + (tabIndex * tabSpacing) + tabWidth &&
                                   transformedMouseY >= tabY && transformedMouseY <= tabY + 30;
            
            int bgColor = isSelected ? 0xFFFEFEFE : (isTabHovered ? 0xFF252220 : 0xFF1A1614);
            drawRoundedButton(context, tabX + (tabIndex * tabSpacing), tabY, tabWidth, 30, bgColor);
            
            // Draw category icon based on index
            int iconSize = 12;
            int iconX = tabX + (tabIndex * tabSpacing) + 8;
            int iconY = tabY + 9;
            
            // Map icons to categories: HUD, Visual, Movement, Player, Misc (blending automatic in 1.21.11)
            if (tabIndex == 0) { // HUD
                drawTexture(context, ICON_1, iconX, iconY, iconSize, iconSize);
            } else if (tabIndex == 1) { // Visual
                drawTexture(context, ICON_2, iconX, iconY, iconSize, iconSize);
            } else if (tabIndex == 2) { // Movement
                drawTexture(context, ICON_4, iconX, iconY, iconSize, iconSize);
            } else if (tabIndex == 3) { // Player
                drawTexture(context, ICON_4, iconX, iconY, iconSize, iconSize);
            } else if (tabIndex == 4) { // Misc
                drawTexture(context, ULTRA_ICON, iconX, iconY, iconSize, iconSize);
            }
            
            String categoryName = category.name().charAt(0) + category.name().substring(1).toLowerCase();
            int textColor = isSelected ? 0xFF100C08 : 0xFFAAAAAA;
            
            // Draw category text without shadow to avoid double font effect
            drawStyledText(context, categoryName, tabX + (tabIndex * tabSpacing) + 24, tabY + 11, textColor, false);
            
            tabIndex++;
        }
        
        // Enable scissor (clipping) to keep cards within GUI bounds
        // Scissor needs to be in framebuffer coordinates, not scaled coordinates
        int framebufferWidth = client.getWindow().getFramebufferWidth();
        int framebufferHeight = client.getWindow().getFramebufferHeight();
        
        // Calculate scissor bounds in framebuffer space
        int scissorX = (int)((fixedGuiLeft) * scaleFactor * currentScale);
        int scissorY = (int)((fixedGuiTop + 105) * scaleFactor * currentScale);
        int scissorWidth = (int)(GUI_WIDTH * scaleFactor * currentScale);
        int scissorHeight = (int)((GUI_HEIGHT - 105) * scaleFactor * currentScale);
        
        // Flip Y coordinate for OpenGL (origin at bottom-left)
        int flippedY = framebufferHeight - scissorY - scissorHeight;
        
        System.out.println("[DragonClient] Scissor: x=" + scissorX + " y=" + flippedY + " w=" + scissorWidth + " h=" + scissorHeight);
        
        // Use GL scissor directly
        com.mojang.blaze3d.systems.RenderSystem.enableScissor(scissorX, flippedY, scissorWidth, scissorHeight);
        
        // Draw module cards with scrolling support
        int startX = fixedGuiLeft + 20;
        int startY = fixedGuiTop + 110 - (int)scrollOffset;
        int cardIndex = 0;
        
        System.out.println("[DragonClient] Rendering cards for category: " + selectedCategory + ", total modules: " + modules.size());
        System.out.println("[DragonClient] GUI position: fixedGuiLeft=" + fixedGuiLeft + " fixedGuiTop=" + fixedGuiTop);
        System.out.println("[DragonClient] Scale: currentScale=" + currentScale + " targetScale=" + targetScale + " scaleFactor=" + scaleFactor);
        System.out.println("[DragonClient] Transformed mouse: " + transformedMouseX + "," + transformedMouseY);
        
        for (Module module : modules) {
            if (module.getCategory() == selectedCategory) {
                System.out.println("[DragonClient] Found module: " + module.getName() + " in category " + selectedCategory);
                int row = cardIndex / CARDS_PER_ROW;
                int col = cardIndex % CARDS_PER_ROW;
                int cardX = startX + (col * (CARD_WIDTH + CARD_SPACING));
                int cardY = startY + (row * (CARD_HEIGHT + CARD_SPACING));
                
                System.out.println("[DragonClient] Card " + cardIndex + ": " + module.getName() + 
                                 " at x=" + cardX + " y=" + cardY + " (row=" + row + " col=" + col + ")");
                System.out.println("[DragonClient]   Visible range: y > " + (fixedGuiTop + 105) + " && y < " + (fixedGuiTop + GUI_HEIGHT));
                System.out.println("[DragonClient]   Card bounds: " + cardY + " to " + (cardY + CARD_HEIGHT));
                
                // Only render if card is within bounds
                if (cardY + CARD_HEIGHT > fixedGuiTop + 105 && cardY < fixedGuiTop + GUI_HEIGHT) {
                    System.out.println("[DragonClient]   RENDERING card " + cardIndex);
                    // Check if card is hovered
                    boolean isCardHovered = transformedMouseX >= cardX && transformedMouseX <= cardX + CARD_WIDTH &&
                                           transformedMouseY >= cardY && transformedMouseY <= cardY + CARD_HEIGHT;
                    
                    // Draw solid card background first
                    String normalizedModuleName = module.getName() == null ? "" : module.getName().replace(" ", "");
                    boolean isTierTaggerCard = "TierTagger".equalsIgnoreCase(normalizedModuleName);
                    boolean isFreelookCard = "Freelook".equalsIgnoreCase(normalizedModuleName);
                    int cardBackgroundColor = isTierTaggerCard ? 0xFF3A2C12 : (isFreelookCard ? 0xFF1A2940 : 0xFF252220);
                    drawRoundedRect(context, cardX, cardY, CARD_WIDTH, CARD_HEIGHT, cardBackgroundColor);
                    
                    // Draw card texture overlay:
                    // - Gold hue for TierTagger card
                    // - Pink hue for first 3 cards
                    // - White tint for all other cards
                    if (isTierTaggerCard) {
                        drawTextureWithColor(context, CARD_TEXTURE, cardX, cardY, CARD_WIDTH, CARD_HEIGHT, 0x66FFD54F);
                    } else if (isFreelookCard) {
                        drawTextureWithColor(context, CARD_TEXTURE, cardX, cardY, CARD_WIDTH, CARD_HEIGHT, 0x66529BFF);
                    } else if (cardIndex < 3) {
                        // Pink hue for new cards (pink tint with 25% opacity)
                        drawTextureWithColor(context, CARD_TEXTURE, cardX, cardY, CARD_WIDTH, CARD_HEIGHT, 0x40FF66B2);
                    } else {
                        // Normal white for other cards (25% opacity)
                        drawTextureWithColor(context, CARD_TEXTURE, cardX, cardY, CARD_WIDTH, CARD_HEIGHT, 0x40FFFFFF);
                    }
                    
                    // Draw subtle border on hover
                    if (isCardHovered) {
                        drawRoundedBorder(context, cardX, cardY, CARD_WIDTH, CARD_HEIGHT, 0xFF2A2622);
                    }
                    
                    // Draw module name with better styling (White)
                    drawStyledText(context, module.getName(), cardX + 10, cardY + 40, 0xFFFEFEFE, true);
                    
                    // Draw module description
                    String desc = "Toggle to " + (module.isEnabled() ? "disable" : "enable");
                    drawStyledText(context, desc, cardX + 10, cardY + 55, 0xFF888888, true);
                    
                    // Draw settings icon
                    drawStyledText(context, "⚙", cardX + CARD_WIDTH - 25, cardY + 10, 0xFF888888, true);
                    
                    // Draw toggle button with rounded corners
                    int buttonBgX = cardX + 10;
                    int buttonBgY = cardY + CARD_HEIGHT - 38;
                    int buttonBgWidth = CARD_WIDTH - 20;
                    int buttonBgHeight = 28;
                    
                    boolean isEnabled = module.isEnabled();
                    int buttonColor = isEnabled ? 0xCCFEFEFE : 0xFF252220; // Reduced opacity for enabled button (CC = 80%)
                    drawRoundedButton(context, buttonBgX, buttonBgY, buttonBgWidth, buttonBgHeight, buttonColor);
                    
                    String buttonText = isEnabled ? "ENABLED" : "DISABLED";
                    int buttonTextColor = isEnabled ? 0xFF100C08 : 0xFF888888;
                    drawCenteredStyledText(context, buttonText, buttonBgX + buttonBgWidth / 2, buttonBgY + 10, buttonTextColor, false);
                }
                
                cardIndex++;
            }
        }
        
        // Disable scissor (clipping)
        com.mojang.blaze3d.systems.RenderSystem.disableScissor();
        
        matrices.pop();
    }
    
    // -------------------------------------------------------------------------
    // Texture helper — 1.21.1-1.21.10: Old signature without RenderPipelines
    // -------------------------------------------------------------------------
    private void drawTexture(DrawContext context, Identifier texture,
                            int x, int y, int width, int height) {
        // 1.21.1-1.21.10: Old signature (Identifier, x, y, z, u, v, width, height, textureWidth, textureHeight)
        context.drawTexture(texture, x, y, 0, 0f, 0f, width, height, width, height);
    }
    
    // Texture helper with color/opacity for 1.21.1-1.21.10
    private void drawTextureWithColor(DrawContext context, Identifier texture, int x, int y, int width, int height, int color) {
        // Note: Old signature doesn't support color parameter directly
        context.drawTexture(texture, x, y, 0, 0f, 0f, width, height, width, height);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MinecraftClient client = MinecraftClient.getInstance();
        double currentScale = client.getWindow().getScaleFactor();
        double targetScale = FIXED_GUI_SCALE;
        float scaleFactor = (float)(targetScale / currentScale);
        
        int mx = (int)(mouseX / scaleFactor);
        int my = (int)(mouseY / scaleFactor);
        
        // Calculate GUI dimensions at fixed scale
        int fixedScaledWidth = (int)(this.width / scaleFactor);
        int fixedScaledHeight = (int)(this.height / scaleFactor);
        int fixedGuiLeft = (fixedScaledWidth - GUI_WIDTH) / 2;
        int fixedGuiTop = (fixedScaledHeight - GUI_HEIGHT) / 2;
        
        // Check close button
        int closeX = fixedGuiLeft + GUI_WIDTH - 35;
        int closeY = fixedGuiTop + 10;
        if (mx >= closeX && mx <= closeX + 25 && my >= closeY && my <= closeY + 25) {
            this.close();
            return true;
        }
        
        // Check category tab clicks
        int tabX = fixedGuiLeft + 15;
        int tabY = fixedGuiTop + 65;
        int tabIndex = 0;
        int tabWidth = 104;
        int tabSpacing = 110;
        
        for (ModuleCategory category : ModuleCategory.values()) {
            if (mx >= tabX + (tabIndex * tabSpacing) && mx <= tabX + (tabIndex * tabSpacing) + tabWidth &&
                my >= tabY && my <= tabY + 30) {
                selectedCategory = category;
                return true;
            }
            tabIndex++;
        }
        
        // Check module card clicks
        int startX = fixedGuiLeft + 20;
        int startY = fixedGuiTop + 110 - (int)scrollOffset;
        int cardIndex = 0;
        
        for (Module module : modules) {
            if (module.getCategory() == selectedCategory) {
                int row = cardIndex / CARDS_PER_ROW;
                int col = cardIndex % CARDS_PER_ROW;
                int cardX = startX + (col * (CARD_WIDTH + CARD_SPACING));
                int cardY = startY + (row * (CARD_HEIGHT + CARD_SPACING));
                
                // Check if toggle button was clicked
                int buttonBgX = cardX + 10;
                int buttonBgY = cardY + CARD_HEIGHT - 38;
                int buttonBgWidth = CARD_WIDTH - 20;
                int buttonBgHeight = 28;
                
                if (mx >= buttonBgX && mx <= buttonBgX + buttonBgWidth &&
                    my >= buttonBgY && my <= buttonBgY + buttonBgHeight) {
                    module.toggle();
                    return true;
                }
                
                cardIndex++;
            }
        }
        
        return false;
    }

    // 1.21.11: Custom click handler (called via MixinMouse)
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        return mouseClicked(mouseX, mouseY, button);
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Calculate total content height
        int moduleCount = 0;
        for (Module module : modules) {
            if (module.getCategory() == selectedCategory) {
                moduleCount++;
            }
        }
        
        int rows = (int) Math.ceil(moduleCount / (double) CARDS_PER_ROW);
        int totalHeight = rows * (CARD_HEIGHT + CARD_SPACING);
        int visibleHeight = GUI_HEIGHT - 120; // Account for header and tabs
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        
        // Update scroll offset
        scrollOffset -= verticalAmount * 20; // Scroll speed
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
    
    // Helper method to draw rounded rectangles
    private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int color) {
        // Draw main rectangle (stay within bounds)
        context.fill(x + 2, y, x + width - 2, y + height, color);
        context.fill(x, y + 2, x + width, y + height - 2, color);
        
        // Draw corners (2x2 pixels for subtle rounding)
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
        context.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
        context.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
        context.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
    }
    
    // Helper method to draw rounded buttons with better styling
    private void drawRoundedButton(DrawContext context, int x, int y, int width, int height, int color) {
        drawRoundedRect(context, x, y, width, height, color);
    }
    
    // Helper method to draw rounded borders
    private void drawRoundedBorder(DrawContext context, int x, int y, int width, int height, int color) {
        // Top and bottom
        context.fill(x + 2, y, x + width - 2, y + 1, color);
        context.fill(x + 2, y + height - 1, x + width - 2, y + height, color);
        
        // Left and right
        context.fill(x, y + 2, x + 1, y + height - 2, color);
        context.fill(x + width - 1, y + 2, x + width, y + height - 2, color);
        
        // Corners
        context.fill(x + 1, y + 1, x + 2, y + 2, color);
        context.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
        context.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
        context.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
    }
}
