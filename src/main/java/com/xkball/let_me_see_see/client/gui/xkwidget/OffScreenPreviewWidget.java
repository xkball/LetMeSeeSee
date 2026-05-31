package com.xkball.let_me_see_see.client.gui.xkwidget;

import com.xkball.let_me_see_see.client.offscreen.OffScreenRenders;
import com.xkball.xklib.ui.render.IGUIGraphics;
import com.xkball.xklib.ui.widget.Widget;

public class OffScreenPreviewWidget extends Widget {

    public OffScreenPreviewWidget() {
    }

    @Override
    public void doRender(IGUIGraphics graphics, int mouseX, int mouseY, float a) {
        super.doRender(graphics, mouseX, mouseY, a);
        // Placeholder rendering - draws a gray box where the preview would be
        graphics.fill((int) x, (int) y, (int) (x + width), (int) (y + height), 0xFF444444);
    }
}
