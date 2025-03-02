package com.xkball.let_me_see_see.mixin;

import net.minecraft.client.data.models.ModelProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ModelProvider.ItemInfoCollector.class)
public class MixinItemInfoCollector {
    
    @SuppressWarnings("rawtypes")
    @Redirect(method = "finalizeAndValidate",at = @At(value = "INVOKE", target = "Ljava/util/List;isEmpty()Z"))
    public boolean cancelValidCheck(List instance){
        return true;
    }
}
