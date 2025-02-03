package com.xkball.let_me_see_see.client.gui.frame.core.render;

import com.xkball.let_me_see_see.client.gui.frame.core.WidgetBoundary;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class SimpleBackgroundRenderer implements IGUIDecoRenderer {
    
    public static SimpleBackgroundRenderer GRAY = new SimpleBackgroundRenderer(VanillaUtils.GUI_GRAY);
    private final int bgColor;
    
    public SimpleBackgroundRenderer(int bgColor) {
        this.bgColor = bgColor;
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, WidgetBoundary boundary, int mouseX, int mouseY, float partialTick) {
        var outer = boundary.outer();
        guiGraphics.fill(outer.x(), outer.y(), outer.maxX(), outer.maxY() + 1, bgColor);
    }
}
