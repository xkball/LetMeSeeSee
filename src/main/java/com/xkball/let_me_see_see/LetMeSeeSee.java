package com.xkball.let_me_see_see;

import com.xkball.let_me_see_see.common.item.LMSItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = LetMeSeeSeeClient.MODID)
public class LetMeSeeSee {
    
    public LetMeSeeSee(IEventBus modEventBus, ModContainer modContainer) {
        LMSItems.init(modEventBus);

    }
}
