package com.xkball.let_me_see_see.test;

import io.netty.buffer.ByteBufAllocator;
import net.minecraft.network.codec.ByteBufCodecs;

public class CodecTest {
    
    public static void main(String[] args) {
        var buf = ByteBufAllocator.DEFAULT.buffer(8);
        ByteBufCodecs.VAR_INT.encode(buf, -1);
        for (var i = 0; i < buf.capacity(); i++) {
            System.out.println(buf.getByte(i));
        }
        
        Runnable runnable = () -> {
            System.out.println(111);
        };
    }
}
