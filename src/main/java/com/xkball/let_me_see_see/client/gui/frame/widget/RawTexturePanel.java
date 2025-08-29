package com.xkball.let_me_see_see.client.gui.frame.widget;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.AutoResizeWidget;
import com.xkball.let_me_see_see.utils.ClientUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class RawTexturePanel extends AutoResizeWidget {
    
    private final Supplier<RenderTarget> targetSupplier;
    @Nullable
    private GpuTexture gpuTexture;
    @Nullable
    private GpuTextureView gpuTextureView;
    
    public RawTexturePanel(RenderTarget target) {
        this(() -> target);
    }
    
    public RawTexturePanel(Supplier<RenderTarget> targetSupplier) {
        super(Component.empty());
        this.targetSupplier = targetSupplier;
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        updateGpuTexture(targetSupplier.get());
        assert this.gpuTextureView != null;
        guiGraphics.submitBlit(RenderPipelines.GUI_TEXTURED, this.gpuTextureView,
                getBoundary().inner().x(),getBoundary().inner().y(),
                getBoundary().inner().maxX(), getBoundary().inner().maxY(),
                0,1,0,1,-1
                );

    }
    
    public void updateGpuTexture(RenderTarget target) {
        var w = target.width;
        var h = target.height;
        if(gpuTexture == null || gpuTextureView == null || gpuTexture.getWidth(0) != w || gpuTexture.getHeight(0) != h) {
            if (gpuTexture != null) {
                gpuTexture.close();
            }
            if(gpuTextureView != null) {
                gpuTextureView.close();
            }
            gpuTexture = ClientUtils.getGpuDevice().createTexture(() -> "lms texture panel",GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_COPY_DST, TextureFormat.RGBA8, w, h, 1, 1);
            gpuTexture.setTextureFilter(FilterMode.NEAREST, false);
            gpuTextureView = ClientUtils.getGpuDevice().createTextureView(gpuTexture);
        }
        assert target.getColorTexture() != null;
        ClientUtils.copyFrameBufferColorTo(target.getColorTexture(), gpuTexture);
    }
    
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    
    }
}
