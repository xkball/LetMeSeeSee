package com.xkball.let_me_see_see.client.gui.frame.core;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public record WidgetPos(int x, int y, int width, int height) {
    
    public int maxX() {
        return x + width;
    }
    
    public int maxY() {
        return y + height;
    }
    
    public boolean inside(int px, int py) {
        return px >= x && px <= maxX() && py >= y && py <= maxY();
    }
    
    public boolean inside(double px, double py) {
        return px >= x && px <= maxX() && py >= y && py <= maxY();
    }
}
