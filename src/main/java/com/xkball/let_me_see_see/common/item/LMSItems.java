package com.xkball.let_me_see_see.common.item;

import com.xkball.let_me_see_see.LetMeSeeSee;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD,modid = LetMeSeeSee.MODID)
public class LMSItems {
    
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, LetMeSeeSee.MODID);
    
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LetMeSeeSee.MODID);
    
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THE_TAB = CREATIVE_MODE_TABS.register("tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.let_me_see_see"))
            .icon(Items.APPLE::getDefaultInstance)
            .withTabsBefore(CreativeModeTabs.FOOD_AND_DRINKS, CreativeModeTabs.INGREDIENTS, CreativeModeTabs.SPAWN_EGGS)
            .build());
    
    public static final DeferredHolder<Item,ScannerItem> SCANNER = ITEMS.register("scanner",() -> new ScannerItem(new Item.Properties().stacksTo(1)));
    
    public static void init(IEventBus eventBus){
        ITEMS.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }
    
    @SubscribeEvent
    public static void creativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == THE_TAB.get()) {
            event.accept(SCANNER.get());
        }
    }
}
