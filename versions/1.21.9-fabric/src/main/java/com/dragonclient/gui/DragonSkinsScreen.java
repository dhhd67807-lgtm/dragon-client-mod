package com.dragonclient.gui;

import com.dragonclient.cosmetics.GearSkinManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

public class DragonSkinsScreen extends Screen {
    private static final int GUI_WIDTH = 700;
    private static final int GUI_HEIGHT = 450;
    private static final int CARD_WIDTH = 210;
    private static final int CARD_HEIGHT = 155;
    private static final int CARD_SPACING = 12;
    private static final float FIXED_UI_TARGET_SCALE = 2.0f;
    private static final int CARDS_PER_ROW = 3;
    private static final int ROWS_PER_PAGE = 2;
    private static final int CARDS_PER_PAGE = CARDS_PER_ROW * ROWS_PER_PAGE;
    private static final int PREVIEW_PANEL_WIDTH = 130;
    private static final int PREVIEW_PANEL_HEIGHT = 90;
    private static final int BUTTON_HEIGHT = 22;
    private static final int PAGE_BUTTON_WIDTH = 22;
    private static final int PAGE_BUTTON_HEIGHT = 18;
    private static final int CATEGORY_TAB_HEIGHT = 20;
    private static final int CATEGORY_TAB_GAP = 8;

    private static final Identifier STAR_ICON = Identifier.of("dragonclient", "textures/gui/cs_star_8.png");
    private static final Identifier CARD_TEXTURE = Identifier.of("dragonclient", "textures/gui/cards.png");

    private static final class SkinCard {
        private final GearSkinManager.Category category;
        private final int skinIndex;

        private SkinCard(GearSkinManager.Category category, int skinIndex) {
            this.category = category;
            this.skinIndex = skinIndex;
        }
    }

    private int guiLeft;
    private int guiTop;
    private final GearSkinManager.Category[] categories = GearSkinManager.Category.values();
    private final List<SkinCard> skinCards = new ArrayList<>();
    private DummyPlayerEntity previewDummy;
    private int currentPage = 0;
    private GearSkinManager.Category selectedCategory = GearSkinManager.Category.SWORD;
    private float renderScale = 1.0f;

    public DragonSkinsScreen() {
        super(Text.literal("Dragon Skins"));
    }

    @Override
    protected void init() {
        super.init();
        MinecraftClient client = MinecraftClient.getInstance();
        double windowScale = client.getWindow().getScaleFactor();
        this.renderScale = (float) (FIXED_UI_TARGET_SCALE / Math.max(1.0, windowScale));
        if (this.renderScale <= 0.0f) {
            this.renderScale = 1.0f;
        }

        int virtualWidth = Math.max(1, Math.round(this.width / this.renderScale));
        int virtualHeight = Math.max(1, Math.round(this.height / this.renderScale));
        this.guiLeft = (virtualWidth - GUI_WIDTH) / 2;
        this.guiTop = (virtualHeight - GUI_HEIGHT) / 2;
        this.previewDummy = DummyPlayerEntity.create(client);
        rebuildSkinCards();
    }

    private void rebuildSkinCards() {
        skinCards.clear();
        int count = GearSkinManager.getSkinCount(selectedCategory);
        for (int i = 0; i < count; i++) {
            skinCards.add(new SkinCard(selectedCategory, i));
        }

        int totalPages = getTotalPages();
        if (currentPage >= totalPages) {
            currentPage = Math.max(0, totalPages - 1);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float scaleFactor = this.renderScale;
        int mx = (int) (mouseX / scaleFactor);
        int my = (int) (mouseY / scaleFactor);

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(scaleFactor, scaleFactor);

        drawRoundedRect(context, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF100C08);
        drawRoundedBorder(context, guiLeft, guiTop, GUI_WIDTH, GUI_HEIGHT, 0xFF2A2622);
        context.fill(guiLeft, guiTop, guiLeft + GUI_WIDTH, guiTop + 50, 0xFF1A1614);
        context.fill(guiLeft, guiTop + 49, guiLeft + GUI_WIDTH, guiTop + 50, 0xFF252220);

        int titleWidth = this.textRenderer.getWidth("SKINS");
        int headerStartX = guiLeft + (GUI_WIDTH - (16 + 5 + titleWidth)) / 2;
        drawTexture(context, STAR_ICON, headerStartX, guiTop + 17, 16, 16);
        context.drawTextWithShadow(this.textRenderer, "SKINS", headerStartX + 21, guiTop + 20, 0xFFFEFEFE);

        drawCategoryTabs(context, mx, my);

        int closeX = guiLeft + GUI_WIDTH - 35;
        int closeY = guiTop + 15;
        boolean closeHover = mx >= closeX && mx <= closeX + 25 && my >= closeY && my <= closeY + 25;
        drawRoundedButton(context, closeX, closeY, 25, 25, closeHover ? 0xFFE63946 : 0xFF252220);
        context.drawCenteredTextWithShadow(this.textRenderer, "✕", closeX + 12, closeY + 9, 0xFFFEFEFE);

        int start = currentPage * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, skinCards.size());
        for (int i = start; i < end; i++) {
            int localIndex = i - start;
            SkinCard card = skinCards.get(i);
            int x = cardX(localIndex);
            int y = cardY(localIndex);

            boolean enabled = GearSkinManager.isEnabled(card.category);
            boolean selected = GearSkinManager.isSelected(card.category, card.skinIndex);
            boolean active = enabled && selected;

            int cardColor = active ? 0xFF2E281F : 0xFF252220;
            int overlay = active ? 0x55FFD36D : 0x40FFFFFF;
            drawRoundedRect(context, x, y, CARD_WIDTH, CARD_HEIGHT, cardColor);
            drawTextureWithColor(context, CARD_TEXTURE, x, y, CARD_WIDTH, CARD_HEIGHT, overlay);
            drawRoundedBorder(context, x, y, CARD_WIDTH, CARD_HEIGHT, 0xFF2A2622);

            context.drawTextWithShadow(this.textRenderer, card.category.title(), x + 12, y + 14, 0xFFFEFEFE);
            context.drawText(this.textRenderer, GearSkinManager.getSkinLabel(card.category, card.skinIndex), x + 12, y + 29, 0xFFE5E5E5, false);

            int previewX = previewPanelX(x);
            int previewY = previewPanelY(y);
            int actionX = actionButtonX(x);
            int actionY = actionButtonY(y);

            renderCardPreview(context, previewX, previewY, card, scaleFactor);

            int buttonColor = active ? 0xCCFEFEFE : 0xFF1A1614;
            int buttonText = active ? 0xFF100C08 : 0xFFFEFEFE;
            drawRoundedButton(context, actionX, actionY, actionButtonWidth(), BUTTON_HEIGHT, buttonColor);
            drawCenteredText(context, active ? "ENABLED" : "APPLY", actionX + actionButtonWidth() / 2, actionY + 7, buttonText);
        }

        drawPagination(context, mx, my);
        matrices.popMatrix();
    }

    private void drawTexture(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 0f, 0f, width, height, width, height, 0xFFFFFFFF);
    }

    private void drawTextureWithColor(DrawContext context, Identifier texture, int x, int y, int width, int height, int color) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, x, y, 0f, 0f, width, height, width, height, color);
    }

    private void drawPagination(DrawContext context, int mx, int my) {
        int totalPages = getTotalPages();
        if (totalPages <= 1) {
            return;
        }

        int navY = guiTop + GUI_HEIGHT - 24;
        int leftX = guiLeft + GUI_WIDTH / 2 - 36;
        int rightX = guiLeft + GUI_WIDTH / 2 + 14;
        boolean canPrev = currentPage > 0;
        boolean canNext = currentPage < totalPages - 1;
        boolean leftHover = mx >= leftX && mx <= leftX + PAGE_BUTTON_WIDTH && my >= navY && my <= navY + PAGE_BUTTON_HEIGHT;
        boolean rightHover = mx >= rightX && mx <= rightX + PAGE_BUTTON_WIDTH && my >= navY && my <= navY + PAGE_BUTTON_HEIGHT;

        drawRoundedButton(context, leftX, navY, PAGE_BUTTON_WIDTH, PAGE_BUTTON_HEIGHT,
            canPrev ? (leftHover ? 0xFF393330 : 0xFF252220) : 0xFF151312);
        drawRoundedButton(context, rightX, navY, PAGE_BUTTON_WIDTH, PAGE_BUTTON_HEIGHT,
            canNext ? (rightHover ? 0xFF393330 : 0xFF252220) : 0xFF151312);
        context.drawCenteredTextWithShadow(this.textRenderer, "<", leftX + PAGE_BUTTON_WIDTH / 2, navY + 5, canPrev ? 0xFFFEFEFE : 0xFF636363);
        context.drawCenteredTextWithShadow(this.textRenderer, ">", rightX + PAGE_BUTTON_WIDTH / 2, navY + 5, canNext ? 0xFFFEFEFE : 0xFF636363);

        String pageText = (currentPage + 1) + " / " + totalPages;
        context.drawCenteredTextWithShadow(this.textRenderer, pageText, guiLeft + GUI_WIDTH / 2, navY + 5, 0xFFCCCCCC);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int mx = (int) (mouseX / this.renderScale);
        int my = (int) (mouseY / this.renderScale);

        int closeX = guiLeft + GUI_WIDTH - 35;
        int closeY = guiTop + 15;
        if (mx >= closeX && mx <= closeX + 25 && my >= closeY && my <= closeY + 25) {
            this.close();
            return true;
        }

        if (handleCategoryTabClick(mx, my)) {
            return true;
        }

        if (handlePaginationClick(mx, my)) {
            return true;
        }

        int start = currentPage * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, skinCards.size());
        for (int i = start; i < end; i++) {
            int localIndex = i - start;
            SkinCard card = skinCards.get(i);
            int x = cardX(localIndex);
            int y = cardY(localIndex);

            int previewX = previewPanelX(x);
            int previewY = previewPanelY(y);
            int actionX = actionButtonX(x);
            int actionY = actionButtonY(y);

            if (mx >= actionX && mx <= actionX + actionButtonWidth() &&
                my >= actionY && my <= actionY + BUTTON_HEIGHT) {
                applyOrDisable(card);
                return true;
            }

            if (mx >= previewX && mx <= previewX + PREVIEW_PANEL_WIDTH &&
                my >= previewY && my <= previewY + PREVIEW_PANEL_HEIGHT) {
                applyOrDisable(card);
                return true;
            }
        }

        return false;
    }

    private void drawCategoryTabs(DrawContext context, int mx, int my) {
        int tabsY = guiTop + 58;
        int totalWidth = 0;
        int[] tabWidths = new int[categories.length];
        for (int i = 0; i < categories.length; i++) {
            tabWidths[i] = Math.max(74, this.textRenderer.getWidth(categories[i].title()) + 18);
            totalWidth += tabWidths[i];
        }
        totalWidth += (categories.length - 1) * CATEGORY_TAB_GAP;
        int x = guiLeft + (GUI_WIDTH - totalWidth) / 2;

        for (int i = 0; i < categories.length; i++) {
            GearSkinManager.Category category = categories[i];
            int width = tabWidths[i];
            boolean selected = category == selectedCategory;
            boolean hover = mx >= x && mx <= x + width && my >= tabsY && my <= tabsY + CATEGORY_TAB_HEIGHT;
            int color = selected ? 0xCCFEFEFE : (hover ? 0xFF312C29 : 0xFF1A1614);
            int textColor = selected ? 0xFF100C08 : 0xFFFEFEFE;

            drawRoundedButton(context, x, tabsY, width, CATEGORY_TAB_HEIGHT, color);
            drawRoundedBorder(context, x, tabsY, width, CATEGORY_TAB_HEIGHT, 0xFF2A2622);
            drawCenteredText(context, category.title(), x + width / 2, tabsY + 6, textColor);
            x += width + CATEGORY_TAB_GAP;
        }
    }

    private boolean handleCategoryTabClick(int mx, int my) {
        int tabsY = guiTop + 58;
        int totalWidth = 0;
        int[] tabWidths = new int[categories.length];
        for (int i = 0; i < categories.length; i++) {
            tabWidths[i] = Math.max(74, this.textRenderer.getWidth(categories[i].title()) + 18);
            totalWidth += tabWidths[i];
        }
        totalWidth += (categories.length - 1) * CATEGORY_TAB_GAP;
        int x = guiLeft + (GUI_WIDTH - totalWidth) / 2;

        for (int i = 0; i < categories.length; i++) {
            int width = tabWidths[i];
            if (mx >= x && mx <= x + width && my >= tabsY && my <= tabsY + CATEGORY_TAB_HEIGHT) {
                GearSkinManager.Category clicked = categories[i];
                if (clicked != selectedCategory) {
                    selectedCategory = clicked;
                    currentPage = 0;
                    rebuildSkinCards();
                }
                return true;
            }
            x += width + CATEGORY_TAB_GAP;
        }
        return false;
    }

    public boolean handleMouseClick(double mouseX, double mouseY, int button) {
        return mouseClicked(mouseX, mouseY, button);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.close();
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private boolean handlePaginationClick(int mx, int my) {
        int totalPages = getTotalPages();
        if (totalPages <= 1) {
            return false;
        }

        int navY = guiTop + GUI_HEIGHT - 24;
        int leftX = guiLeft + GUI_WIDTH / 2 - 36;
        int rightX = guiLeft + GUI_WIDTH / 2 + 14;

        if (mx >= leftX && mx <= leftX + PAGE_BUTTON_WIDTH && my >= navY && my <= navY + PAGE_BUTTON_HEIGHT && currentPage > 0) {
            currentPage--;
            return true;
        }
        if (mx >= rightX && mx <= rightX + PAGE_BUTTON_WIDTH && my >= navY && my <= navY + PAGE_BUTTON_HEIGHT && currentPage < totalPages - 1) {
            currentPage++;
            return true;
        }
        return false;
    }

    private void applyOrDisable(SkinCard card) {
        boolean active = GearSkinManager.isEnabled(card.category) && GearSkinManager.isSelected(card.category, card.skinIndex);
        if (active) {
            GearSkinManager.setEnabled(card.category, false);
            return;
        }
        GearSkinManager.selectSkin(card.category, card.skinIndex);
        GearSkinManager.setEnabled(card.category, true);
    }

    private int cardX(int indexOnPage) {
        int cardStartX = guiLeft + 20;
        int col = indexOnPage % CARDS_PER_ROW;
        return cardStartX + col * (CARD_WIDTH + CARD_SPACING);
    }

    private int cardY(int indexOnPage) {
        int cardStartY = guiTop + 90;
        int row = indexOnPage / CARDS_PER_ROW;
        return cardStartY + row * (CARD_HEIGHT + CARD_SPACING);
    }

    private int previewPanelX(int cardX) {
        return cardX + (CARD_WIDTH - PREVIEW_PANEL_WIDTH) / 2;
    }

    private int previewPanelY(int cardY) {
        return cardY + 34;
    }

    private int actionButtonX(int cardX) {
        return cardX + 12;
    }

    private int actionButtonY(int cardY) {
        return cardY + CARD_HEIGHT - 30;
    }

    private int actionButtonWidth() {
        return CARD_WIDTH - 24;
    }

    private int getTotalPages() {
        return Math.max(1, (int) Math.ceil(skinCards.size() / (double) CARDS_PER_PAGE));
    }

    private void drawRoundedRect(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 2, y, x + w - 2, y + h, color);
        ctx.fill(x, y + 2, x + w, y + h - 2, color);
        ctx.fill(x + 1, y + 1, x + 2, y + 2, color);
        ctx.fill(x + w - 2, y + 1, x + w - 1, y + 2, color);
        ctx.fill(x + 1, y + h - 2, x + 2, y + h - 1, color);
        ctx.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, color);
    }

    private void drawRoundedBorder(DrawContext ctx, int x, int y, int w, int h, int color) {
        ctx.fill(x + 2, y, x + w - 2, y + 1, color);
        ctx.fill(x + 2, y + h - 1, x + w - 2, y + h, color);
        ctx.fill(x, y + 2, x + 1, y + h - 2, color);
        ctx.fill(x + w - 1, y + 2, x + w, y + h - 2, color);
        ctx.fill(x + 1, y + 1, x + 2, y + 2, color);
        ctx.fill(x + w - 2, y + 1, x + w - 1, y + 2, color);
        ctx.fill(x + 1, y + h - 2, x + 2, y + h - 1, color);
        ctx.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, color);
    }

    private void drawRoundedButton(DrawContext context, int x, int y, int width, int height, int color) {
        drawRoundedRect(context, x, y, width, height, color);
    }

    private void drawCenteredText(DrawContext context, String text, int centerX, int y, int color) {
        int width = this.textRenderer.getWidth(text);
        context.drawText(this.textRenderer, text, centerX - width / 2, y, color, false);
    }

    private void renderCardPreview(DrawContext context, int x, int y, SkinCard card, float scaleFactor) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.world == null || client.player == null) {
            return;
        }

        if (previewDummy == null) {
            previewDummy = DummyPlayerEntity.create(client);
        }
        if (previewDummy == null) {
            return;
        }

        ItemStack previewStack = GearSkinManager.getPreviewStack(card.category, card.skinIndex);
        if (previewStack.isEmpty()) {
            return;
        }

        previewDummy.setStackInHand(Hand.MAIN_HAND, previewStack);

        int scissorInset = 1;
        int boxLeft = Math.round((x + scissorInset) * scaleFactor);
        int boxTop = Math.round((y + scissorInset) * scaleFactor);
        int boxRight = Math.round((x + PREVIEW_PANEL_WIDTH - scissorInset) * scaleFactor);
        int boxBottom = Math.round((y + PREVIEW_PANEL_HEIGHT - scissorInset) * scaleFactor);
        float centerX = (boxLeft + boxRight) * 0.5f;
        float centerY = (boxTop + boxBottom) * 0.5f;

        var matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(1.0f / scaleFactor, 1.0f / scaleFactor);
        InventoryScreen.drawEntity(
            context,
            boxLeft,
            boxTop,
            boxRight,
            boxBottom,
            Math.max(1, Math.round(44 * scaleFactor)),
            0.0625f,
            centerX + 138.0f * scaleFactor,
            centerY + 16.0f * scaleFactor,
            previewDummy
        );
        matrices.popMatrix();
    }
}
