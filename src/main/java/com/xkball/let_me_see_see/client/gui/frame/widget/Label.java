package com.xkball.let_me_see_see.client.gui.frame.widget;

import com.xkball.let_me_see_see.client.gui.frame.core.IUpdateMarker;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.AutoResizeWidget;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class Label extends AutoResizeWidget {
    
    private float scale;
    public int color = -1;
    public boolean dropShadow = true;
    private int length;
    
    public static Label of(String str) {
        return of(Component.literal(str));
    }
    
    public static Label of(Component message) {
        return of(message, 1);
    }
    
    public static Label of(Supplier<Component> message) {
        return of(message, 1);
    }
    
    
    public static Label of(Component message, float scale) {
        return new Label(message, scale, -1, true);
    }
    
    public static Label of(Supplier<Component> message, float scale) {
        return new DynamicLabel(message, scale, -1, true);
    }
    
    public Label(Component message, float scale, int color, boolean dropShadow) {
        super(message);
        this.setScale(scale);
        this.color = color;
        this.dropShadow = dropShadow;
    }
    
    @Override
    protected boolean isValidClickButton(int button) {
        return false;
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, 1);
        var boundary = getBoundary().inner();
        var font = Minecraft.getInstance().font;
        if (boundary.width() < length) {
            renderScrollingString(guiGraphics, font, getMessage(), boundary.x(), boundary.y(), boundary.maxX(), boundary.maxY(), color, dropShadow);
        } else {
            guiGraphics.drawString(font, getMessage().getVisualOrderText(), boundary.x() / scale, boundary.y() / scale, color, dropShadow);
        }
        guiGraphics.pose().popPose();
    }
    
    public static void renderScrollingString(
            GuiGraphics guiGraphics, Font font, Component text, int minX, int minY, int maxX, int maxY, int color, boolean dropShadow
    ) {
        renderScrollingString(guiGraphics, font, text, (minX + maxX) / 2, minX, minY, maxX, maxY, color, dropShadow);
    }
    
    public static void renderScrollingString(
            GuiGraphics guiGraphics, Font font, Component text, int centerX, int minX, int minY, int maxX, int maxY, int color, boolean dropShadow
    ) {
        int i = font.width(text);
        int j = (minY + maxY - 9) / 2 + 1;
        int k = maxX - minX;
        if (i > k) {
            int l = i - k;
            double d0 = (double) Util.getMillis() / 1000.0;
            double d1 = Math.max((double) l * 0.5, 3.0);
            double d2 = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * d0 / d1)) / 2.0 + 0.5;
            double d3 = Mth.lerp(d2, 0.0, l);
            guiGraphics.enableScissor(minX, 0, maxX, Integer.MAX_VALUE);
            guiGraphics.drawString(font, text, minX - (int) d3, j, color);
            guiGraphics.disableScissor();
        } else {
            int i1 = Mth.clamp(centerX, minX + i / 2, maxX - i / 2);
            guiGraphics.drawCenteredString(font, text, i1, j, color);
        }
    }
    
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, getMessage());
    }
    
    @Override
    public void setMessage(Component message) {
        super.setMessage(message);
        this.setScale(scale);
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
    
    public float getScale() {
        return scale;
    }
    
    //修改后需要父组件resize
    public void setScale(float scale) {
        this.scale = scale;
        var font = Minecraft.getInstance().font;
        this.length = font.width(getMessage());
        this.setFixWidth((int) (length * scale));
        this.setYMin((int) (font.lineHeight * scale));
    }
    
    @Override
    public void trim() {
        var font = Minecraft.getInstance().font;
        this.length = font.width(getMessage());
        this.setFixWidth((int) (length * scale));
        this.setFixHeight((int) (font.lineHeight * scale));
    }
    
    public static class DynamicLabel extends Label {
        
        private final Supplier<Component> textSupplier;
        
        public DynamicLabel(Supplier<Component> textSupplier, float scale, int color, boolean dropShadow) {
            super(textSupplier.get(), scale, color, dropShadow);
            this.textSupplier = textSupplier;
        }
        
        @Override
        public boolean update(IUpdateMarker marker) {
            var newText = textSupplier.get();
            if (getMessage().equals(newText)) return false;
            this.setMessage(newText);
            return true;
        }
    }
}
