package com.xkball.let_me_see_see.client.gui.frame.widget.basic;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.WidgetPos;
import com.xkball.let_me_see_see.client.gui.frame.core.render.GuiDecorations;
import com.xkball.let_me_see_see.client.gui.frame.core.render.SimpleBackgroundRenderer;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class ScrollableVerticalPanel extends VerticalPanel {
    
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller");
    private static final ResourceLocation SCROLLER_BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("widget/scroller_background");
    
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
        this.maxScroll = Math.max(0, heightSum - parentPos.height());
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
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderDecoration(guiGraphics, mouseX, mouseY, partialTick);
        this.hovered = this.getEntryAtPosition(mouseX, mouseY);
        guiGraphics.pose().pushPose();
        this.enableScissor(guiGraphics);
        guiGraphics.pose().translate(0, -scrollAmount, 0);
        renderSelectedBackground(guiGraphics, mouseX, mouseY, partialTick);
        int actualMouseY = (int) (mouseY + scrollAmount);
        for (int i = 0; i < heightList.size(); i++) {
            var pos = heightList.getInt(i);
            if (pos < scrollAmount - 10) continue;
            if (pos > scrollAmount + getBoundaryHeight() + 10) break;
            var widget = children.get(i);
            var widgetRec = widget.getRectangle();
            widget.render(guiGraphics, mouseX, actualMouseY, partialTick);
            widget.isHovered =  mouseX >= widget.getX()
                    && actualMouseY >= widget.getY()
                    && mouseX < widget.getRight()
                    && actualMouseY < widget.getBottom();
            this.refreshScrollWidgetTooltip(widget.tooltip.get(), widget.isHovered(), widget.isFocused(), new ScreenRectangle(new ScreenPosition(widgetRec.left(), (int) (widgetRec.top()-scrollAmount)),widgetRec.width(),widgetRec.height()));
        }
        renderDecoration(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.disableScissor();
        guiGraphics.pose().popPose();
        
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
            
            RenderSystem.enableBlend();
            guiGraphics.blitSprite(RenderType::guiTextured,SCROLLER_BACKGROUND_SPRITE, l, y, 6, h);
            guiGraphics.blitSprite(RenderType::guiTextured,SCROLLER_SPRITE, l, k, 6, i1);
            RenderSystem.disableBlend();
        }
        
    }
    
    private void refreshScrollWidgetTooltip(@Nullable Tooltip tooltip, boolean isHovered, boolean isFocused, ScreenRectangle rect){
        if (tooltip == null) return;
        boolean flag = isHovered|| isFocused && Minecraft.getInstance().getLastInputType().isKeyboard();
        if (flag) {
            Screen screen = Minecraft.getInstance().screen;
            if (screen != null) {
                screen.setTooltipForNextRenderPass(tooltip, DefaultTooltipPositioner.INSTANCE, isFocused);
            }
        }
        
    }
    
    protected boolean scrollbarVisible() {
        return this.maxScroll > 0;
    }
    
    protected void enableScissor(GuiGraphics guiGraphics) {
        var bound = getBoundary().inner();
        guiGraphics.enableScissor(bound.x(), bound.y(), bound.maxX() - 6, bound.maxY());
    }
    
    private void scroll(int scroll) {
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
                    if (widget.mouseClicked(mouseX, mouseY + scrollAmount, button)) {
                        var oldFocused = this.getFocused();
                        if (oldFocused != widget && oldFocused instanceof ContainerEventHandler containereventhandler) {
                            containereventhandler.setFocused(null);
                        }
                        this.setFocused(widget);
                        this.setDragging(true);
                        return true;
                    }
                }
                return this.scrolling;
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
