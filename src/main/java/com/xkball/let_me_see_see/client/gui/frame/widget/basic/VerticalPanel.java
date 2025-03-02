package com.xkball.let_me_see_see.client.gui.frame.widget.basic;

import com.xkball.let_me_see_see.client.gui.frame.core.HorizontalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.ITypeset;
import com.xkball.let_me_see_see.client.gui.frame.core.VerticalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.WidgetBoundary;
import com.xkball.let_me_see_see.client.gui.frame.core.WidgetPos;
import com.xkball.let_me_see_see.client.gui.frame.core.render.CombineRenderer;
import com.xkball.let_me_see_see.client.gui.frame.core.render.IGUIDecoRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class VerticalPanel extends BaseContainerWidget implements ITypeset {
    public final List<AbstractWidget> children = new ArrayList<>();
    public final List<IPanel> childrenPanels = new ArrayList<>();
    
    public HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;
    public VerticalAlign verticalAlign = VerticalAlign.CENTER;
    
    public static VerticalPanel of(Screen screen) {
        return new VerticalPanel(screen.width, screen.height, screen.getTitle());
    }
    
    public VerticalPanel() {
        this(0, 0, Component.empty());
    }
    
    public VerticalPanel(int width, int height, Component message) {
        super( message);
        this.xPercentage = 1f;
        this.yPercentage = 1f;
        this.widgetBoundary = new WidgetBoundary(new WidgetPos(0, 0, width, height), new WidgetPos(0, 0, width, height));
    }
    
    public <T extends AbstractWidget & IPanel> VerticalPanel addWidget(T widget) {
        return addWidget(widget, false);
    }
    
    public <T extends AbstractWidget & IPanel> VerticalPanel addWidget(T widget, boolean resize) {
        children.add(widget);
        childrenPanels.add(widget);
        if (resize) resize();
        return this;
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public <T extends AbstractWidget & IPanel> VerticalPanel addWidgets(List<T> widgets, boolean resize) {
        children.addAll(widgets);
        childrenPanels.addAll(widgets);
        if (resize) resize();
        return this;
    }
    
    @SuppressWarnings("UnusedReturnValue")
    public <T extends AbstractWidget & IPanel> VerticalPanel addWidgets(Supplier<List<T>> widgets, boolean resize) {
        var list = widgets.get();
        children.addAll(list);
        childrenPanels.addAll(list);
        if (resize) resize();
        return this;
    }
    
    public void clearWidget() {
        children.clear();
        childrenPanels.clear();
        setFocused(null);
    }
    
    @Override
    @SuppressWarnings("DuplicatedCode")
    public void resize() {
        var parentPos = widgetBoundary.inner();
        var y = parentPos.y();
        for (var widget : childrenPanels) {
            IPanel.calculateBoundary(widget, parentPos, parentPos.x(), y);
            y += widget.getBoundary().outer().height();
        }
        var heightSum = y - parentPos.y();
        var shiftY = IPanel.calculateShift(verticalAlign, parentPos.height(), heightSum);
        for (var widget : childrenPanels) {
            var shiftX = IPanel.calculateShift(horizontalAlign, parentPos.width(), widget.getBoundary().outer().width());
            widget.shiftWidgetBoundary(shiftX, shiftY);
            widget.resize();
        }
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDecoration(guiGraphics, mouseX, mouseY, partialTick);
        for (var child : children) {
            child.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
    
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        for (var child : children) {
            child.updateNarration(narrationElementOutput);
        }
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible && this.getBoundary().inner().inside(mouseX, mouseY);
    }
    
    @Override
    public List<IPanel> getChildren() {
        return childrenPanels;
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }
    
    @Override
    public VerticalAlign getVerticalAlign() {
        return verticalAlign;
    }
    
    @Override
    public HorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }
    
    @Override
    public void setVerticalAlign(VerticalAlign verticalAlign) {
        this.verticalAlign = verticalAlign;
    }
    
    @Override
    public void setHorizontalAlign(HorizontalAlign horizontalAlign) {
        this.horizontalAlign = horizontalAlign;
    }
}
