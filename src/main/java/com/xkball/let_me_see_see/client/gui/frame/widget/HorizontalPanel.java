package com.xkball.let_me_see_see.client.gui.frame.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class HorizontalPanel extends AbstractContainerWidget {
    
    public List<AutoResizeWidgetWrapper> children = new ArrayList<>();
    
    public HorizontalPanel(int x, int y, int width, int height, Component message) {
        super(x, y, width, height, message);
    }
    
    public void addWidget(AutoResizeWidgetWrapper wrapper) {
        children.add(wrapper);
        resize();
    }
    
    public void resize(){
    
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for(var child : children) {
            child.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }
    
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        for(var child : children) {
            child.updateNarration(narrationElementOutput);
        }
    }
    
    @Override
    public List<? extends GuiEventListener> children() {
        return children;
    }
}
