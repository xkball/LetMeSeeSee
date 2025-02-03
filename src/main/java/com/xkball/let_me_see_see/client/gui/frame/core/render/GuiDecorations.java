package com.xkball.let_me_see_see.client.gui.frame.core.render;

import com.xkball.let_me_see_see.utils.VanillaUtils;
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
}
