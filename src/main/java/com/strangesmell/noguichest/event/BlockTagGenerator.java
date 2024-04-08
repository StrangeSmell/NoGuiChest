package com.strangesmell.noguichest.event;

import com.strangesmell.noguichest.NoGuiChest;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends IntrinsicHolderTagsProvider<Block> {
    public BlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> future, ExistingFileHelper helper) {
        super(output, Registries.BLOCK, future, block -> block.builtInRegistryHolder().key(), NoGuiChest.MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider p_256380_) {
        tag(BlockTags.MINEABLE_WITH_AXE).add(NoGuiChest.NGChest.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NoGuiChest.NGEnder.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NoGuiChest.NGHopper.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NoGuiChest.NGBrew.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NoGuiChest.NGDispenser.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(NoGuiChest.NGDropper.get());
    }
}
