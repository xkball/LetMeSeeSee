package com.xkball.let_me_see_see.common.item;

import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.utils.RelateClassFinder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

public class ScannerItem extends Item {
    
    public ScannerItem(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult useOn(UseOnContext context) {
        var level = context.getLevel();
        //涉及渲染器类 必须在客户端执行
        if (level.isClientSide()){
            var pos = context.getClickedPos();
            var bs = level.getBlockState(pos);
            var block = bs.getBlock();
            LetMeSeeSee.scanClasses(RelateClassFinder.analysisBlock(block));
        }
        return InteractionResult.SUCCESS;
    }
}
