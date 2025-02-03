package com.xkball.let_me_see_see.client.offscreen;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.MemoryStack;
import org.slf4j.Logger;

import java.nio.ByteBuffer;

@OnlyIn(Dist.CLIENT)
public class OffScreenFBO {
    private static final Logger LOGGER = LogUtils.getLogger();
    private int width;
    private int height;
    private int fboID = -1;
    private int textureID = -1;
    private int depthRbID = -1;
    
    public OffScreenFBO(int width, int height) {
        this.width = width;
        this.height = height;
        this.createFBO();
    }
    
    public void resize(int width, int height) {
        if(this.width == width && this.height == height) return;
        this.width = width;
        this.height = height;
        this.deleteFBO();
        this.createFBO();
    }
    
    public NativeImage drawOffScreen(Runnable renderer){
        var oldFBO = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        MemoryStack.stackPush();
        var oldViewPort = MemoryStack.stackMallocInt(4);
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, oldViewPort);
        
        GL11.glViewport(0, 0, width, height);
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,fboID);
        GL11.glClearColor(0,0,0,0);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT|GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        
        renderer.run();
        
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,oldFBO);
        GL11.glViewport(oldViewPort.get(0), oldViewPort.get(1), oldViewPort.get(2), oldViewPort.get(3));
        MemoryStack.stackPop();
        var oldTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        var result = new NativeImage(width, height, false);
        result.downloadTexture(0,false);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, oldTexture);
        return result;
    }
    
    private void createFBO(){
        fboID = EXTFramebufferObject.glGenFramebuffersEXT();
        textureID = GL11.glGenTextures();
        depthRbID = EXTFramebufferObject.glGenRenderbuffersEXT();
        
        var oldFBO = GL11.glGetInteger(EXTFramebufferObject.GL_FRAMEBUFFER_BINDING_EXT);
        var oldTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        var oldRBO=  GL11.glGetInteger(EXTFramebufferObject.GL_RENDERBUFFER_BINDING_EXT);
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureID);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthRbID);
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, GL11.GL_DEPTH_COMPONENT, width, height);
        
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,fboID);
        EXTFramebufferObject.glFramebufferTexture2DEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_COLOR_ATTACHMENT0_EXT, GL11.GL_TEXTURE_2D, textureID,0);
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, depthRbID);
        
        int status = EXTFramebufferObject.glCheckFramebufferStatusEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT);
        if (status != EXTFramebufferObject.GL_FRAMEBUFFER_COMPLETE_EXT) {
            LOGGER.error("Failed to create FBO: {}", status);
            throw new RuntimeException("Failed to create FBO: " + status);
        }
        
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, oldTexture);
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, oldRBO);
        EXTFramebufferObject.glBindFramebufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT,oldFBO);
    }
    
    private void deleteFBO(){
        GL11.glDeleteTextures(textureID);
        EXTFramebufferObject.glDeleteRenderbuffersEXT(depthRbID);
        EXTFramebufferObject.glDeleteFramebuffersEXT(fboID);
    }
    
}
