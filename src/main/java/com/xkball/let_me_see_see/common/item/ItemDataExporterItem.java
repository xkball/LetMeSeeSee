package com.xkball.let_me_see_see.common.item;

import com.xkball.let_me_see_see.client.offscreen.OffScreenRenders;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemDataExporterItem extends Item {
    
    public ItemDataExporterItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if(level.isClientSide){
            BuiltInRegistries.ITEM.stream().forEach(it -> OffScreenRenders.exportItemStackAsPng(it.getDefaultInstance(),128,128,1F));
            
        }
        return InteractionResultHolder.success(player.getItemInHand(usedHand));
    }
}
