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
public class AutoResizeWidgetWrapper extends BaseContainerWidget {
    
    protected final AbstractWidget inner;
    private final List<? extends GuiEventListener> child;
    
    public static AutoResizeWidgetWrapper of(AbstractWidget inner) {
        return new AutoResizeWidgetWrapper(inner);
    }
    
    public AutoResizeWidgetWrapper(AbstractWidget inner) {
        super(inner.getMessage());
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
    public void setBoundary(WidgetBoundary boundary) {
        this.widgetBoundary = boundary;
        this.inner.setPosition(boundary.inner().x(), boundary.inner().y());
        this.inner.setSize(boundary.inner().width(), boundary.inner().height());
        this.width = boundary.inner().width();
        this.height = boundary.inner().height();
    }
}
