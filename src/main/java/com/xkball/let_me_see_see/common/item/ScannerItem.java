package com.xkball.let_me_see_see.common.item;

import com.xkball.let_me_see_see.LetMeSeeSeeClient;
import com.xkball.let_me_see_see.utils.RelateClassFinder;
import com.xkball.xklibmc.annotation.NonNullByDefault;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

@NonNullByDefault
public class ScannerItem extends Item {

    public ScannerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        //涉及渲染器类 必须在客户端执行
        if (level.isClientSide()) {
            var pos = context.getClickedPos();
            var bs = level.getBlockState(pos);
            var block = bs.getBlock();
            LetMeSeeSeeClient.scanClasses(RelateClassFinder.analysisBlock(block));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand usedHand) {
        var level = player.level();
        if (level.isClientSide()) {
            LetMeSeeSeeClient.scanClasses(RelateClassFinder.analysisEntity(interactionTarget));
        }
        return InteractionResult.SUCCESS;
    }
}
