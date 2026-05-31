package com.xkball.let_me_see_see.client.gui.xkwidget;

import com.xkball.xklib.ui.render.IGUIGraphics;
import com.xkball.xklib.ui.widget.Widget;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.List;

public class CodePreviewWidget extends Widget {

    private final List<Component> lines;
    private static final int LINE_HEIGHT = 10;

    public CodePreviewWidget(List<Component> lines) {
        this.lines = lines;
    }

    @Override
    public void doRender(IGUIGraphics graphics, int mouseX, int mouseY, float a) {
        super.doRender(graphics, mouseX, mouseY, a);
        float yPos = y + 2;
        for (var line : lines) {
            var text = line.getString();
            if (text.isEmpty()) {
                yPos += LINE_HEIGHT;
                continue;
            }
            // Handle styled components for syntax highlighting
            if (line.getStyle().equals(Style.EMPTY) && line.getSiblings().isEmpty()) {
                graphics.drawString(text, x + 2, yPos, -1);
            } else {
                float xPos = x + 2;
                renderComponent(graphics, line, xPos, yPos);
            }
            yPos += LINE_HEIGHT;
        }
    }

    private float renderComponent(IGUIGraphics graphics, Component comp, float xPos, float yPos) {
        var text = comp.getString();
        int color = comp.getStyle().getColor() != null ? comp.getStyle().getColor().getValue() : -1;
        if (!text.isEmpty()) {
            graphics.drawString(text, xPos, yPos, color);
            xPos += text.length() * 6; // approximate char width
        }
        for (var sibling : comp.getSiblings()) {
            xPos = renderComponent(graphics, sibling, xPos, yPos);
        }
        return xPos;
    }

    public static CodePreviewWidget fromLines(List<Component> lines) {
        return new CodePreviewWidget(lines);
    }
}
