package com.xkball.let_me_see_see.client.offscreen;

import com.mojang.blaze3d.ProjectionType;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.utils.ClientUtils;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ProjectionMatrixBuffer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class OffScreenRenders {


    private static final ProjectionMatrixBuffer projBuffer = new ProjectionMatrixBuffer("LMS Off Screen Proj");

    public static RenderTarget renderTarget = new TextureTarget("off screen fbo",128,128,true,false);
    private static final ItemStackRenderState itemStackRenderState = new ItemStackRenderState();

    public static String exportItemStackAsPng(ItemStack itemStack, int width, int height, float scale, boolean writeToFile) {
        renderTarget.resize(width, height);
        return exportItemStackAsPng(renderTarget, itemStack, scale, writeToFile);
    }

    public static String exportItemStackAsPng(RenderTarget fbo, ItemStack itemStack, float scale, boolean writeToFile) {
        var itemID = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        var exportPath = Path.of(LetMeSeeSee.EXPORT_DIR_PATH, "_data", itemID.getNamespace(), itemID.getPath() + ".png");
        renderItemStack(itemStack, fbo, scale);
        AtomicReference<String> result = new AtomicReference<>("");
        ClientUtils.takeScreenshotWithAlpha(fbo,(nativeImage -> {
            try {
                if (writeToFile) {
                    Util.ioPool().execute(() -> {
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            exportPath.getParent().toFile().mkdirs();
                            nativeImage.writeToFile(exportPath);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
                result.set(VanillaUtils.base64(ClientUtils.asByteArray(nativeImage)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }));
        return result.get();
    }

    public static void renderItemStack(ItemStack itemStack, RenderTarget fbo, float scaleMul) {
        var width = fbo.width;
        var height = fbo.height;
        var mc = Minecraft.getInstance();

        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(projBuffer.getBuffer(new Matrix4f().setOrtho(0, width, 0, height, -4000.0F, 4000.0F)), ProjectionType.ORTHOGRAPHIC);
        var bufferSource = mc.renderBuffers().bufferSource();
        var submitNodeCollector = mc.gameRenderer.getSubmitNodeStorage();
        var featureDispatcher = mc.gameRenderer.getFeatureRenderDispatcher();

        float scale = Math.min(width, height);
        var shift = Math.abs(width - height) / 2f;
        var shiftX = width > height ? shift : 0;
        var shiftY = height > width ? shift : 0;
        scale *= scaleMul;
        var modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.set(new Matrix4f());

        var poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(shiftX, shiftY, 0);
//        poseStack.translate(scale / (scaleMul * 2), scale / (scaleMul * 2), 0);
        poseStack.translate((float) width / 2, (float) height / 2, 0);
        poseStack.scale(scale, -scale, scale);
//        poseStack.scale(2,2,2);
        Minecraft.getInstance()
                .getItemModelResolver()
                .updateForTopItem(itemStackRenderState, itemStack, ItemDisplayContext.GUI, null, null, 42);
        var flag = !itemStackRenderState.usesBlockLight();
        if (flag) {
            bufferSource.endBatch();
            Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_FLAT);
        }
        ClientUtils.clear(fbo,true);
        RenderSystem.outputColorTextureOverride = fbo.getColorTextureView();
        RenderSystem.outputDepthTextureOverride = fbo.getDepthTextureView();
        itemStackRenderState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);
        featureDispatcher.renderAllFeatures();
        bufferSource.endBatch();
//        VanillaUtils.ClientHandler.renderAxis(bufferSource,poseStack);
        RenderSystem.outputColorTextureOverride = null;
        RenderSystem.outputDepthTextureOverride = null;
        if (flag) {
            Minecraft.getInstance().gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        }

        poseStack.popPose();
        modelView.popMatrix();
        RenderSystem.restoreProjectionMatrix();
    }
}
