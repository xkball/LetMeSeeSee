package com.xkball.let_me_see_see.datagen;

import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.common.item.LMSItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.PackOutput;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ModItemModelProvider extends ModelProvider {
    
    public ModItemModelProvider(PackOutput output) {
        super(output, LetMeSeeSee.MODID);
    }
    
    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        itemModels.declareCustomModelItem(LMSItems.DATA_BASE.get());
        itemModels.declareCustomModelItem(LMSItems.RETRIEVER.get());
        itemModels.declareCustomModelItem(LMSItems.SCANNER.get());
    }
}
