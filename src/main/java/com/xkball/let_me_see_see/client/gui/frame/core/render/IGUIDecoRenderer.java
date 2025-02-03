package com.xkball.let_me_see_see.client.gui.frame.core.render;

import com.xkball.let_me_see_see.client.gui.frame.core.WidgetBoundary;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IGUIDecoRenderer {
    
    void render(GuiGraphics guiGraphics, WidgetBoundary boundary, int mouseX, int mouseY, float partialTick);
}
