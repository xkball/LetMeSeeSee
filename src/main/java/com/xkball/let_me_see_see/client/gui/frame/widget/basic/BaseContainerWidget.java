package com.xkball.let_me_see_see.client.gui.frame.widget.basic;

import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.WidgetBoundary;
import com.xkball.let_me_see_see.client.gui.frame.core.render.CombineRenderer;
import com.xkball.let_me_see_see.client.gui.frame.core.render.IGUIDecoRenderer;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public abstract class BaseContainerWidget extends AbstractContainerWidget implements IPanel {
    
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
    
    public BaseContainerWidget( Component p_313894_) {
        super(0,0,0,0, p_313894_);
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
    protected int contentHeight() {
        return getHeight();
    }
    
    @Override
    protected double scrollRate() {
        return 0;
    }
    
    @Override
    public boolean updateScrolling(double mouseX, double mouseY, int button) {
        this.scrolling = false;
        return false;
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return this.getChildAt(mouseX, mouseY)
                .filter(p_293596_ -> p_293596_.mouseScrolled(mouseX, mouseY, scrollX, scrollY)).isPresent();
    }
}
