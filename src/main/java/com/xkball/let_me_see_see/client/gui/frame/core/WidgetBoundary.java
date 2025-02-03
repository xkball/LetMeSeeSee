package com.xkball.let_me_see_see.client.gui.frame.core;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record WidgetBoundary(WidgetPos outer, WidgetPos inner) {
    public static final WidgetBoundary DEFAULT = new WidgetBoundary(new WidgetPos(0, 0, 0, 0), new WidgetPos(0, 0, 0, 0));
    
}
