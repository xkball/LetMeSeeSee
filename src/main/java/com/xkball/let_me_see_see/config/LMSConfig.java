package com.xkball.let_me_see_see.config;

import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = LetMeSeeSee.MODID, bus = EventBusSubscriber.Bus.MOD)
public class LMSConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec.ConfigValue<String> IDEA_PATH_CONFIG = BUILDER.comment("The path of your Intellij IDEA (idea64.exe) (Or other program can open .class file)").define("idea_path", "");
    public static final ModConfigSpec SPEC = BUILDER.build();
    public static String IDEA_PATH;
    
    public static void update() {
        IDEA_PATH = IDEA_PATH_CONFIG.get();
    }
    
    @SubscribeEvent
    public static void onLoad(ModConfigEvent.Loading event) {
        update();
        ExportsDataManager.readRecordedClasses();
    }
    
    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        update();
    }
}
