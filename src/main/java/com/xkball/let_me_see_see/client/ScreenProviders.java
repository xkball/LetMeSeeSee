package com.xkball.let_me_see_see.client;

import com.xkball.let_me_see_see.LetMeSeeSeeClient;
import com.xkball.let_me_see_see.client.gui.screen.AgentNotAvailableScreen;
import com.xkball.let_me_see_see.client.gui.screen.ExplorerScreen;
import com.xkball.let_me_see_see.client.gui.screen.ItemDataExporterScreen;
import com.xkball.let_me_see_see.common.item.LMSItems;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ScreenProviders {

    public static final Map<Identifier, IScreenProviderItemClient> PROVIDERS = new HashMap<>();

    public static void init() {
        PROVIDERS.put(LMSItems.EXPLORER.getId(), (stack, slot) -> {
            if (LetMeSeeSeeClient.isAgentAvailable()) {
                return new ExplorerScreen();
            }
            return new AgentNotAvailableScreen();
        });
        PROVIDERS.put(LMSItems.ITEM_ITEM_DATA_EXPORTER.getId(), (stack, slot) -> new ItemDataExporterScreen());
    }

    public interface IScreenProviderItemClient {
        Screen createScreen(ItemStack stack, EquipmentSlot slot);
    }
}
