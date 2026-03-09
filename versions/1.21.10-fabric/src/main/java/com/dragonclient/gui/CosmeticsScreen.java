package com.dragonclient.gui;

import com.dragonclient.cosmetics.CapeManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.text.Text;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.client.font.TextRenderer;

public class CosmeticsScreen extends Screen {
    private static final int GUI_WIDTH = 700;
    private static final int GUI_HEIGHT = 450;
    private static final int FIXED_GUI_SCALE = 2;
    
    private static final Identifier CS_STAR_ICON = Identifier.of("dragonclient", "textures/gui/cs_star_8.png");
    
    private int guiLeft;
    private int guiTop;
    private int scaledWidth;
    private int scaledHeight;
    private String selectedTab = "CAPES"; // CAPES or SKINS
    private TextRenderer bebasFont;
    private float playerRotation = 0;
    private boolean isDragging = false;
    private double lastMouseX = 0;
    private double lastMouseY = 0;
    private float scrollOffset = 0;
    private int selectedCapeIndex = -1; // -1 means no cape equipped
    private DummyPlayerEntity dummyPlayer = null;
    private int currentPage = 0; // Current page for pagination
    private static final int ITEMS_PER_PAGE = 3;
    
    private static final Identifier CAPE_1 = Identifier.of("dragonclient", "textures/capes/cape1.png");
    private static final Identifier CAPE_2 = Identifier.of("dragonclient", "textures/capes/cape2.png");
    private static final Identifier CAPE_3 = Identifier.of("dragonclient", "textures/capes/cape3.png");
    private static final Identifier CAPE_4 = Identifier.of("dragonclient", "textures/capes/cape4.png");
    
    public CosmeticsScreen() {
        super(Text.literal("Cosmetics"));
        MinecraftClient client = MinecraftClient.getInstance();
        
        // Load equipped cape from CapeManager
        this.selectedCapeIndex = CapeManager.getInstance().getEquippedCapeIndex();
        
        try {
            var fontManagerField = MinecraftClient.class.getDeclaredField("field_1772");
            fontManagerField.setAccessible(true);
            var fontManager = fontManagerField.get(client);
            var getTextRendererMethod = fontManager.getClass().getMethod("method_27542", Identifier.class);
            this.bebasFont = (TextRenderer) getTextRendererMethod.invoke(fontManager, Identifier.of("minecraft", "dragon"));
        } catch (Exception e) {
            this.bebasFont = client.textRenderer;
        }
    }

    @Override
    protected void init() {
        super.init();
        MinecraftClient client = MinecraftClient.getInstance();
        this.scaledWidth = client.getWindow().getFramebufferWidth() / FIXED_GUI_SCALE;
        this.scaledHeight = client.getWindow().getFramebufferHeight() / FIXED_GUI_SCALE;
        this.guiLeft = (scaledWidth - GUI_WIDTH) / 2;
        this.guiTop = (scaledHeight - GUI_HEIGHT) / 2;
        
        // Create dummy player for cape preview
        if (this.dummyPlayer == null) {
            this.dummyPlayer = DummyPlayerEntity.create(client);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        MinecraftClient client = MinecraftClient.getInstance();
        double currentScale = client.getWindow().getScaleFactor();
        double targetScale = FIXED_GUI_SCALE;
        float scaleFactor = (float)(targetScale / currentScale);
        
        int transformedMouseX = (int)(mouseX / scaleFactor);
        int transformedMouseY = (int)(mouseY / scaleFactor);
        
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(scaleFactor, scaleFactor); // 1.21.6+: 2D scaling only
        
        // Draw main GUI background (Smoky Black)
        drawRoundedRect(context, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF100C08);
        drawRoundedBorder(context, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF2A2622);
        
        // Draw header with gradient
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + 50, 0xFF1A1614);
        context.fill(guiLeft, guiTop + 49, guiLeft + GUI_WIDTH, guiTop + 50, 0xFF252220);
        
        // Draw "COSMETICS" title with star icon
        String title = "COSMETICS";
        int titleWidth = this.textRenderer.getWidth(title);
        int starSize = 16;
        int totalWidth = starSize + 5 + titleWidth;
        int headerStartX = guiLeft + (GUI_WIDTH - totalWidth) / 2;
        
        int starX = headerStartX;
        int starY = guiTop + 17;
        
        // 1.21.6+: Draw star icon with color tint via RenderPipelines
        drawTextureWithColor(context, CS_STAR_ICON, starX, starY, starSize, starSize, 0xFF4DFF4D); // Green hue
        
        TextRenderer font = this.bebasFont != null ? this.bebasFont : this.textRenderer;
        context.drawTextWithShadow(font, title, starX + starSize + 5, guiTop + 20, 0xFFFEFEFE);
        
        // Draw close button
        int closeX = guiLeft + GUI_WIDTH - 35;
        int closeY = guiTop + 15;
        boolean isCloseHovered = transformedMouseX >= closeX && transformedMouseX <= closeX + 25 && transformedMouseY >= closeY && transformedMouseY <= closeY + 25;
        drawRoundedButton(context, closeX, closeY, 25, 25, isCloseHovered ? 0xFFE63946 : 0xFF252220);
        context.drawCenteredTextWithShadow(this.textRenderer, "\u2715", closeX + 12, closeY + 9, 0xFFFEFEFE);
        
        // Draw content area (no tabs, start directly below header)
        int contentY = guiTop + 60;
        int contentHeight = GUI_HEIGHT - 70;
        
        // Draw player preview on left side (smaller)
        int previewX = guiLeft + 30;
        int previewY = contentY + 20;
        int previewWidth = 180;
        int previewHeight = contentHeight - 40;
        
        drawRoundedRect(context, previewX, previewY, previewWidth, previewHeight, 0x33FEFEFE); // 20% opacity white
        drawRoundedBorder(context, previewX, previewY, previewWidth, previewHeight, 0xFF2A2622);
        
        // Draw player model using InventoryScreen.drawEntity (works with 1.21.6+ 2D matrix stack)
        int playerModelX = previewX + previewWidth / 2;
        int playerModelY = previewY + previewHeight - 60;
        int modelSize = 100;
        
        // Render player entity using Minecraft's built-in method
        if (client.player != null) {
            matrices.popMatrix(); // Pop our custom scale to use screen coords for entity rendering
            
            // Calculate screen-space coordinates for entity rendering
            int entityScreenX = (int)(playerModelX * scaleFactor);
            int entityScreenY = (int)(playerModelY * scaleFactor);
            int entitySize = (int)(modelSize * scaleFactor);
            int entityBoxX1 = (int)(previewX * scaleFactor);
            int entityBoxY1 = (int)(previewY * scaleFactor);
            int entityBoxX2 = (int)((previewX + previewWidth) * scaleFactor);
            int entityBoxY2 = (int)((previewY + previewHeight) * scaleFactor);
            
            // Use InventoryScreen.drawEntity which handles 3D rendering internally
            InventoryScreen.drawEntity(context, entityBoxX1, entityBoxY1, entityBoxX2, entityBoxY2,
                entitySize, 0.0625f, 
                (float)(mouseX) - entityScreenX, 
                (float)(entityScreenY - entitySize * 0.75f) - mouseY,
                client.player);
            
            matrices.pushMatrix(); // Re-push for remaining rendering
            matrices.scale(scaleFactor, scaleFactor);
        }
        
        // Draw cosmetics grid on right side (bigger)
        int gridX = guiLeft + 230;
        int gridY = contentY + 20;
        int gridWidth = GUI_WIDTH - 250;
        int gridHeight = contentHeight - 40;
        
        drawRoundedRect(context, gridX, gridY, gridWidth, gridHeight, 0xFF1A1614);
        drawRoundedBorder(context, gridX, gridY, gridWidth, gridHeight, 0xFF2A2622);
        
        // Draw cosmetics items in grid (pagination - 3 per page)
        String categoryLabel = "AVAILABLE CAPES";
        int labelWidth = this.textRenderer.getWidth(categoryLabel);
        drawStyledText(context, categoryLabel, gridX + (gridWidth - labelWidth) / 2, gridY + 10, 0xFFAAAAAA, false);
        
        // Calculate pagination
        int totalItems = 4;
        int totalPages = (int) Math.ceil(totalItems / (double) ITEMS_PER_PAGE);
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);
        
        // Draw pagination indicator (capsule with dots)
        int capsuleWidth = 60;
        int capsuleHeight = 20;
        int capsuleX = gridX + (gridWidth - capsuleWidth) / 2;
        int capsuleY = gridY + gridHeight - 30;
        
        drawRoundedButton(context, capsuleX, capsuleY, capsuleWidth, capsuleHeight, 0xFF1A1614);
        
        // Draw page dots
        int dotSize = 6;
        int dotSpacing = 12;
        int activeDotWidth = 20;
        
        int totalDotsWidth = ((totalPages - 1) * dotSize) + activeDotWidth + ((totalPages - 1) * (dotSpacing - dotSize));
        int dotsStartX = capsuleX + (capsuleWidth - totalDotsWidth) / 2;
        
        int currentX = dotsStartX;
        for (int p = 0; p < totalPages; p++) {
            int dotY = capsuleY + (capsuleHeight - dotSize) / 2;
            
            if (p == currentPage) {
                int dotColor = 0xFFFEFEFE;
                context.fill(currentX + 3, dotY, currentX + activeDotWidth - 3, dotY + dotSize, dotColor);
                context.fill(currentX, dotY + 2, currentX + activeDotWidth, dotY + dotSize - 2, dotColor);
                context.fill(currentX + 1, dotY + 1, currentX + 2, dotY + 2, dotColor);
                context.fill(currentX + 1, dotY + dotSize - 2, currentX + 2, dotY + dotSize - 1, dotColor);
                context.fill(currentX + activeDotWidth - 2, dotY + 1, currentX + activeDotWidth - 1, dotY + 2, dotColor);
                context.fill(currentX + activeDotWidth - 2, dotY + dotSize - 2, currentX + activeDotWidth - 1, dotY + dotSize - 1, dotColor);
                currentX += activeDotWidth + (dotSpacing - dotSize);
            } else {
                int dotColor = 0xFF555555;
                context.fill(currentX, dotY, currentX + dotSize, dotY + dotSize, dotColor);
                currentX += dotSpacing;
            }
        }
        
        // Enable scissor to keep cards within grid bounds
        // 1.21.6+: enableScissor uses logical scaled GUI coordinates
        int scissorMinX = gridX;
        int scissorMinY = gridY + 35;
        int scissorMaxX = gridX + gridWidth;
        int scissorMaxY = gridY + gridHeight - 35;
        context.enableScissor(scissorMinX, scissorMinY, scissorMaxX, scissorMaxY);
        
        // Draw cosmetic item cards (3 per page)
        int cardWidth = 130;
        int cardHeight = 180;
        int cardSpacing = 15;
        int cardsPerRow = 3;
        int startCardX = gridX + 10;
        int startCardY = gridY + 50;
        
        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int row = displayIndex / cardsPerRow;
            int col = displayIndex % cardsPerRow;
            int cardX = startCardX + (col * (cardWidth + cardSpacing));
            int cardY = startCardY + (row * (cardHeight + cardSpacing));
            
            boolean isCardHovered = transformedMouseX >= cardX && transformedMouseX <= cardX + cardWidth &&
                                   transformedMouseY >= cardY && transformedMouseY <= cardY + cardHeight;
            
            boolean isCardEquipped = (selectedCapeIndex == i);
            
            // Draw card background
            int cardBgColor = isCardEquipped ? 0x80FEFEFE : 0xFF252220;
            drawRoundedRect(context, cardX, cardY, cardWidth, cardHeight, cardBgColor);
            
            if (isCardEquipped) {
                drawRoundedBorder(context, cardX, cardY, cardWidth, cardHeight, 0xFFDDDDDD);
            } else if (isCardHovered) {
                drawRoundedBorder(context, cardX, cardY, cardWidth, cardHeight, 0xFF2A2622);
            }
            
            // Draw cosmetic preview area
            int previewBoxX = cardX + 10;
            int previewBoxY = cardY + 10;
            int previewBoxWidth = cardWidth - 20;
            int previewBoxHeight = 110;
            drawRoundedRect(context, previewBoxX, previewBoxY, previewBoxWidth, previewBoxHeight, 0xFF1A1614);
            
            // Draw cape texture preview (2D cape image instead of 3D entity)
            Identifier capeTexture;
            if (i == 0) capeTexture = CAPE_1;
            else if (i == 1) capeTexture = CAPE_2;
            else if (i == 2) capeTexture = CAPE_3;
            else capeTexture = CAPE_4;
            
            // Draw cape texture scaled to fit preview box
            int capeDrawWidth = previewBoxWidth - 20;
            int capeDrawHeight = previewBoxHeight - 20;
            int capeDrawX = previewBoxX + 10;
            int capeDrawY = previewBoxY + 10;
            
            // Render cape texture as 2D preview
            drawTexture(context, capeTexture, capeDrawX, capeDrawY, capeDrawWidth, capeDrawHeight);
            
            // Draw equip button
            int equipBtnX = cardX + 10;
            int equipBtnY = cardY + cardHeight - 35;
            int equipBtnWidth = cardWidth - 20;
            boolean isEquipHovered = transformedMouseX >= equipBtnX && transformedMouseX <= equipBtnX + equipBtnWidth &&
                                    transformedMouseY >= equipBtnY && transformedMouseY <= equipBtnY + 20;
            
            boolean isEquipped = (selectedCapeIndex == i);
            int btnBgColor = isEquipped ? 0xFFE63946 : (isEquipHovered ? 0xCCFEFEFE : 0xFF1A1614);
            drawRoundedButton(context, equipBtnX, equipBtnY, equipBtnWidth, 20, btnBgColor);
            int btnTextColor = isEquipped ? 0xFFFEFEFE : (isEquipHovered ? 0xFF100C08 : 0xFFAAAAAA);
            String btnText = isEquipped ? "EQUIPPED" : "EQUIP";
            context.drawText(this.textRenderer, btnText, equipBtnX + (equipBtnWidth - this.textRenderer.getWidth(btnText)) / 2, equipBtnY + 6, btnTextColor, false);
        }
        
        // Disable scissor
        context.disableScissor();
        
        matrices.popMatrix();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        MinecraftClient client = MinecraftClient.getInstance();
        double currentScale = client.getWindow().getScaleFactor();
        double targetScale = FIXED_GUI_SCALE;
        float scaleFactor = (float)(targetScale / currentScale);
        
        int transformedMouseX = (int)(mouseX / scaleFactor);
        int transformedMouseY = (int)(mouseY / scaleFactor);
        
        // Check close button
        int closeX = guiLeft + GUI_WIDTH - 35;
        int closeY = guiTop + 15;
        if (transformedMouseX >= closeX && transformedMouseX <= closeX + 25 && transformedMouseY >= closeY && transformedMouseY <= closeY + 25) {
            this.close();
            return true;
        }
        
        // Check equip button clicks on cosmetic cards
        int gridX2 = guiLeft + 230;
        int gridY2 = guiTop + 60 + 20;
        
        int cardWidth = 130;
        int cardHeight = 180;
        int cardSpacing = 15;
        int cardsPerRow = 3;
        int startCardX = gridX2 + 10;
        int startCardY = gridY2 + 50;
        
        int totalItems = 4;
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);
        
        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int row = displayIndex / cardsPerRow;
            int col = displayIndex % cardsPerRow;
            int cardX = startCardX + (col * (cardWidth + cardSpacing));
            int cardY = startCardY + (row * (cardHeight + cardSpacing));
            
            int equipBtnX = cardX + 10;
            int equipBtnY = cardY + cardHeight - 35;
            int equipBtnWidth = cardWidth - 20;
            
            if (transformedMouseX >= equipBtnX && transformedMouseX <= equipBtnX + equipBtnWidth &&
                transformedMouseY >= equipBtnY && transformedMouseY <= equipBtnY + 20) {
                selectedCapeIndex = (selectedCapeIndex == i) ? -1 : i;
                CapeManager.getInstance().setEquippedCape(selectedCapeIndex);
                return true;
            }
        }
        
        // Check pagination dot clicks
        int gridX3 = guiLeft + 230;
        int gridY3 = guiTop + 60 + 20;
        int gridHeight3 = GUI_HEIGHT - 70 - 40;
        int capsuleWidth = 60;
        int capsuleHeight = 20;
        int capsuleX = gridX3 + ((GUI_WIDTH - 250) - capsuleWidth) / 2;
        int capsuleY = gridY3 + gridHeight3 - 30;
        
        if (transformedMouseX >= capsuleX && transformedMouseX <= capsuleX + capsuleWidth &&
            transformedMouseY >= capsuleY && transformedMouseY <= capsuleY + capsuleHeight) {
            int totalItems2 = 4;
            int totalPages = (int) Math.ceil(totalItems2 / (double) ITEMS_PER_PAGE);
            
            int dotSize = 6;
            int dotSpacing = 12;
            int activeDotWidth = 20;
            int totalDotsWidth = ((totalPages - 1) * dotSize) + activeDotWidth + ((totalPages - 1) * (dotSpacing - dotSize));
            int dotsStartX = capsuleX + (capsuleWidth - totalDotsWidth) / 2;
            
            int currentX = dotsStartX;
            for (int p = 0; p < totalPages; p++) {
                int dotWidth = (p == currentPage) ? activeDotWidth : dotSize;
                if (transformedMouseX >= currentX && transformedMouseX <= currentX + dotWidth) {
                    currentPage = p;
                    return true;
                }
                currentX += dotWidth + (dotSpacing - dotSize);
            }
        }
        
        // Check if right-click on preview area to start dragging
        if (button == 1) {
            int contentY = guiTop + 60;
            int contentHeight = GUI_HEIGHT - 70;
            int previewXArea = guiLeft + 30;
            int previewYArea = contentY + 20;
            int previewWidthArea = 180;
            int previewHeightArea = contentHeight - 40;
            
            if (transformedMouseX >= previewXArea && transformedMouseX <= previewXArea + previewWidthArea &&
                transformedMouseY >= previewYArea && transformedMouseY <= previewYArea + previewHeightArea) {
                isDragging = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    // 1.21.6+: Custom click handler (called via MixinMouse)
    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        return mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1) {
            isDragging = false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging && button == 1) {
            double dx = mouseX - lastMouseX;
            playerRotation += (float) dx * 0.5f;
            lastMouseX = mouseX;
            lastMouseY = mouseY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int totalItems = 4;
        int cardHeight = 180;
        int cardSpacing = 15;
        int cardsPerRow = 3;
        
        int rows = (int) Math.ceil(totalItems / (double) cardsPerRow);
        int totalHeight = rows * (cardHeight + cardSpacing);
        int visibleHeight = 250;
        int maxScroll = Math.max(0, totalHeight - visibleHeight);
        
        scrollOffset -= verticalAmount * 20;
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
        
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
    
    private void drawStyledText(DrawContext context, String text, int x, int y, int color, boolean shadow) {
        TextRenderer font = this.bebasFont != null ? this.bebasFont : this.textRenderer;
        if (shadow) {
            context.drawTextWithShadow(font, text, x, y, color);
        } else {
            context.drawText(font, text, x, y, color, false);
        }
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
    
    // -------------------------------------------------------------------------
    // Texture helpers — 1.21.6+: Use RenderPipelines.GUI_TEXTURED
    // -------------------------------------------------------------------------
    private void drawTexture(DrawContext context, Identifier texture,
                            int x, int y, int width, int height) {
        context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, 
                          texture, x, y, 0f, 0f, width, height, width, height, 0xFFFFFFFF);
    }
    
    private void drawTextureWithColor(DrawContext context, Identifier texture,
                                     int x, int y, int width, int height, int color) {
        context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED,
                          texture, x, y, 0f, 0f, width, height, width, height, color);
    }
}
