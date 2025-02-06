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
import net.minecraft.client.gui.components.AbstractContainerWidget;
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
public class VerticalPanel extends AbstractContainerWidget implements IPanel, ITypeset {
    public final List<AbstractWidget> children = new ArrayList<>();
    public final List<IPanel> childrenPanels = new ArrayList<>();
    
    public float xPercentage = 1f;
    public float yPercentage = 1f;
    public float leftPadding = 0f;
    public float rightPadding = 0f;
    public float topPadding = 0f;
    public float bottomPadding = 0f;
    
    public int xMax = Integer.MAX_VALUE;
    public int yMax = Integer.MAX_VALUE;
    public int xMin = 0;
    public int yMin = 0;
    
    public WidgetBoundary widgetBoundary;
    public HorizontalAlign horizontalAlign = HorizontalAlign.CENTER;
    public VerticalAlign verticalAlign = VerticalAlign.CENTER;
    @Nullable
    public IGUIDecoRenderer guiDecoRenderer = null;
    
    public static VerticalPanel of(Screen screen) {
        return new VerticalPanel(0, 0, screen.width, screen.height, screen.getTitle());
    }
    
    public VerticalPanel() {
        this(0, 0, 0, 0, Component.empty());
    }
    
    public VerticalPanel(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
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
            IPanel.calculateBoundary(widget,parentPos,parentPos.x(),y);
            y += widget.getBoundary().outer().height();
        }
        var heightSum = y - parentPos.y();
        var shiftY = IPanel.calculateShift(verticalAlign,parentPos.height(),heightSum);
        for (var widget : childrenPanels) {
            var shiftX = IPanel.calculateShift(horizontalAlign,parentPos.width(),widget.getBoundary().outer().width());
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
    public int getX() {
        return getBoundary().inner().x();
    }
    
    @Override
    public int getY() {
        return getBoundary().inner().y();
    }
    
    @Override
    public int getRight() {
        return getBoundary().inner().maxX();
    }
    
    @Override
    public int getBottom() {
        return getBoundary().inner().maxY();
    }
    
    @Override
    public float getXPercentage() {
        return xPercentage;
    }
    
    @Override
    public float getYPercentage() {
        return yPercentage;
    }
    
    @Override
    public float getLeftPadding() {
        return leftPadding;
    }
    
    @Override
    public float getRightPadding() {
        return rightPadding;
    }
    
    @Override
    public float getTopPadding() {
        return topPadding;
    }
    
    @Override
    public float getBottomPadding() {
        return bottomPadding;
    }
    
    @Override
    public int getXMax() {
        return xMax;
    }
    
    @Override
    public int getYMax() {
        return yMax;
    }
    
    @Override
    public int getXMin() {
        return xMin;
    }
    
    @Override
    public int getYMin() {
        return yMin;
    }
    
    @Override
    public WidgetBoundary getBoundary() {
        return widgetBoundary;
    }
    
    @Nullable
    @Override
    public IGUIDecoRenderer getDecoRenderer() {
        return guiDecoRenderer;
    }
    
    @Override
    public boolean getIsFocused() {
        return isFocused();
    }
    
    @Override
    public void setXPercentage(float percentage) {
        this.xPercentage = percentage;
    }
    
    @Override
    public void setYPercentage(float percentage) {
        this.yPercentage = percentage;
    }
    
    @Override
    public void setLeftPadding(float percentage) {
        this.leftPadding = percentage;
    }
    
    @Override
    public void setRightPadding(float percentage) {
        this.rightPadding = percentage;
    }
    
    @Override
    public void setTopPadding(float percentage) {
        this.topPadding = percentage;
    }
    
    @Override
    public void setBottomPadding(float percentage) {
        this.bottomPadding = percentage;
    }
    
    @Override
    public void setXMax(int max) {
        this.xMax = max;
    }
    
    @Override
    public void setYMax(int max) {
        this.yMax = max;
    }
    
    @Override
    public void setXMin(int min) {
        this.xMin = min;
    }
    
    @Override
    public void setYMin(int min) {
        this.yMin = min;
    }
    
    @Override
    public void setBoundary(WidgetBoundary boundary) {
        this.widgetBoundary = boundary;
        this.width = boundary.inner().width();
        this.height = boundary.inner().height();
    }
    
    @Override
    public void setDecoRenderer(IGUIDecoRenderer decoRenderer) {
        if (this.guiDecoRenderer == null) {
            this.guiDecoRenderer = decoRenderer;
        } else {
            this.guiDecoRenderer = new CombineRenderer(guiDecoRenderer, decoRenderer);
        }
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
