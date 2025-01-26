package com.xkball.let_me_see_see.client.gui.frame.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.util.List;

public class AutoResizeWidgetWrapper extends AbstractContainerWidget {
    
    private final AbstractWidget inner;
    private final List<? extends GuiEventListener> child;
    
    //均使用0-1表示占用上级空间的比例
    //小于0则表示不参与计算
    public float xPercentage = -1f;
    public float xPadding = -1f;
    public float yPercentage = -1f;
    public float yPadding = -1f;
    
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
        inner.render(guiGraphics, mouseX, mouseY, partialTick);
    }
    
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        inner.updateNarration(narrationElementOutput);
    }
    
    
    @Override
    public List<? extends GuiEventListener> children() {
        return child;
    }
}
