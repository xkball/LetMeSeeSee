package com.xkball.let_me_see_see.client.gui.frame.widget.basic;

import com.xkball.let_me_see_see.client.gui.frame.core.HorizontalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.WidgetPos;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClampHorizontalPanel extends HorizontalPanel {
    
    @Nullable
    public IPanel left_;
    @Nullable
    public IPanel right_;
    public WidgetPos innerPos = new WidgetPos(0, 0, 0, 0);
    private final List<IPanel> actuallyPanels = new ArrayList<>();
    
    public ClampHorizontalPanel() {
    }
    
    @Override
    public void resize() {
        var xl = 0;
        var xr = 0;
        var parentPos = getBoundary().inner();
        var x = parentPos.x();
        var y = parentPos.y();
        if (left_ != null) {
            IPanel.calculateBoundary(left_, parentPos, x, y);
            var shiftY = IPanel.calculateShift(verticalAlign, parentPos.height(), left_.getBoundary().outer().height());
            left_.shiftWidgetBoundary(0, shiftY);
            xl = left_.getBoundary().outer().width();
        }
        if (right_ != null) {
            IPanel.calculateBoundary(right_, parentPos, x, y);
            var shiftX = IPanel.calculateShift(HorizontalAlign.RIGHT, parentPos.width(), right_.getBoundary().outer().width());
            var shiftY = IPanel.calculateShift(verticalAlign, parentPos.height(), right_.getBoundary().outer().height());
            right_.shiftWidgetBoundary(shiftX, shiftY);
            xr = right_.getBoundary().outer().width();
        }
        this.innerPos = new WidgetPos(x + xl, y, parentPos.width() - xr - xl, parentPos.height());
        super.resize();
    }
    
    @Override
    public WidgetPos getInnerPos() {
        return innerPos;
    }
    
    @Override
    public void clearWidget() {
        super.clearWidget();
        actuallyPanels.clear();
        this.left_ = null;
        this.right_ = null;
    }
    
    @Override
    public List<IPanel> getChildren() {
        return actuallyPanels;
    }
    
    @Override
    public <T extends AbstractWidget & IPanel> HorizontalPanel addWidget(T wrapper, boolean resize) {
        this.actuallyPanels.add(wrapper);
        return super.addWidget(wrapper, resize);
    }
    
    @Override
    public <T extends AbstractWidget & IPanel> HorizontalPanel addWidgets(Supplier<List<T>> widgets, boolean resize) {
        this.actuallyPanels.addAll(widgets.get());
        return super.addWidgets(widgets, resize);
    }
    
    public <T extends AbstractWidget & IPanel> ClampHorizontalPanel setLeft(T left) {
        this.children.add(left);
        this.actuallyPanels.add(left);
        this.left_ = left;
        return this;
    }
    
    public <T extends AbstractWidget & IPanel> ClampHorizontalPanel setRight(T right) {
        this.children.add(right);
        this.actuallyPanels.add(right);
        this.right_ = right;
        return this;
    }
}
