package com.xkball.let_me_see_see.network.server2client;

import com.xkball.let_me_see_see.common.item.IScreenProviderItem;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@MethodsReturnNonnullByDefault
//@NetworkPacket(type = NetworkPacket.Type.PLAY_SERVER_TO_CLIENT)
public record OpenItemScreen(ItemStack stack, EquipmentSlot slot) implements CustomPacketPayload {
    
    public static final Type<OpenItemScreen> TYPE = new Type<>(VanillaUtils.modRL("open_item_screen"));
    
    //@NetworkPacket.Codec
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenItemScreen> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            OpenItemScreen::stack,
            ByteBufCodecs.fromCodec(EquipmentSlot.CODEC),
            OpenItemScreen::slot,
            OpenItemScreen::new
    );
    
    //@NetworkPacket.Handler
    public void handle(IPayloadContext context) {
        context.enqueueWork(() -> {
            var mc = Minecraft.getInstance();
            if (stack.getItem() instanceof IScreenProviderItem screenProvider) {
                mc.setScreen(screenProvider.getScreenSupplier(stack, slot).get());
            }
        });
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
    
}
