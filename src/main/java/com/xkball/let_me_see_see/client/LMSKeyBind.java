package com.xkball.let_me_see_see.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.util.Lazy;
import org.lwjgl.glfw.GLFW;

//@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class LMSKeyBind {
    
    public static final Lazy<KeyMapping> TRANSLATE_KEY = Lazy.of(() -> new KeyMapping("keys.let_me_see_see.translate", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_T,"key.categories.misc"));
    public static boolean markItemNextFrame = false;
    
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(TRANSLATE_KEY.get());
    }
    
    //@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
    public static class GameEventHandler{
        @SubscribeEvent
        public static void beforeRender(ScreenEvent.Render.Pre event) {
            if(!markItemNextFrame) return;
            markItemNextFrame = false;
            if(!(event.getScreen() instanceof AbstractContainerScreen<?> acs)) return;
            if(acs.hoveredSlot == null) return;
            ItemStackTooltipTranslator.submit(acs.hoveredSlot.getItem(),event.getGuiGraphics());
        }
        
        @SubscribeEvent
        public static void onKeyInput(ScreenEvent.KeyPressed.Pre event){
            if(!TRANSLATE_KEY.get().isActiveAndMatches(InputConstants.getKey(event.getKeyCode(), event.getScanCode()))) return;
            markItemNextFrame = true;
        }
    }
    
}
