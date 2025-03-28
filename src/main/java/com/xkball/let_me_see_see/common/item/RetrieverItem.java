package com.xkball.let_me_see_see.common.item;

import com.xkball.let_me_see_see.client.gui.screen.RetrieverScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.function.Supplier;

public class RetrieverItem extends GUIItem {
    
    public RetrieverItem(Properties properties) {
        super(properties);
    }
    
    @Override
    @OnlyIn(Dist.CLIENT)
    public Supplier<Screen> getScreenSupplier(ItemStack stack, EquipmentSlot slot) {
        return RetrieverScreen::new;
    }
    
}
