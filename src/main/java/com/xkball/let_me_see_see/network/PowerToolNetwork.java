package com.xkball.let_me_see_see.network;

import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.network.server2client.OpenItemScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = LetMeSeeSee.MODID,bus = EventBusSubscriber.Bus.MOD)
public class PowerToolNetwork {

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        var register = event.registrar(LetMeSeeSee.MODID);
        register.playToClient(
                OpenItemScreen.TYPE,
                OpenItemScreen.STREAM_CODEC,
                OpenItemScreen::handle
        );

    }
    
}
