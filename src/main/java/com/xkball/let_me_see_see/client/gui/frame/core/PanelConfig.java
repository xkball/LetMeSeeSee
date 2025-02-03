package com.xkball.let_me_see_see.client.gui.frame.core;

import com.xkball.let_me_see_see.client.gui.frame.core.render.CombineRenderer;
import com.xkball.let_me_see_see.client.gui.frame.core.render.IGUIDecoRenderer;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class PanelConfig {
    
    public float xPercentage = -1f;
    public float yPercentage = -1f;
    public float leftPadding = -1f;
    public float rightPadding = -1f;
    public float topPadding = -1f;
    public float bottomPadding = -1f;
    
    public int xMax = -1;
    public int yMax = -1;
    public int xMin = -1;
    public int yMin = -1;
    
    @Nullable
    public HorizontalAlign horizontalAlign = null;
    @Nullable
    public VerticalAlign verticalAlign = null;
    @Nullable
    public IGUIDecoRenderer guiDecoRenderer = null;
    @Nullable
    public Tooltip tooltip = null;
    
    public boolean trim = false;
    
    public static PanelConfig of() {
        return new PanelConfig();
    }
    
    public static PanelConfig of(float xPercentage, float yPercentage) {
        var result = new PanelConfig();
        result.xPercentage = xPercentage;
        result.yPercentage = yPercentage;
        return result;
    }
    
    public PanelConfig tooltip(String trans_key) {
        this.tooltip = Tooltip.create(Component.translatable(trans_key));
        return this;
    }
    
    public PanelConfig tooltip(Tooltip tooltip) {
        this.tooltip = tooltip;
        return this;
    }
    
    public PanelConfig fixSize(int width, int height) {
        fixWidth(width);
        fixHeight(height);
        return this;
    }
    
    public PanelConfig fixWidth(int width) {
        this.xMax = width;
        this.xMin = width;
        return this;
    }
    
    public PanelConfig fixHeight(int height) {
        this.yMax = height;
        this.yMin = height;
        return this;
    }
    
    public PanelConfig paddingLeft(float leftPadding) {
        this.leftPadding = leftPadding;
        return this;
    }
    
    public PanelConfig paddingRight(float rightPadding) {
        this.rightPadding = rightPadding;
        return this;
    }
    
    public PanelConfig paddingTop(float topPadding) {
        this.topPadding = topPadding;
        return this;
    }
    
    public PanelConfig paddingBottom(float bottomPadding) {
        this.bottomPadding = bottomPadding;
        return this;
    }
    
    public PanelConfig padding(float leftPadding, float rightPadding, float topPadding, float bottomPadding) {
        this.leftPadding = leftPadding;
        this.rightPadding = rightPadding;
        this.topPadding = topPadding;
        this.bottomPadding = bottomPadding;
        return this;
    }
    
    public PanelConfig sizeLimitXMin(int xMin) {
        this.xMin = xMin;
        return this;
    }
    
    public PanelConfig sizeLimitYMin(int yMin) {
        this.yMin = yMin;
        return this;
    }
    
    public PanelConfig sizeLimitXMax(int xMax) {
        this.xMax = xMax;
        return this;
    }
    
    public PanelConfig sizeLimitYMax(int yMax) {
        this.yMax = yMax;
        return this;
    }
    
    public PanelConfig sizeLimit(int xMin, int yMin, int xMax, int yMax) {
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        return this;
    }
    
    public PanelConfig trim() {
        this.trim = true;
        return this;
    }
    
    public PanelConfig align(HorizontalAlign horizontalAlign, VerticalAlign verticalAlign) {
        this.horizontalAlign = horizontalAlign;
        this.verticalAlign = verticalAlign;
        return this;
    }
    
    public PanelConfig decoRenderer(IGUIDecoRenderer decoRenderer) {
        if (this.guiDecoRenderer == null) {
            this.guiDecoRenderer = decoRenderer;
        } else {
            this.guiDecoRenderer = new CombineRenderer(guiDecoRenderer, decoRenderer);
        }
        return this;
    }
    
    public <T extends IPanel> T apply(T panel) {
        if (trim) panel.trim();
        if (xPercentage > 0) panel.setXPercentage(xPercentage);
        if (yPercentage > 0) panel.setYPercentage(yPercentage);
        if (leftPadding > 0) panel.setLeftPadding(leftPadding);
        if (rightPadding > 0) panel.setRightPadding(rightPadding);
        if (topPadding > 0) panel.setTopPadding(topPadding);
        if (bottomPadding > 0) panel.setBottomPadding(bottomPadding);
        if (xMax > 0) panel.setXMax(xMax);
        if (yMax > 0) panel.setYMax(yMax);
        if (xMin > 0) panel.setXMin(xMin);
        if (yMin > 0) panel.setYMin(yMin);
        if (guiDecoRenderer != null) panel.setDecoRenderer(guiDecoRenderer);
        if (panel instanceof ITypeset t) {
            if (verticalAlign != null) t.setVerticalAlign(verticalAlign);
            if (horizontalAlign != null) t.setHorizontalAlign(horizontalAlign);
        }
        if (panel instanceof AbstractWidget widget && tooltip != null) {
            widget.setTooltip(tooltip);
        }
        return panel;
    }
}
