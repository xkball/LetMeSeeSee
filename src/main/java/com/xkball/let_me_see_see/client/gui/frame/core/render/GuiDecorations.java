package com.xkball.let_me_see_see.client.gui.frame.core.render;

import com.xkball.let_me_see_see.utils.VanillaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiDecorations {
    
    public static final IGUIDecoRenderer BOTTOM_DARK_BORDER_LINE = (guiGraphics, boundary, mouseX, mouseY, partialTick) -> {
        guiGraphics.hLine(boundary.outer().x(), boundary.outer().maxX(), boundary.outer().maxY() - 1, VanillaUtils.getColor(240, 240, 240, 255));
        guiGraphics.hLine(boundary.outer().x(), boundary.outer().maxX(), boundary.outer().maxY(), VanillaUtils.getColor(20, 20, 20, 255));
    };
    
    public static final IGUIDecoRenderer RIGHT_DARK_BORDER_LINE = (guiGraphics, boundary, mouseX, mouseY, partialTick) -> {
        guiGraphics.vLine(boundary.outer().maxX() - 1, boundary.outer().y(), boundary.outer().maxY(), VanillaUtils.getColor(240, 240, 240, 255));
        guiGraphics.vLine(boundary.outer().maxX(), boundary.outer().y(), boundary.outer().maxY(), VanillaUtils.getColor(20, 20, 20, 255));
    };
    
    public static final IGUIDecoRenderer WHITE_BORDER = (guiGraphics, boundary, mouseX, mouseY, partialTick) ->
            guiGraphics.renderOutline(boundary.inner().x(), boundary.inner().y(), boundary.inner().width(), boundary.inner().height(), -1);
    
    public static final IGUIDecoRenderer GRAY_BORDER = (guiGraphics, boundary, mouseX, mouseY, partialTick) ->
            guiGraphics.renderOutline(boundary.inner().x(), boundary.inner().y(), boundary.inner().width(), boundary.inner().height(), VanillaUtils.getColor(160, 160, 160, 200));
    
    private static void drawString(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color, boolean dropShadow, float scale) {
        if (scale != 1) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().scale(scale, scale, 1);
            guiGraphics.drawString(font, text.getVisualOrderText(), x / scale, y / scale, color, dropShadow);
            guiGraphics.pose().popPose();
        } else {
            guiGraphics.drawString(font, text, x, y, color, dropShadow);
        }
    }
    
    public static IGUIDecoRenderer leftCenterString(Component text) {
        return leftCenterString(text, -1, true, 1);
    }
    
    public static IGUIDecoRenderer leftCenterString(Component text, int color, boolean dropShadow, float scale) {
        return (guiGraphics, boundary, mouseX, mouseY, partialTick) -> {
            var font = Minecraft.getInstance().font;
            var length = font.width(text);
            var x = boundary.inner().x() - length - 8;
            var y = boundary.inner().y() + boundary.inner().height() / 2 - 4;
            drawString(guiGraphics, font, text, x, y, color, dropShadow, scale);
        };
    }
    
    public static IGUIDecoRenderer bottomLeftString(Component text) {
        return bottomLeftString(text, -1, true, 1);
    }
    
    public static IGUIDecoRenderer bottomLeftString(Component text, int color, boolean dropShadow, float scale) {
        return (guiGraphics, boundary, mouseX, mouseY, partialTick) -> {
            var font = Minecraft.getInstance().font;
            drawString(guiGraphics, font, text, boundary.inner().x(), boundary.inner().maxY() + 1, color, dropShadow, scale);
        };
    }
    
    public static IGUIDecoRenderer bottomCenterString(Component text) {
        return bottomCenterString(text, -1, true, 1);
    }
    
    public static IGUIDecoRenderer bottomCenterString(Component text, int color, boolean dropShadow, float scale) {
        return (guiGraphics, boundary, mouseX, mouseY, partialTick) -> {
            var font = Minecraft.getInstance().font;
            var length = font.width(text);
            var x = boundary.inner().x() + boundary.inner().width() / 2 - length / 2;
            drawString(guiGraphics, font, text, x, boundary.inner().maxY() + 1, color, dropShadow, scale);
        };
    }
}
