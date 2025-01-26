package com.xkball.let_me_see_see.client.gui.frame;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum Align implements StringRepresentable {
    LEFT(Component.translatable("let_me_see_see.gui.align_left")),
    CENTER(Component.translatable("let_me_see_see.gui.align_center")),
    RIGHT(Component.translatable("let_me_see_see.gui.align_right"));
    
    public static final Codec<Align> CODEC = StringRepresentable.fromEnum(Align::values);
    public static final StreamCodec<ByteBuf,Align> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);
    
    private static final Align[] VALUES = Align.values();
    public final Component displayName;

    Align(Component displayName) {
        this.displayName = displayName;
    }

    public static Align byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal <= VALUES.length ? VALUES[ordinal] : CENTER;
    }
    
    @Override
    public String getSerializedName() {
        return name();
    }
        
}