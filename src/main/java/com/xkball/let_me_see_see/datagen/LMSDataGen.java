package com.xkball.let_me_see_see.datagen;

import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.xorlib.api.annotation.DataGenProvider;
import com.xkball.xorlib.api.annotation.SubscribeEventEnhanced;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = LetMeSeeSee.MODID, bus = EventBusSubscriber.Bus.MOD)
public class LMSDataGen {
    
    @SubscribeEvent
    //@SubscribeEventEnhanced
    public static void gatherData(GatherDataEvent.Client event) {
        var dataGenerator = event.getGenerator();
        var packOutput = dataGenerator.getPackOutput();
        event.addProvider(new ModItemModelProvider(packOutput));
    }
}
