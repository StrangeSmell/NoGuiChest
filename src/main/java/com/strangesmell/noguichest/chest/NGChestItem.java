package com.strangesmell.noguichest.chest;

import com.strangesmell.noguichest.NoGuiChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NGChestItem extends BlockItem {
    public NGChestItem(Block p_40565_, Properties p_40566_) {
        super(p_40565_, p_40566_);
    }


    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);

        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                Supplier<BlockEntity> modelToUse = ()->new NGChestEntity(BlockPos.ZERO, NoGuiChest.NGChest.get().defaultBlockState());

                return new NGChestItemStackRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels(), modelToUse);


            }
        }
        );
    }

}
