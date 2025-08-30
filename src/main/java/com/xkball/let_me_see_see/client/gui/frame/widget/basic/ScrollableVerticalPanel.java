package com.xkball.let_me_see_see.client.gui.frame.widget.basic;

import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.WidgetPos;
import com.xkball.let_me_see_see.client.gui.frame.core.render.GuiDecorations;
import com.xkball.let_me_see_see.client.gui.frame.core.render.SimpleBackgroundRenderer;
import com.xkball.let_me_see_see.mixin.MixinAbstractWidgetAccess;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector2f;

import javax.annotation.Nullable;

public class ScrollableVerticalPanel extends VerticalPanel {
    
    protected static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
    protected static final ResourceLocation SCROLLER_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller_background");
    
    public IntArrayList heightList = new IntArrayList();
    public int maxScroll = 0;
    public int maxPosition = 0;
    public double scrollAmount;
    public boolean scrolling;
    @Nullable
    public AbstractWidget selected;
    @Nullable
    public AbstractWidget hovered;
    
    public ScrollableVerticalPanel() {
    
    }
    
    @Override
    public void clearWidget() {
        super.clearWidget();
        this.heightList.clear();
        this.selected = null;
        this.hovered = null;
    }
    
    @Override
    @SuppressWarnings("DuplicatedCode")
    public void resize() {
        this.heightList.clear();
        var parentPos = widgetBoundary.inner();
        var parentPos_ = new WidgetPos(parentPos.x(), parentPos.y(), parentPos.width(), Integer.MAX_VALUE / 2);
        var y = parentPos.y();
        for (var widget : childrenPanels) {
            IPanel.calculateBoundary(widget, parentPos_, parentPos.x(), y);
            y += widget.getBoundary().outer().height();
            this.heightList.add(y - parentPos.y());
        }
        var heightSum = y - parentPos.y();
        this.maxPosition = heightSum;
        this.maxScroll = Math.max(0, heightSum - parentPos.height() + 6);
        var shiftY = IPanel.calculateShift(verticalAlign, parentPos.height(), heightSum);
        for (var widget : childrenPanels) {
            var shiftX = IPanel.calculateShift(horizontalAlign, parentPos.width(), widget.getBoundary().outer().width());
            widget.shiftWidgetBoundary(shiftX, shiftY);
            widget.resize();
        }
        clampScrollAmount();
    }
    
    public void renderSelectedBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        if (selected == null) return;
        if (selected instanceof IPanel panel) {
            var boundary = panel.getBoundary();
            SimpleBackgroundRenderer.GRAY.render(guiGraphics, boundary, mouseX, mouseY, partialTicks);
            if (boundary.outer().inside(mouseX, mouseY)) {
                GuiDecorations.WHITE_BORDER.render(guiGraphics, boundary, mouseX, mouseY, partialTicks);
            } else {
                GuiDecorations.GRAY_BORDER.render(guiGraphics, boundary, mouseX, mouseY, partialTicks);
            }
        } else
            guiGraphics.fill(selected.getX(), selected.getY(), selected.getRight(), selected.getBottom(), VanillaUtils.GUI_GRAY);
    }
    
    public int getBoundaryHeight() {
        return getBoundary().inner().height();
    }
    
    public int getBoundaryY() {
        return getBoundary().inner().y();
    }
    
    public void translateGUIMatrix(GuiGraphics guiGraphics){
        guiGraphics.pose().translate(0f,(float) -scrollAmount);
    }
    
    public Vector2f getActualMousePos(float mouseX, float mouseY) {
        return new Vector2f(mouseX, (float) (mouseY + scrollAmount));
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDecoration(guiGraphics, mouseX, mouseY, partialTick);
        this.hovered = this.getEntryAtPosition(mouseX, mouseY);
        guiGraphics.pose().pushMatrix();
        this.enableScissor(guiGraphics);
        this.translateGUIMatrix(guiGraphics);
        renderSelectedBackground(guiGraphics, mouseX, mouseY, partialTick);
        var actualMouse = getActualMousePos(mouseX, mouseY);
        for (int i = 0; i < heightList.size(); i++) {
            var pos = heightList.getInt(i);
            if (pos < scrollAmount - 10) continue;
            if (pos > scrollAmount + getBoundaryHeight() + 10) break;
            var widget = children.get(i);
            var widgetRec = widget.getRectangle();
            widget.isHovered =  actualMouse.x >= widget.getX()
                    && actualMouse.y >= widget.getY()
                    && actualMouse.x < widget.getRight()
                    && actualMouse.y < widget.getBottom();
            ((MixinAbstractWidgetAccess)widget).invokeRenderWidget(guiGraphics, (int) actualMouse.x, (int) actualMouse.y, partialTick);
            widget.tooltip.refreshTooltipForNextRenderPass(guiGraphics,mouseX,mouseY,widget.isHovered(), widget.isFocused(),
                    new ScreenRectangle(new ScreenPosition(widgetRec.left(), (int) (widgetRec.top()-scrollAmount)),widgetRec.width(),widgetRec.height()));
        }
        renderDecoration(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.disableScissor();
        guiGraphics.pose().popMatrix();
        
        if (this.scrollbarVisible()) {
            int l = this.getScrollbarPosition();
            var h = getBoundaryHeight();
            var y = getBoundaryY();
            int i1 = (int) ((float) (h * h) / (float) this.maxPosition);
            i1 = Mth.clamp(i1, 32, h - 8);
            int k = (int) this.scrollAmount * (h - i1) / this.maxScroll + y;
            if (k < this.getY()) {
                k = this.getY();
            }
            
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,SCROLLER_BACKGROUND_SPRITE, l, y, 6, h);
            guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,SCROLLER_SPRITE, l, k, 6, i1);
           
        }
        
    }
    
    protected boolean scrollbarVisible() {
        return this.maxScroll > 0;
    }
    
    protected void enableScissor(GuiGraphics guiGraphics) {
        var bound = getBoundary().inner();
        guiGraphics.enableScissor(bound.x(), bound.y(), bound.maxX() - 6, bound.maxY());
    }
    
    protected void scroll(int scroll) {
        this.setScrollAmount(this.scrollAmount + (double) scroll);
    }
    
    public void setClampedScrollAmount(double scroll) {
        this.scrollAmount = Mth.clamp(scroll, 0.0, this.maxScroll);
    }
    
    public void setScrollAmount(double scroll) {
        this.setClampedScrollAmount(scroll);
    }
    
    public void clampScrollAmount() {
        this.setClampedScrollAmount(this.scrollAmount);
    }
    
    protected void updateScrollingState(double mouseX, double mouseY, int button) {
        this.scrolling = button == 0 && mouseX >= (double) this.getScrollbarPosition() && mouseX < (double) (this.getScrollbarPosition() + 6);
    }
    
    protected int getScrollbarPosition() {
        return getBoundary().inner().maxX() - 6;
    }
    
    protected boolean isValidMouseClick(int button) {
        return button == 0;
    }
    
    @Nullable
    protected AbstractWidget getEntryAtPosition(double mouseX, double mouseY) {
        if (!isMouseOver(mouseX, mouseY)) return null;
        var yOnPanel = mouseY - getBoundaryY() + scrollAmount;
        for (int i = 0; i < this.heightList.size(); i++) {
            if (yOnPanel < this.heightList.getInt(i)) {
                return this.children.get(i);
            }
        }
        return null;
    }
    
    protected boolean anyScrolling(){
        return this.scrolling;
    }
    
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.isValidMouseClick(button)) {
            return false;
        } else {
            this.updateScrollingState(mouseX, mouseY, button);
            if (!this.isMouseOver(mouseX, mouseY)) {
                return false;
            } else {
                var widget = this.getEntryAtPosition(mouseX, mouseY);
                //this.setSelected(widget);
                if (widget != null) {
                    var actualMouse = this.getActualMousePos((float) mouseX, (float) mouseY);
                    if (widget.mouseClicked(actualMouse.x, actualMouse.y, button)) {
                        var oldFocused = this.getFocused();
                        if (oldFocused != widget && oldFocused instanceof ContainerEventHandler containereventhandler) {
                            containereventhandler.setFocused(null);
                        }
                        this.setFocused(widget);
                        this.setDragging(true);
                        return true;
                    }
                }
                return this.anyScrolling();
            }
        }
    }
    
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (super.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            return true;
        } else if (button == 0 && this.scrolling) {
            if (mouseY < (double) this.getBoundaryY()) {
                this.setScrollAmount(0.0);
            } else if (mouseY > (double) this.getBoundary().inner().maxY()) {
                this.setScrollAmount(this.maxScroll);
            } else {
                double d0 = Math.max(1, this.maxScroll);
                int i = this.getBoundaryHeight();
                int j = Mth.clamp((int) ((float) (i * i) / (float) this.maxPosition), 32, i - 8);
                double d1 = Math.max(1.0, d0 / (double) (i - j));
                this.setScrollAmount(this.scrollAmount + dragY * d1);
            }
            
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        //todo[xkball] 支持滚动组件内的滚动组件
        this.setScrollAmount(this.scrollAmount - scrollY * 10);
        return true;
    }
    
    public void setSelected(@Nullable AbstractWidget selected) {
        this.selected = selected;
    }
    
    @Override
    public void setFocused(@Nullable GuiEventListener focused) {
        super.setFocused(focused);
        if (focused == null) selected = null;
        if (!(focused instanceof AbstractWidget)) return;
        super.setFocused(focused);
        int i = this.children.indexOf(focused);
        if (i >= 0) {
            var e = this.children.get(i);
            this.setSelected(e);
            if (Minecraft.getInstance().getLastInputType().isKeyboard()) {
                this.ensureVisible(e);
            }
        }
    }
    
    protected void ensureVisible(AbstractWidget entry) {
        int index = this.children().indexOf(entry);
        if (index < 0) return;
        var top = this.heightList.getInt(index);
        var bottom = index == 0 ? 0 : this.heightList.getInt(index - 1);
        var currentBottom = this.scrollAmount + this.getBoundaryHeight();
        if (top > currentBottom) {
            this.scroll((int) (top - currentBottom));
        }
        if (bottom < scrollAmount) {
            this.scroll((int) (bottom - scrollAmount));
        }
    }
}
