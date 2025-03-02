package com.xkball.let_me_see_see.client;

import com.mojang.datafixers.util.Either;
import com.xkball.let_me_see_see.utils.GoogleTranslate;
import com.xkball.let_me_see_see.utils.LLMTranslate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

//@EventBusSubscriber(bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class ItemStackTooltipTranslator {
    
    public static final Map<String,String> translationMappings = new ConcurrentHashMap<>();
    public static final Set<String> translatedText = ConcurrentHashMap.newKeySet();
    private static final Style DARK_GRAY = Style.EMPTY.withColor(ChatFormatting.DARK_GRAY);
    @Nullable
    private static ItemStack track = null;
    
    public static void submit(ItemStack stack, GuiGraphics graphics) {
        if(stack.isEmpty()) return;
        track = stack;
        graphics.renderTooltip(Minecraft.getInstance().font, stack, 0, 0);
        track = null;
    }
    
    @SubscribeEvent
    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event){
        if(event.getItemStack().equals(track)) {
            submit(event.getTooltipElements());
        }
        var newTooltips = new ArrayList<Either<FormattedText, TooltipComponent>>();
        for(var either : event.getTooltipElements()) {
            either.ifLeft(text ->{
                        newTooltips.add(either);
                        var str = text.getString();
                        if(str.isEmpty()) return;
                        var translation = translationMappings.get(str);
                        if(translation != null) {
                            newTooltips.add(Either.left(FormattedText.of(translation, DARK_GRAY)));
                        }
            });
            either.ifRight(text -> newTooltips.add(either));
        }
        event.getTooltipElements().clear();
        event.getTooltipElements().addAll(newTooltips);
    }
    
    private static void submit(List<Either<FormattedText, TooltipComponent>> components){
        for(var either : components) {
            either.ifLeft(text -> {
                var str = text.getString();
                if(str.isEmpty()) return;
                if(translatedText.contains(str)) return;
                translationMappings.put(str, "翻译中...");
                LLMTranslate.translate(str,GoogleTranslate.ZN_CH).whenCompleteAsync((result, t) -> {
                    translationMappings.put(str, result);
                    translatedText.add(str);
                });
            });
        }
    }
}
