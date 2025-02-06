package com.xkball.let_me_see_see.common.item;

import com.xkball.let_me_see_see.client.gui.screen.ItemDataExporterScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ItemDataExporterItem extends GUIItem {
    
    public ItemDataExporterItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public Supplier<Screen> getScreenSupplier(ItemStack stack, EquipmentSlot slot) {
        return ItemDataExporterScreen::new;
    }
    
}
