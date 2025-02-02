package com.xkball.let_me_see_see.client.gui.frame.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum HorizontalAlign implements StringRepresentable {
    LEFT(Component.translatable("let_me_see_see.gui.align_left")),
    CENTER(Component.translatable("let_me_see_see.gui.align_center")),
    RIGHT(Component.translatable("let_me_see_see.gui.align_right"));
    
    public static final Codec<HorizontalAlign> CODEC = StringRepresentable.fromEnum(HorizontalAlign::values);
    public static final StreamCodec<ByteBuf, HorizontalAlign> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    
    private static final HorizontalAlign[] VALUES = HorizontalAlign.values();
    public final Component displayName;
    
    HorizontalAlign(Component displayName) {
        this.displayName = displayName;
    }
    
    public static HorizontalAlign byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal <= VALUES.length ? VALUES[ordinal] : CENTER;
    }
    
    @Override
    public String getSerializedName() {
        return name().toLowerCase();
    }
    
}