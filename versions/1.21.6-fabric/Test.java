package com.dragonclient.gui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class Test {
    public void test(DrawContext context) {
        MatrixStack stack = context.getMatrices();
        stack.push();
    }
}
