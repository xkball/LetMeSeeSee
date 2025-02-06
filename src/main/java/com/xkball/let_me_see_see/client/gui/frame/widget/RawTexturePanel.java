package com.xkball.let_me_see_see.client.gui.frame.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.AutoResizeWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import org.joml.Matrix4f;

import java.util.function.IntSupplier;

public class RawTexturePanel extends AutoResizeWidget {
    
    private final IntSupplier textureIDGetter;
    
    public RawTexturePanel(int textureID){
        this(() -> textureID);
    }
    
    public RawTexturePanel(IntSupplier textureIDGetter) {
        super(Component.empty());
        this.textureIDGetter = textureIDGetter;
    }
    
    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        var texture = textureIDGetter.getAsInt();
        //texture = 18;
        
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        
        Matrix4f matrix4f = guiGraphics.pose().last().pose();
        var x1 = getBoundary().inner().x();
        var y1 = getBoundary().inner().y();
        var x2 = getBoundary().inner().maxX();
        var y2 = getBoundary().inner().maxY();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(matrix4f, x1, y1, 0).setUv(0, 1);
        bufferbuilder.addVertex(matrix4f, x1, y2, 0).setUv(0, 0);
        bufferbuilder.addVertex(matrix4f, x2, y2, 0).setUv(1, 0);
        bufferbuilder.addVertex(matrix4f, x2, y1, 0).setUv(1, 1);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
        
    }
    
    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    
    }
}
