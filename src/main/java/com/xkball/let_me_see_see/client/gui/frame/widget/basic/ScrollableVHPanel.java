package com.xkball.let_me_see_see.client.gui.frame.widget.basic;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

public class ScrollableVHPanel extends ScrollableVerticalPanel{
    
    public int maxScrollH = 0;
    public int maxPositionH = 0;
    public double scrollAmountH = 0;
    public boolean scrollingH = false;
    
    @Override
    public void resize() {
        super.resize();
        this.maxPositionH = 0;
        for(var widget : childrenPanels){
            this.maxPositionH = Math.max(this.maxPositionH, widget.getBoundary().outer().width());
        }
        this.maxScrollH = Math.max(0, this.maxPositionH - this.getBoundary().inner().width() + 6);
        
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        if(this.scrollbarVisibleH()){
            int l = this.getScrollbarPositionH();
            var w = getBoundary().inner().width();
            var x = getBoundary().inner().x();
            int i1 = (int) ((float) (w * w) / (float) this.maxPositionH);
            i1 = Mth.clamp(i1, 32, w - 8);
            int k = (int) this.scrollAmountH * (w - i1) / this.maxScrollH + x;
            if (k < this.getX()) {
                k = this.getX();
            }
            
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,SCROLLER_BACKGROUND_SPRITE, x, l, w, 6);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,SCROLLER_SPRITE, k, l, i1, 6);
            
        }
    }
    
    @Override
    public void clampScrollAmount() {
        super.clampScrollAmount();
        this.setClampedScrollAmountH(this.scrollAmountH);
    }
    
    @Override
    public void translateGUIMatrix(GuiGraphics guiGraphics) {
        guiGraphics.pose().translate((float) -scrollAmountH,(float) -scrollAmount);
    }
    
    @Override
    public Vector2f getActualMousePos(float mouseX, float mouseY) {
        return new Vector2f((float) (mouseX + scrollAmountH), (float) (mouseY + scrollAmount));
    }
    
    @Override
    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        super.updateScrollingState(mouseX, mouseY, button);
        this.scrollingH = button == 0 && mouseY > this.getScrollbarPositionH() && mouseY < this.getScrollbarPositionH() + 6;
    }
    
    @Override
    protected boolean anyScrolling() {
        return super.anyScrolling() || this.scrollingH;
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        var result = super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        if(result) return true;
        else if (button == 0 && this.scrollingH) {
            if(mouseX < this.getBoundary().inner().x()){
                this.setClampedScrollAmountH(0.0);
            }
            else if (mouseX > this.getBoundary().inner().maxX()){
                this.setClampedScrollAmountH(this.maxScrollH);
            }
            else{
                double d0 = Math.max(1, this.maxScrollH);
                int i = this.getBoundary().inner().width();
                int j = Mth.clamp((int) ((float) (i * i) / (float) this.maxPositionH), 32, i - 8);
                double d1 = Math.max(1.0, d0 / (double) (i - j));
                this.setClampedScrollAmountH(this.scrollAmountH + dragX * d1);
            }
            return true;
        }
        else {
            return false;
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(),InputConstants.KEY_LSHIFT)){
            this.setClampedScrollAmountH(this.scrollAmountH - scrollY * 10);
        }
        else {
            this.setScrollAmount(this.scrollAmount - scrollY * 10);
        }
        return true;
    }
    
    public void setClampedScrollAmountH(double scroll) {
        this.scrollAmountH = Mth.clamp(scroll, 0.0, this.maxScrollH);
    }
    
    protected boolean scrollbarVisibleH(){
        return this.maxScrollH > 0;
    }
    
    protected void scrollH(int scroll) {
        this.setClampedScrollAmountH(this.scrollAmountH + scroll);
    }
    
    protected int getScrollbarPositionH() {
        return getBoundary().inner().maxY() - 6;
    }
    
    
}
