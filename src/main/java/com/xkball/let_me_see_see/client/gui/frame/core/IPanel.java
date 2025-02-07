package com.xkball.let_me_see_see.client.gui.frame.core;

import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.client.gui.frame.core.render.IGUIDecoRenderer;
import com.xkball.let_me_see_see.client.gui.frame.screen.FrameScreen;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public interface IPanel {
    
    IUpdateMarker GLOBAL_UPDATE_MARKER = new IUpdateMarker() {
        
        @Override
        public boolean needUpdate() {
            var screen = Minecraft.getInstance().screen;
            if(screen instanceof FrameScreen frameScreen){
                return frameScreen.needUpdate();
            }
            return false;
        }
        
        @Override
        public void setNeedUpdate() {
            var screen = Minecraft.getInstance().screen;
            if(screen instanceof FrameScreen frameScreen){
                frameScreen.setNeedUpdate();
            }
        }
    };
    
    List<IPanel> EMPTY = List.of();
    
    //均使用0-1表示占用上级空间的比例
    //不应小于0
    float getXPercentage();
    
    float getYPercentage();
    
    //没有padding压缩(?)
    float getLeftPadding();
    
    float getRightPadding();
    
    float getTopPadding();
    
    float getBottomPadding();
    
    //单位为由mc调整后的像素
    int getXMax();
    
    int getYMax();
    
    int getXMin();
    
    int getYMin();
    
    WidgetBoundary getBoundary();
    
    @Nullable
    IGUIDecoRenderer getDecoRenderer();
    
    boolean getIsFocused();
    
    void setXPercentage(float percentage);
    
    void setYPercentage(float percentage);
    
    void setLeftPadding(float percentage);
    
    void setRightPadding(float percentage);
    
    void setTopPadding(float percentage);
    
    void setBottomPadding(float percentage);
    
    void setXMax(int max);
    
    void setYMax(int max);
    
    void setXMin(int min);
    
    void setYMin(int min);
    
    void setBoundary(WidgetBoundary boundary);
    
    void setDecoRenderer(IGUIDecoRenderer decoRenderer);
    
    default List<IPanel> getChildren() {
        return EMPTY;
    }
    
    //调用此方法后才会计算子部件布局
    default void resize() {
    }
    
    //调用此方法更新内容 如果改变子部件应该调用resize
    default boolean update(IUpdateMarker updateMarker) {
        var flag = false;
        for (var child : getChildren()) {
            flag |= child.update(updateMarker);
        }
        if (flag) resize();
        return false;
    }
    
    //如果部件可以计算占用的最小空间则调用方法后应仅占用最小空间
    default void trim() {
    }
    
    default void setFixWidth(int width) {
        setXMax(width);
        setXMin(width);
    }
    
    default void setFixHeight(int height) {
        setYMax(height);
        setYMin(height);
    }
    
    default void shiftWidgetBoundary(int x, int y) {
        if (x == 0 && y == 0) return;
        var boundary = getBoundary();
        var newInner = new WidgetPos(boundary.inner().x() + x, boundary.inner().y() + y, boundary.inner().width(), boundary.inner().height());
        var newOuter = new WidgetPos(boundary.outer().x() + x, boundary.outer().y() + y, boundary.outer().width(), boundary.outer().height());
        setBoundary(new WidgetBoundary(newOuter, newInner));
    }
    
    default void renderDecoration(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (getDecoRenderer() != null) {
            getDecoRenderer().render(guiGraphics, getBoundary(), mouseX, mouseY, partialTick);
        }
        recordDebugBoundary(getBoundary());
    }
    
    default void recordDebugBoundary(WidgetBoundary boundary) {
        if (LetMeSeeSee.IS_DEBUG && Minecraft.getInstance().getDebugOverlay().showDebugScreen()) {
            var od = boundary.outer();
            var id = boundary.inner();
            var blue0 = VanillaUtils.getColor(173, 216, 230, 10);
            var yellow0 = VanillaUtils.getColor(255, 250, 205, 10);
            var blue1 = VanillaUtils.getColor(0, 0, 255, 255);
            var yellow1 = VanillaUtils.getColor(255, 255, 0, 255);
            DebugLine.lines.add(new DebugLine(od.x(), od.y(), od.width(), od.height(), getIsFocused() ? blue1 : blue0));
            DebugLine.lines.add(new DebugLine(id.x(), id.y(), id.width(), id.height(), getIsFocused() ? yellow1 : yellow0));
        }
    }
    
    static float calculatePadding(float padding, int base) {
        return padding > 1f ? padding : Mth.clamp(base * padding, 0f, base);
    }
    
    static void calculateBoundary(IPanel widget, WidgetPos parentPos, int x, int y){
        var width = Mth.clamp(parentPos.width() * widget.getXPercentage(), widget.getXMin(), Math.min(widget.getXMax(), parentPos.maxX() - x));
        var height = Mth.clamp(parentPos.height() * widget.getYPercentage(), widget.getYMin(), Math.min(widget.getYMax(), parentPos.maxY() - y));
        var leftPadding = IPanel.calculatePadding(widget.getLeftPadding(), parentPos.width());
        var rightPadding = IPanel.calculatePadding(widget.getRightPadding(), parentPos.width());
        var topPadding = IPanel.calculatePadding(widget.getTopPadding(), parentPos.height());
        var bottomPadding = IPanel.calculatePadding(widget.getBottomPadding(), parentPos.height());
        
        var outerWidth = (int) (leftPadding + width + rightPadding);
        var outerHeight = (int) (topPadding + height + bottomPadding);
        var outer = new WidgetPos(x, y, outerWidth, outerHeight);
        var inner = new WidgetPos((int) (x + leftPadding), (int) (y + topPadding), (int) width, (int) height);
        widget.setBoundary(new WidgetBoundary(outer, inner));
    }
    
    static int calculateShift(HorizontalAlign align, int sizeLimit, int size){
        return switch (align) {
            case LEFT -> 0;
            case CENTER -> (int) (sizeLimit / 2f - size / 2f);
            case RIGHT -> sizeLimit - size;
        };
    }
    
    static int calculateShift(VerticalAlign align, int sizeLimit, int size){
        return switch (align) {
            case TOP -> 0;
            case CENTER -> (int) (sizeLimit / 2f - size / 2f);
            case BOTTOM -> sizeLimit - size;
        };
    }
    
    record DebugLine(int x, int y, int width, int height, int color) {
        public static final List<DebugLine> lines = new ArrayList<>();
        
        public static void drawAllDebugLines(GuiGraphics guiGraphics) {
            for (var line : lines) {
                guiGraphics.renderOutline(line.x(), line.y(), line.width(), line.height(), line.color());
            }
            lines.clear();
        }
    }
}
