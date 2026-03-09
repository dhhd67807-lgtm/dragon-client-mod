package com.dragonclient.gui;

import com.dragonclient.cosmetics.CapeManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
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
        // Apply dark gradient background instead of blur
        context.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
        
        MinecraftClient client = MinecraftClient.getInstance();
        double currentScale = client.getWindow().getScaleFactor();
        double targetScale = FIXED_GUI_SCALE;
        float scaleFactor = (float)(targetScale / currentScale);
        
        int transformedMouseX = (int)(mouseX / scaleFactor);
        int transformedMouseY = (int)(mouseY / scaleFactor);
        
        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(scaleFactor, scaleFactor);
        
        // Draw main GUI background (Smoky Black)
        drawRoundedRect(context, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF100C08);
        drawRoundedBorder(context, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF2A2622);
        
        // Draw header with gradient
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + 50, 0xFF1A1614);
        context.fill(guiLeft, guiTop + 49, guiLeft + GUI_WIDTH, guiTop + 50, 0xFF252220);
        
        // Draw "COSMETICS" title with fics star icon
        String title = "COSMETICS";
        int titleWidth = this.textRenderer.getWidth(title);
        int starSize = 16;
        int totalWidth = starSize + 5 + titleWidth;
        int headerStartX = guiLeft + (GUI_WIDTH - totalWidth) / 2;
        
        int starX = headerStartX;
        int starY = guiTop + 17;
        
        context.drawTexture(net.minecraft.client.gl.RenderPipelines.GUI_TEXTURED, CS_STAR_ICON, starX, starY, 0, 0, starSize, starSize, starSize, starSize);
        
        TextRenderer font = this.bebasFont != null ? this.bebasFont : this.textRenderer;
        context.drawTextWithShadow(font, title, starX + starSize + 5, guiTop + 20, 0xFFFEFEFE);
        
        // Draw close button
        int closeX = guiLeft + GUI_WIDTH - 35;
        int closeY = guiTop + 15;
        boolean isCloseHovered = transformedMouseX >= closeX && transformedMouseX <= closeX + 25 && transformedMouseY >= closeY && transformedMouseY <= closeY + 25;
        drawRoundedButton(context, closeX, closeY, 25, 25, isCloseHovered ? 0xFFE63946 : 0xFF252220);
        context.drawCenteredTextWithShadow(this.textRenderer, "✕", closeX + 12, closeY + 9, 0xFFFEFEFE);
        
        // Draw tabs (CAPES and SKINS) - centered horizontally
        String[] tabs = {"CAPES", "SKINS"};
        int totalTabsWidth = (tabs.length * 90) + ((tabs.length - 1) * 5); // 90px per tab + 5px spacing
        int tabX = guiLeft + (GUI_WIDTH - totalTabsWidth) / 2; // Center horizontally
        int tabY = guiTop + 65;
        
        for (int i = 0; i < tabs.length; i++) {
            boolean isSelected = tabs[i].equals(selectedTab);
            boolean isTabHovered = transformedMouseX >= tabX + (i * 95) && transformedMouseX <= tabX + (i * 95) + 90 &&
                                   transformedMouseY >= tabY && transformedMouseY <= tabY + 30;
            
            int bgColor = isSelected ? 0xFFFEFEFE : (isTabHovered ? 0xFF252220 : 0xFF1A1614);
            drawRoundedButton(context, tabX + (i * 95), tabY, 90, 30, bgColor);
            
            int textColor = isSelected ? 0xFF100C08 : 0xFFAAAAAA;
            int textWidth = this.textRenderer.getWidth(tabs[i]);
            drawStyledText(context, tabs[i], tabX + (i * 95) + (90 - textWidth) / 2, tabY + 11, textColor, false); // Center text
        }
        
        // Draw content area
        int contentY = guiTop + 110;
        int contentHeight = GUI_HEIGHT - 120;
        
        // Draw player preview on left side (smaller)
        int previewX = guiLeft + 30;
        int previewY = contentY + 20;
        int previewWidth = 180; // Reduced from 200
        int previewHeight = contentHeight - 40;
        
        drawRoundedRect(context, previewX, previewY, previewWidth, previewHeight, 0x33FEFEFE); // 20% opacity white
        drawRoundedBorder(context, previewX, previewY, previewWidth, previewHeight, 0xFF2A2622);
        
        // Draw player model
        int playerModelX = previewX + previewWidth / 2;
        int playerModelY = previewY + previewHeight - 60;
        int modelSize = 100;
        
        // Render 3D player model with rotation
        if (client.player != null) {
            drawEntity(context, playerModelX, playerModelY, modelSize, playerRotation, 0, client.player);
        }
        
        // Draw cosmetics grid on right side (bigger)
        int gridX = guiLeft + 230; // Moved left from 250
        int gridY = contentY + 20;
        int gridWidth = GUI_WIDTH - 250; // Increased from 270
        int gridHeight = contentHeight - 40;
        
        drawRoundedRect(context, gridX, gridY, gridWidth, gridHeight, 0xFF1A1614);
        drawRoundedBorder(context, gridX, gridY, gridWidth, gridHeight, 0xFF2A2622);
        
        // Draw cosmetics items in grid (pagination - 3 per page)
        String categoryLabel = selectedTab.equals("CAPES") ? "AVAILABLE CAPES" : "AVAILABLE SKINS";
        int labelWidth = this.textRenderer.getWidth(categoryLabel);
        drawStyledText(context, categoryLabel, gridX + (gridWidth - labelWidth) / 2, gridY + 10, 0xFFAAAAAA, false);
        
        // Calculate pagination
        int totalItems = selectedTab.equals("CAPES") ? 4 : 0;
        int totalPages = (int) Math.ceil(totalItems / (double) ITEMS_PER_PAGE);
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);
        
        // Draw pagination indicator (capsule with dots)
        int capsuleWidth = 60;
        int capsuleHeight = 20;
        int capsuleX = gridX + (gridWidth - capsuleWidth) / 2;
        int capsuleY = gridY + gridHeight - 30;
        
        // Draw capsule background
        drawRoundedButton(context, capsuleX, capsuleY, capsuleWidth, capsuleHeight, 0xFF1A1614);
        
        // Draw page dots (current page is elongated capsule)
        int dotSize = 6;
        int dotSpacing = 12;
        int activeDotWidth = 20; // Elongated width for active page
        
        // Calculate total width with elongated active dot
        int totalDotsWidth = ((totalPages - 1) * dotSize) + activeDotWidth + ((totalPages - 1) * (dotSpacing - dotSize));
        int dotsStartX = capsuleX + (capsuleWidth - totalDotsWidth) / 2;
        
        int currentX = dotsStartX;
        for (int p = 0; p < totalPages; p++) {
            int dotY = capsuleY + (capsuleHeight - dotSize) / 2;
            
            if (p == currentPage) {
                // Draw elongated capsule for current page
                int dotColor = 0xFFFEFEFE;
                // Draw rounded capsule
                context.fill(currentX + 3, dotY, currentX + activeDotWidth - 3, dotY + dotSize, dotColor);
                context.fill(currentX, dotY + 2, currentX + activeDotWidth, dotY + dotSize - 2, dotColor);
                // Rounded ends
                context.fill(currentX + 1, dotY + 1, currentX + 2, dotY + 2, dotColor);
                context.fill(currentX + 1, dotY + dotSize - 2, currentX + 2, dotY + dotSize - 1, dotColor);
                context.fill(currentX + activeDotWidth - 2, dotY + 1, currentX + activeDotWidth - 1, dotY + 2, dotColor);
                context.fill(currentX + activeDotWidth - 2, dotY + dotSize - 2, currentX + activeDotWidth - 1, dotY + dotSize - 1, dotColor);
                currentX += activeDotWidth + (dotSpacing - dotSize);
            } else {
                // Draw small circle for inactive pages
                int dotColor = 0xFF555555;
                context.fill(currentX, dotY, currentX + dotSize, dotY + dotSize, dotColor);
                currentX += dotSpacing;
            }
        }
        
        // Enable scissor (clipping) to keep cards within grid bounds
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
        
        for (int i = startIndex; i < endIndex; i++) { // Show items for current page
            int displayIndex = i - startIndex; // Index within current page
            int row = displayIndex / cardsPerRow;
            int col = displayIndex % cardsPerRow;
            int cardX = startCardX + (col * (cardWidth + cardSpacing));
            int cardY = startCardY + (row * (cardHeight + cardSpacing));
            
            boolean isCardHovered = transformedMouseX >= cardX && transformedMouseX <= cardX + cardWidth &&
                                   transformedMouseY >= cardY && transformedMouseY <= cardY + cardHeight;
            
            boolean isCardEquipped = (selectedTab.equals("CAPES") && selectedCapeIndex == i);
            
            // Draw card background (white with 50% opacity if equipped, dark if not)
            int cardBgColor = isCardEquipped ? 0x80FEFEFE : 0xFF252220; // 0x80 = 50% opacity
            drawRoundedRect(context, cardX, cardY, cardWidth, cardHeight, cardBgColor);
            
            // Draw border
            if (isCardEquipped) {
                drawRoundedBorder(context, cardX, cardY, cardWidth, cardHeight, 0xFFDDDDDD);
            } else if (isCardHovered) {
                drawRoundedBorder(context, cardX, cardY, cardWidth, cardHeight, 0xFF2A2622);
            }
            
            // Draw cosmetic preview area with background
            int previewBoxX = cardX + 10;
            int previewBoxY = cardY + 10;
            int previewBoxWidth = cardWidth - 20;
            int previewBoxHeight = 110; // Increased from 100
            drawRoundedRect(context, previewBoxX, previewBoxY, previewBoxWidth, previewBoxHeight, 0xFF1A1614);
            
            // Draw cape preview if in CAPES tab - render dummy player from back
            if (selectedTab.equals("CAPES") && dummyPlayer != null) {
                // Set cape on dummy player based on index
                Identifier capeTexture;
                if (i == 0) capeTexture = CAPE_1;
                else if (i == 1) capeTexture = CAPE_2;
                else if (i == 2) capeTexture = CAPE_3;
                else capeTexture = CAPE_4;
                
                dummyPlayer.setCustomCape(capeTexture);
                
                // Update dummy player state for proper cape rendering
                dummyPlayer.age++;
                dummyPlayer.tick(); // Update cape physics
                
                // Render larger player model showing cape from back/side view (zoomed in)
                int modelX = previewBoxX + previewBoxWidth / 2;
                int modelY = previewBoxY + previewBoxHeight + 20;
                int previewModelSize = 50; // Zoomed in
                
                // Save matrix state
                var previewMatrices = context.getMatrices();
                previewMatrices.pushMatrix();
                
                // Enable scissor for preview box
                int previewScissorMinX = previewBoxX;
                int previewScissorMinY = previewBoxY;
                int previewScissorMaxX = previewBoxX + previewBoxWidth;
                int previewScissorMaxY = previewBoxY + previewBoxHeight;
                context.enableScissor(previewScissorMinX, previewScissorMinY, previewScissorMaxX, previewScissorMaxY);
                
                // Render dummy player from back-side angle (135 degrees) to show cape better
                if (dummyPlayer != null) {
                    drawEntity(context, modelX, modelY, previewModelSize, 135.0f, 0, dummyPlayer);
                }
                
                context.disableScissor();
                previewMatrices.popMatrix();
            }
            
            // Draw equip button (moved up to reduce gap)
            int equipBtnX = cardX + 10;
            int equipBtnY = cardY + cardHeight - 35; // Adjusted for bigger card
            int equipBtnWidth = cardWidth - 20;
            boolean isEquipHovered = transformedMouseX >= equipBtnX && transformedMouseX <= equipBtnX + equipBtnWidth &&
                                    transformedMouseY >= equipBtnY && transformedMouseY <= equipBtnY + 20;
            
            boolean isEquipped = (selectedTab.equals("CAPES") && selectedCapeIndex == i);
            int btnBgColor = isEquipped ? 0xFFE63946 : (isEquipHovered ? 0xCCFEFEFE : 0xFF1A1614); // Red when equipped
            drawRoundedButton(context, equipBtnX, equipBtnY, equipBtnWidth, 20, btnBgColor);
            int btnTextColor = isEquipped ? 0xFFFEFEFE : (isEquipHovered ? 0xFF100C08 : 0xFFAAAAAA);
            String btnText = isEquipped ? "EQUIPPED" : "EQUIP";
            context.drawText(this.textRenderer, btnText, equipBtnX + (equipBtnWidth - this.textRenderer.getWidth(btnText)) / 2, equipBtnY + 6, btnTextColor, false);
        }
        
        // Disable scissor (clipping)
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
        
        // Check tab clicks
        String[] tabs = {"CAPES", "SKINS"};
        int totalTabsWidth = (tabs.length * 90) + ((tabs.length - 1) * 5);
        int tabX = guiLeft + (GUI_WIDTH - totalTabsWidth) / 2; // Match centered position
        int tabY = guiTop + 65;
        
        for (int i = 0; i < tabs.length; i++) {
            if (transformedMouseX >= tabX + (i * 95) && transformedMouseX <= tabX + (i * 95) + 90 &&
                transformedMouseY >= tabY && transformedMouseY <= tabY + 30) {
                selectedTab = tabs[i];
                scrollOffset = 0; // Reset scroll when changing tabs
                currentPage = 0; // Reset page when changing tabs
                return true;
            }
        }
        
        // Check equip button clicks on cosmetic cards
        int gridX2 = guiLeft + 230; // Match the new gridX
        int gridY2 = guiTop + 110 + 20;
        
        int cardWidth = 130; // Match the new card width
        int cardHeight = 180; // Match the new card height
        int cardSpacing = 15;
        int cardsPerRow = 3;
        int startCardX = gridX2 + 10; // Match the new startCardX
        int startCardY = gridY2 + 50; // Match the new startCardY
        
        int totalItems = selectedTab.equals("CAPES") ? 4 : 0;
        int startIndex = currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, totalItems);
        
        for (int i = startIndex; i < endIndex; i++) {
            int displayIndex = i - startIndex;
            int row = displayIndex / cardsPerRow;
            int col = displayIndex % cardsPerRow;
            int cardX = startCardX + (col * (cardWidth + cardSpacing));
            int cardY = startCardY + (row * (cardHeight + cardSpacing));
            
            int equipBtnX = cardX + 10;
            int equipBtnY = cardY + cardHeight - 35; // Match the render position
            int equipBtnWidth = cardWidth - 20;
            
            if (transformedMouseX >= equipBtnX && transformedMouseX <= equipBtnX + equipBtnWidth &&
                transformedMouseY >= equipBtnY && transformedMouseY <= equipBtnY + 20) {
                if (selectedTab.equals("CAPES")) {
                    selectedCapeIndex = (selectedCapeIndex == i) ? -1 : i; // Toggle equip
                    CapeManager.getInstance().setEquippedCape(selectedCapeIndex);
                }
                return true;
            }
        }
        
        // Check pagination dot clicks
        int gridX3 = guiLeft + 230;
        int gridY3 = guiTop + 110 + 20;
        int gridHeight3 = GUI_HEIGHT - 120 - 40;
        int capsuleWidth = 60;
        int capsuleHeight = 20;
        int capsuleX = gridX3 + ((GUI_WIDTH - 250) - capsuleWidth) / 2;
        int capsuleY = gridY3 + gridHeight3 - 30;
        
        if (transformedMouseX >= capsuleX && transformedMouseX <= capsuleX + capsuleWidth &&
            transformedMouseY >= capsuleY && transformedMouseY <= capsuleY + capsuleHeight) {
            int totalItems2 = selectedTab.equals("CAPES") ? 4 : 0;
            int totalPages = (int) Math.ceil(totalItems2 / (double) ITEMS_PER_PAGE);
            
            // Calculate which dot was clicked
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
        if (button == 1) { // Right click
            int contentY = guiTop + 110;
            int contentHeight = GUI_HEIGHT - 120;
            int previewX = guiLeft + 30;
            int previewY = contentY + 20;
            int previewWidth = 200;
            int previewHeight = contentHeight - 40;
            
            if (transformedMouseX >= previewX && transformedMouseX <= previewX + previewWidth &&
                transformedMouseY >= previewY && transformedMouseY <= previewY + previewHeight) {
                isDragging = true;
                lastMouseX = mouseX;
                lastMouseY = mouseY;
                return true;
            }
        }
        
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 1) { // Right click
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
        int totalItems = selectedTab.equals("CAPES") ? 4 : 0;
        int cardHeight = 180; // Match the new card height
        int cardSpacing = 15;
        int cardsPerRow = 3;
        
        int rows = (int) Math.ceil(totalItems / (double) cardsPerRow);
        int totalHeight = rows * (cardHeight + cardSpacing);
        int visibleHeight = 250; // Approximate visible area
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
    
    private int getGradientColor(float progress) {
        // Create a smooth gradient: purple -> blue -> cyan -> purple
        progress = progress % 1.0f;
        
        if (progress < 0.33f) {
            // Purple to Blue
            float t = progress / 0.33f;
            int r = (int) (138 + (41 - 138) * t);
            int g = (int) (43 + (98 - 43) * t);
            int b = (int) (226 + (255 - 226) * t);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        } else if (progress < 0.66f) {
            // Blue to Cyan
            float t = (progress - 0.33f) / 0.33f;
            int r = (int) (41 + (0 - 41) * t);
            int g = (int) (98 + (229 - 98) * t);
            int b = 255;
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        } else {
            // Cyan to Purple
            float t = (progress - 0.66f) / 0.34f;
            int r = (int) (0 + (138 - 0) * t);
            int g = (int) (229 + (43 - 229) * t);
            int b = (int) (255 + (226 - 255) * t);
            return 0xFF000000 | (r << 16) | (g << 8) | b;
        }
    }
    
    private void drawEntity(DrawContext context, int x, int y, int size, float rotation, float pitch, net.minecraft.entity.LivingEntity entity) {
        // For 1.21.6+, manually render entity using 2D matrices for GUI and 3D for entity
        if (entity == null) {
            return;
        }
        
        try {
            // Create separate 3D MatrixStack for entity rendering
            net.minecraft.client.util.math.MatrixStack matrices = new net.minecraft.client.util.math.MatrixStack();
            matrices.push();
            matrices.translate(x, y, 50);
            matrices.scale(size, size, -size);
            
            var quaternion = new org.joml.Quaternionf().rotationXYZ(0, 0, (float) Math.PI);
            var quaternion2 = new org.joml.Quaternionf().rotationXYZ(pitch * 0.2f, 0, 0);
            quaternion.mul(quaternion2);
            matrices.multiply(quaternion);
            
            // Set rotation
            entity.bodyYaw = 180.0f + rotation;
            entity.setYaw(180.0f + rotation);
            entity.setPitch(-pitch * 20.0f);
            entity.headYaw = entity.getYaw();
            
            var entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
            quaternion2.conjugate();
            entityRenderDispatcher.setRotation(quaternion2);
            entityRenderDispatcher.setRenderShadows(false);
            
            var immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            
            entityRenderDispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, matrices, immediate, 15728880);
            immediate.draw();
            
            entityRenderDispatcher.setRenderShadows(true);
            
            matrices.pop();
        } catch (Exception e) {
            // Silently fail
        }
    }
}
