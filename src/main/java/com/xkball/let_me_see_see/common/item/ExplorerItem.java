package com.xkball.let_me_see_see.common.item;

import com.xkball.let_me_see_see.client.gui.screen.ExplorerScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ExplorerItem extends GUIItem{
    
    public ExplorerItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public Supplier<Screen> getScreenSupplier(ItemStack stack, EquipmentSlot slot) {
        return ExplorerScreen::new;
    }
}
