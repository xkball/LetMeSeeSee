package com.xkball.let_me_see_see.client.gui.frame.widget.basic;

import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.WidgetBoundary;
import com.xkball.let_me_see_see.client.gui.frame.core.render.CombineRenderer;
import com.xkball.let_me_see_see.client.gui.frame.core.render.IGUIDecoRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class AutoResizeWidgetWrapper extends AbstractContainerWidget implements IPanel {
    
    protected final AbstractWidget inner;
    private final List<? extends GuiEventListener> child;
    
    public float xPercentage = 0f;
    public float yPercentage = 0f;
    public float leftPadding = 0f;
    public float rightPadding = 0f;
    public float topPadding = 0f;
    public float bottomPadding = 0f;
    
    public int xMax = Integer.MAX_VALUE;
    public int yMax = Integer.MAX_VALUE;
    public int xMin = 0;
    public int yMin = 0;
    
    public WidgetBoundary widgetBoundary = WidgetBoundary.DEFAULT;
    @Nullable
    public IGUIDecoRenderer guiDecoRenderer = null;
    
    public static AutoResizeWidgetWrapper of(AbstractWidget inner) {
        return new AutoResizeWidgetWrapper(inner);
    }
    
    public AutoResizeWidgetWrapper(AbstractWidget inner) {
        super(inner.getX(), inner.getY(), inner.getWidth(), inner.getHeight(), inner.getMessage());
        this.inner = inner;
        this.child = List.of(inner);
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDecoration(guiGraphics, mouseX, mouseY, partialTick);
        inner.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        inner.updateNarration(narrationElementOutput);
    }
    
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return this.active && this.visible && this.getBoundary().inner().inside(mouseX, mouseY);
    }
    
    @Override
    @SuppressWarnings("DuplicatedCode")
    public void resize() {
        if (inner instanceof IPanel widget) {
            var parentPos = widgetBoundary.inner();
            IPanel.calculateBoundary(widget, parentPos, parentPos.x(), parentPos.y());
            widget.resize();
        }
    }
    
    @Override
    public List<IPanel> getChildren() {
        if (inner instanceof IPanel widget) {
            return List.of(widget);
        }
        return EMPTY;
    }
    
    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        inner.setFocused(focused);
        
    }
    
    @Override
    public void setTooltip(@Nullable Tooltip tooltip) {
        inner.setTooltip(tooltip);
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return child;
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
        this.inner.setPosition(boundary.inner().x(), boundary.inner().y());
        this.inner.setSize(boundary.inner().width(), boundary.inner().height());
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
}
