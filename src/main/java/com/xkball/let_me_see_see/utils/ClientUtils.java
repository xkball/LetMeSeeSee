package com.xkball.let_me_see_see.utils;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL43;
import org.lwjgl.stb.STBImage;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Consumer;

public class ClientUtils {
    
    public static final Logger LOGGER = LogUtils.getLogger();
    
    public static GpuDevice getGpuDevice(){
        return RenderSystem.getDevice();
    }
    
    public static CommandEncoder getCommandEncoder(){
        return RenderSystem.getDevice().createCommandEncoder();
    }
    
    public static RenderPass createRenderPass(String name){
        var colorTarget = Minecraft.getInstance().getMainRenderTarget().getColorTextureView();
        var depthTarget = Minecraft.getInstance().getMainRenderTarget().getDepthTextureView();
        //noinspection DataFlowIssue
        return getCommandEncoder().createRenderPass(() -> name, colorTarget, OptionalInt.empty(), depthTarget, OptionalDouble.empty());
    }
    
    public static void clear(RenderTarget target){
        clear(target, true);
    }
    
    public static void clear(RenderTarget target, boolean clearDepth){
        if(target.useDepth && clearDepth){
            getCommandEncoder().clearColorAndDepthTextures(Objects.requireNonNull(target.getColorTexture()),0,Objects.requireNonNull(target.getDepthTexture()),1d);
        }
        else {
            getCommandEncoder().clearColorTexture(Objects.requireNonNull(target.getColorTexture()),0);
        }
    }
    
    public static void renderAxis(MultiBufferSource bufferSource, PoseStack poseStack) {
        var buffer = bufferSource.getBuffer(RenderTypes.LINES);
        var matrix = poseStack.last();
        buffer.addVertex(matrix, 0, 0, 0).setNormal(matrix, -1, 0, 0).setColor(0xFFFF0000);
        buffer.addVertex(matrix, 100, 0, 0).setNormal(matrix, 1, 0, 0).setColor(0xFFFF0000);
        buffer.addVertex(matrix, 0, 0, 0).setNormal(matrix, 0, -1, 0).setColor(0xFF00FF00);
        buffer.addVertex(matrix, 0, 100, 0).setNormal(matrix, 0, 1, 0).setColor(0xFF00FF00);
        buffer.addVertex(matrix, 0, 0, 0).setNormal(matrix, 0, 0, -1).setColor(0xFF0000FF);
        buffer.addVertex(matrix, 0, 0, 100).setNormal(matrix, 0, 0, 1).setColor(0xFF0000FF);
    }
    
    public static void copyFrameBufferColorTo(GpuTexture from, GpuTexture to) {
        getCommandEncoder().copyTextureToTexture(Objects.requireNonNull(from), Objects.requireNonNull(to),0, 0, 0, 0, 0, from.getWidth(0), from.getHeight(0));
    }
    
    public static void copyFrameBufferColorTo(RenderTarget from, RenderTarget to) {
            getCommandEncoder().copyTextureToTexture(Objects.requireNonNull(from.getColorTexture()), Objects.requireNonNull(to.getColorTexture()),0, 0, 0, 0, 0, from.width, from.height);
    }
    
    public static void copyFrameBufferDepthTo(RenderTarget from, RenderTarget to) {
        to.copyDepthFrom(from);
    }
    
    public static BufferBuilder beginWithRenderPipeline(RenderPipeline pipeline){
        return Tesselator.getInstance().begin(pipeline.getVertexFormatMode(),pipeline.getVertexFormat());
    }
    
    public static boolean isCounterclockwisePoints(List<Vector3f> points){
        var n = new Vector3f();
        for(var i = 0; i < points.size() - 1; i++){
            var a = points.get(i);
            var b = points.get(i + 1);
            n.add(a.cross(b,new Vector3f()));
        }
        return n.dot(points.getFirst()) < 0;
    }
    
    public static boolean isEar(Vector3f c, Vector3f l, Vector3f r, Collection<Vector3f> points){
        var nab = l.cross(c,new Vector3f()).normalize();
        var nbc = c.cross(r,new Vector3f()).normalize();
        var nac = r.cross(l,new Vector3f()).normalize();
        
        var d = l.dot(c.cross(r,new Vector3f()));
        if(d < 0) return false;
        
        for(var p : points){
            //此处用 == 是有意义的 因此也要求输入三个点必须要在points中
            if(p == c || p == l || p == r) continue;
            var dab = Math.signum(p.dot(nab));
            var dbc = Math.signum(p.dot(nbc));
            var dac = Math.signum(p.dot(nac));
            if(dab > 0 && dbc > 0 && dac > 0){
                return false;
            }
        }
        return true;
    }
    
    public static byte[] asByteArray(NativeImage image) throws IOException {
        byte[] abyte;
        try (
                ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
                WritableByteChannel writablebytechannel = Channels.newChannel(bytearrayoutputstream)
        ) {
            if (!image.writeToChannel(writablebytechannel)) {
                throw new IOException("Could not write image to byte array: " + STBImage.stbi_failure_reason());
            }
            
            abyte = bytearrayoutputstream.toByteArray();
        }
        
        return abyte;
    }
    
    public static void takeScreenshotWithAlpha(RenderTarget renderTarget, Consumer<NativeImage> writer){
        int width = renderTarget.width;
        int height = renderTarget.height;
        GpuTexture gputexture = renderTarget.getColorTexture();
        if (gputexture == null) {
            throw new IllegalStateException("Tried to capture screenshot of an incomplete framebuffer");
        } else {
            GpuBuffer gpubuffer = RenderSystem.getDevice().createBuffer(() -> "Screenshot buffer", 9, width * height * gputexture.getFormat().pixelSize());
            CommandEncoder commandencoder = RenderSystem.getDevice().createCommandEncoder();
            RenderSystem.getDevice()
                    .createCommandEncoder()
                    .copyTextureToBuffer(
                            gputexture,
                            gpubuffer,
                            0,
                            () -> {},
                            0
                    );
            GL43.glFinish();
            try (GpuBuffer.MappedView gpubuffer$mappedview = commandencoder.mapBuffer(gpubuffer, true, false)) {
                NativeImage nativeimage = new NativeImage(width, height, false);
                for (int i1 = 0; i1 < height; i1++) {
                    for (int j1 = 0; j1 < width; j1++) {
                        int i3 = gpubuffer$mappedview.data().getInt((j1 + i1 * width) * gputexture.getFormat().pixelSize());
                        nativeimage.setPixelABGR(j1, height - i1 - 1, i3);
                    }
                }
                writer.accept(nativeimage);
            }
            gpubuffer.close();
        }
    }
}
