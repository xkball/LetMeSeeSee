package com.xkball.let_me_see_see.client.gui.frame.widget;

import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.WidgetBoundary;
import com.xkball.let_me_see_see.client.gui.frame.core.WidgetPos;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.AutoResizeWidgetWrapper;
import net.minecraft.client.gui.components.AbstractWidget;

public class SquareWidgetWrapper extends AutoResizeWidgetWrapper {
    
    public SquareWidgetWrapper(AbstractWidget inner) {
        super(inner);
    }
    
    @Override
    public void resize() {
        super.resize();
        if(!(inner instanceof IPanel panel)) return;
        var in = panel.getBoundary().inner();
        var w = in.width();
        var h = in.height();
        var size = Math.min(w,h);
        var shift = Math.abs(w-h)/2f;
        var shiftX = w > h ? shift : 0;
        var shiftY = h > w ? shift : 0;
        panel.setBoundary(new WidgetBoundary(panel.getBoundary().outer(),new WidgetPos((int) (in.x()+shiftX), (int) (in.y()+shiftY),size,size)));
    }
}
