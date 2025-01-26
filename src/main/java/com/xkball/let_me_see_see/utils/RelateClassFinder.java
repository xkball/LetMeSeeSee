package com.xkball.let_me_see_see.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RelateClassFinder {
    
    public static List<Class<?>> analysisBlock(Block block) {
        var result = new ArrayList<Class<?>>();
        result.add(block.getClass());
        result.add(block.asItem().getClass());
        var bs = block.defaultBlockState();
        if(bs.hasBlockEntity()){
            var renderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
            var teTypeList = BuiltInRegistries.BLOCK_ENTITY_TYPE.entrySet().stream()
                    .map(Map.Entry::getValue)
                    .filter(type -> type.getValidBlocks().contains(block))
                    .toList();
            var teClassesList = teTypeList.stream()
                    .map(type -> type.create(BlockPos.ZERO,bs))
                    .filter(Objects::nonNull)
                    .map(Object::getClass)
                    .toList();
            var teRenderClassesList = teTypeList.stream()
                    .map(type -> renderDispatcher.renderers.get(type))
                    .filter(Objects::nonNull)
                    .map(Object::getClass)
                    .toList();
            result.addAll(teClassesList);
            result.addAll(teRenderClassesList);
        }
        return result;
    }
    
}
