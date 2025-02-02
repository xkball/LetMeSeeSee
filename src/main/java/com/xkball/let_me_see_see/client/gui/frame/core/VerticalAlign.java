package com.xkball.let_me_see_see.client.gui.frame.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum VerticalAlign implements StringRepresentable {
    TOP(Component.translatable("let_me_see_see.gui.align_top")),
    CENTER(Component.translatable("let_me_see_see.gui.align_center")),
    BOTTOM(Component.translatable("let_me_see_see.gui.align_bottom"));
    
    public static final Codec<VerticalAlign> CODEC = StringRepresentable.fromEnum(VerticalAlign::values);
    public static final StreamCodec<ByteBuf, VerticalAlign> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    
    private static final VerticalAlign[] VALUES = values();
    public final Component displayName;
    
    VerticalAlign(Component displayName) {
        this.displayName = displayName;
    }
    
    
    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
}
