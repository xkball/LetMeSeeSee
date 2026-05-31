package com.xkball.let_me_see_see.mixin;

import com.xkball.xklib.ui.render.ComponentStyle;
import com.xkball.xklibmc.x3d.backend.b3d.gui.ComponentConverter;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ComponentConverter.class)
public class MixinComponentConverter {
    
    @Overwrite
    public static Style toStyle(ComponentStyle componentStyle) {
        var result = Style.EMPTY;
        if(componentStyle.color() != null) result = result.withColor(componentStyle.color());
        if(componentStyle.baseline()) result = result.withUnderlined(true);
        if(componentStyle.strikethrough()) result = result.withStrikethrough(true);
        if(componentStyle.bold()) result = result.withBold(true);
        if(componentStyle.italic()) result = result.withItalic(true);
        return result;
    }
}
