package com.xkball.let_me_see_see.client.gui.xkwidget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.xkball.let_me_see_see.client.offscreen.OffScreenRenders;
import com.xkball.xklib.ui.render.IGUIGraphics;
import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklibmc.api.client.b3d.SamplerCacheCache;
import com.xkball.xklibmc.x3d.backend.b3d.B3dGuiGraphics;

public class OffScreenPreviewWidget extends Widget {

    public OffScreenPreviewWidget() {
    }

    @Override
    public void doRender(IGUIGraphics graphics, int mouseX, int mouseY, float a) {
        super.doRender(graphics, mouseX, mouseY, a);

        var fbo = OffScreenRenders.renderTarget;
        if (fbo == null || !(graphics instanceof B3dGuiGraphics b3d)) return;
        
//        b3d.fill((int) this.x, (int) this.y, (int) this.getMaxX(), (int) this.getMaxY(), 0xff000000);
        b3d.getInner().blit(fbo.getColorTextureView(), SamplerCacheCache.NEAREST_CLAMP, (int) this.x, (int) this.y, (int) this.getMaxX(), (int) this.getMaxY(), 0, 1, 1, 0);
    }
}
