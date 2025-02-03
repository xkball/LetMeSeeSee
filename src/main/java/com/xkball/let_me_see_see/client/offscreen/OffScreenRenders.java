package com.xkball.let_me_see_see.client.offscreen;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexSorting;
import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.io.IOException;
import java.nio.file.Path;

@OnlyIn(Dist.CLIENT)
public class OffScreenRenders {
    
    public static final OffScreenFBO FBO = new OffScreenFBO(128,128);
    
    public static void exportItemStackAsPng(ItemStack itemStack,int width,int height, float scale){
        FBO.resize(width,height);
        var itemID = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
        var exportPath = Path.of(LetMeSeeSee.EXPORT_DIR_PATH,"_data",itemID.getNamespace(),itemID.getPath()+".png");
        try(var result = FBO.drawOffScreen(() -> renderItemStack(itemStack,width,height,scale))){
            //noinspection ResultOfMethodCallIgnored
            exportPath.getParent().toFile().mkdirs();
            result.flipY();
            result.writeToFile(exportPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void renderItemStack(ItemStack itemStack,int width,int height,float scaleMul) {
        var itemRenderer = Minecraft.getInstance().getItemRenderer();
        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        var size = Math.min(width,height);
        var scale = size / 16f;
        var model = itemRenderer.getModel(itemStack,null,null,42);
        var shift = Math.abs(width-height)/2f;
        var shiftX = width > height ? shift : 0;
        var shiftY = height > width ? shift : 0;
        var orthoProj = new Matrix4f();
        orthoProj.setOrtho(0,scale,scale,0,-1000,1000);
        scale*=scaleMul;
        var oldProjMatrix = RenderSystem.getProjectionMatrix();
        var oldSorting = RenderSystem.getVertexSorting();
        RenderSystem.setProjectionMatrix(orthoProj, VertexSorting.byDistance(v -> v.z));
        var poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(shiftX,shiftY,0);
        poseStack.translate(scale/(scaleMul*2),scale/(scaleMul*2),0);
        poseStack.scale(scale,-scale,scale);
        var flag = !model.usesBlockLight();
//        poseStack.mulPose(Axis.YP.rotationDegrees(45));
//        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        //poseStack.mulPose(Axis.ZP.rotationDegrees(180));

        if(flag){
            Lighting.setupForFlatItems();
        }
        RenderSystem.disableDepthTest();
        itemRenderer.render(itemStack, ItemDisplayContext.GUI,false,poseStack,bufferSource,15728880, OverlayTexture.NO_OVERLAY,model);
        VanillaUtils.ClientHandler.renderAxis(bufferSource,poseStack);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if(flag){
            Lighting.setupFor3DItems();
        }
        
        poseStack.popPose();
        RenderSystem.setProjectionMatrix(oldProjMatrix,oldSorting);
    }
}
