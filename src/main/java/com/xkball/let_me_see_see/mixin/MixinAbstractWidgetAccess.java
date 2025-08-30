package com.xkball.let_me_see_see.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractWidget.class)
public interface MixinAbstractWidgetAccess {
    
    @Invoker
    void invokeRenderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick);
    
}
