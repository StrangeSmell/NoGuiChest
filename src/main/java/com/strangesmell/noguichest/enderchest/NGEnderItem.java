package com.strangesmell.noguichest.enderchest;

import com.strangesmell.noguichest.NoGuiChest;
import com.strangesmell.noguichest.chest.NGChestEntity;
import com.strangesmell.noguichest.chest.NGChestItemStackRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class NGEnderItem extends BlockItem {
    public NGEnderItem(Block p_40565_, Properties p_40566_) {
        super(p_40565_, p_40566_);
    }
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);

        consumer.accept(new IClientItemExtensions() {
                            @Override
                            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                                Supplier<BlockEntity> modelToUse = ()->new NGEnderChestBlockEntity(BlockPos.ZERO, NoGuiChest.NGEnder.get().defaultBlockState());

                                return new NGChestItemStackRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels(), modelToUse);


                            }
                        }
        );
    }
}
