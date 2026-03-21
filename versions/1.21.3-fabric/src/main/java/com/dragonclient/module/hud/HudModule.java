package com.dragonclient.module.hud;

import com.dragonclient.module.Module;
import com.dragonclient.module.ModuleCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class HudModule extends Module {
    protected static final float DEFAULT_SCALE = 2.0f;
    protected static final int PANEL_PADDING_X = 8;
    protected static final int PANEL_PADDING_Y = 6;

    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean dragging;
    protected boolean useDefaultPosition = true;
    protected boolean useDefaultScale = true;
    protected float scale = DEFAULT_SCALE;

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
        this.useDefaultPosition = false;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
        this.useDefaultPosition = false;
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

    public boolean hasCustomPosition() {
        return !useDefaultPosition;
    }

    public void setUseDefaultPosition(boolean useDefaultPosition) {
        this.useDefaultPosition = useDefaultPosition;
    }

    public boolean hasCustomScale() {
        return !useDefaultScale;
    }

    public void setUseDefaultScale(boolean useDefaultScale) {
        this.useDefaultScale = useDefaultScale;
        if (useDefaultScale) {
            this.scale = DEFAULT_SCALE;
        }
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.useDefaultScale = false;
        this.scale = Math.max(1.0f, Math.min(6.0f, scale));
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    
    // Helper method to apply scaling transformation
    protected void applyScale(DrawContext context) {
        float currentScale = getScale();
        var matrices = context.getMatrices();
        matrices.push();
        matrices.translate((float) x, (float) y, 0f);
        matrices.scale(currentScale, currentScale, 1.0f);
        matrices.translate((float) -x, (float) -y, 0f);
    }
    
    // Helper method to remove scaling transformation
    protected void removeScale(DrawContext context) {
        context.getMatrices().pop();
    }

    protected void applyDefaultTopRight(MinecraftClient client, int contentWidth, int topMargin) {
        if (!useDefaultPosition || client == null || client.getWindow() == null) {
            return;
        }
        int scaledWidth = Math.round(contentWidth * getScale());
        this.x = client.getWindow().getScaledWidth() - scaledWidth - 16;
        this.y = topMargin;
    }

    protected void applyDefaultTopLeft(MinecraftClient client, int leftMargin, int topMargin) {
        if (!useDefaultPosition || client == null || client.getWindow() == null) {
            return;
        }
        this.x = leftMargin;
        this.y = topMargin;
    }

    protected void applyDefaultBottomRight(
        MinecraftClient client,
        int contentWidth,
        int contentHeight,
        int rightMargin,
        int bottomMargin
    ) {
        if (!useDefaultPosition || client == null || client.getWindow() == null) {
            return;
        }
        int scaledWidth = Math.round(contentWidth * getScale());
        int scaledHeight = Math.round(contentHeight * getScale());
        this.x = client.getWindow().getScaledWidth() - scaledWidth - rightMargin;
        this.y = client.getWindow().getScaledHeight() - scaledHeight - bottomMargin;
    }

    protected void drawLiquidGlassTextPanel(DrawContext context, int textWidth, int textHeight) {
        drawLiquidGlassPanel(
            context,
            x - PANEL_PADDING_X,
            y - PANEL_PADDING_Y,
            x + textWidth + PANEL_PADDING_X,
            y + textHeight + PANEL_PADDING_Y,
            false
        );
    }

    protected void drawLiquidGlassPanel(
        DrawContext context,
        int left,
        int top,
        int right,
        int bottom,
        boolean pressed
    ) {
        int width = right - left;
        int height = bottom - top;
        if (width <= 2 || height <= 2) {
            return;
        }

        // Full capsule radius for the cleanest rounded ends.
        int radius = Math.max(2, Math.min(width / 2, height / 2));
        int panelColor = pressed ? 0x78000000 : 0x60000000;

        // Single fill avoids side color mismatch from offset shadows.
        fillRoundedRect(context, left, top, right, bottom, radius, panelColor);
    }

    private static void fillRoundedRect(
        DrawContext context,
        int left,
        int top,
        int right,
        int bottom,
        int radius,
        int color
    ) {
        int width = right - left;
        int height = bottom - top;
        if (width <= 0 || height <= 0) {
            return;
        }

        int r = Math.max(1, Math.min(radius, Math.min(width, height) / 2));
        if (r <= 1) {
            context.fill(left, top, right, bottom, color);
            return;
        }

        int alpha = (color >>> 24) & 0xFF;
        int rgb = color & 0x00FFFFFF;

        // Single-pass scanline fill with a subtle 1px feather on curved rows.
        // This reduces visible stair-steps at 2x HUD scale.
        for (int y = 0; y < height; y++) {
            double insetExact = cornerInsetExactForRow(r, y, height);
            int inset = (int) Math.ceil(insetExact);

            int x1 = left + inset;
            int x2 = right - inset;
            if (x2 > x1) {
                context.fill(x1, top + y, x2, top + y + 1, color);
            }

            // Feather outer edge on curved rows for smoother capsule corners.
            if (alpha > 0 && inset > 0 && (y < r || y >= height - r)) {
                double edgeCoverage = Math.max(0.0d, Math.min(1.0d, inset - insetExact));
                double smoothedCoverage =
                    edgeCoverage * edgeCoverage * (3.0d - (2.0d * edgeCoverage));

                int edgeAlpha1 = (int) Math.round(alpha * smoothedCoverage);
                if (edgeAlpha1 > 0) {
                    int edgeColor1 = (edgeAlpha1 << 24) | rgb;
                    context.fill(x1 - 1, top + y, x1, top + y + 1, edgeColor1);
                    context.fill(x2, top + y, x2 + 1, top + y + 1, edgeColor1);
                }

                int edgeAlpha2 = (int) Math.round(alpha * smoothedCoverage * 0.35d);
                if (edgeAlpha2 > 0 && inset > 1) {
                    int edgeColor2 = (edgeAlpha2 << 24) | rgb;
                    context.fill(x1 - 2, top + y, x1 - 1, top + y + 1, edgeColor2);
                    context.fill(x2 + 1, top + y, x2 + 2, top + y + 1, edgeColor2);
                }
            }
        }
    }

    private static void drawRoundedOutline(
        DrawContext context,
        int left,
        int top,
        int right,
        int bottom,
        int radius,
        int color
    ) {
        int width = right - left;
        int height = bottom - top;
        if (width <= 1 || height <= 1) {
            return;
        }

        int r = Math.max(1, Math.min(radius, Math.min(width, height) / 2));
        int topInset = cornerInsetForRow(r, 0);

        for (int row = 0; row < r; row++) {
            int inset = cornerInsetForRow(r, row);
            int topRow = top + row;
            int bottomRow = bottom - row - 1;

            context.fill(left + inset, topRow, left + inset + 1, topRow + 1, color);
            context.fill(right - inset - 1, topRow, right - inset, topRow + 1, color);
            context.fill(left + inset, bottomRow, left + inset + 1, bottomRow + 1, color);
            context.fill(right - inset - 1, bottomRow, right - inset, bottomRow + 1, color);
        }

        context.fill(left + topInset, top, right - topInset, top + 1, color);
        context.fill(left + topInset, bottom - 1, right - topInset, bottom, color);
        context.fill(left, top + r, left + 1, bottom - r, color);
        context.fill(right - 1, top + r, right, bottom - r, color);
    }

    private static int cornerInsetForRow(int radius, int row) {
        if (radius <= 1) {
            return 0;
        }

        double dy = radius - row - 0.5d;
        double dx = Math.sqrt(Math.max(0.0d, (radius * radius) - (dy * dy)));
        return Math.max(0, radius - (int) Math.ceil(dx));
    }

    private static double cornerInsetExactForRow(int radius, int row, int height) {
        if (radius <= 1) {
            return 0.0d;
        }

        int cornerRow = -1;
        if (row < radius) {
            cornerRow = row;
        } else {
            int fromBottom = height - 1 - row;
            if (fromBottom < radius) {
                cornerRow = fromBottom;
            }
        }

        if (cornerRow < 0) {
            return 0.0d;
        }

        double dy = radius - cornerRow - 0.5d;
        double dx = Math.sqrt(Math.max(0.0d, (radius * radius) - (dy * dy)));
        return Math.max(0.0d, radius - dx);
    }

}
