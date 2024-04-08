package com.strangesmell.noguichest.util;

import com.strangesmell.noguichest.brewing.NGBrewingStandBlockEntity;
import com.strangesmell.noguichest.channel.Channel;
import com.strangesmell.noguichest.hopper.S2CMessageHopper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class Util {
    public static void setItem(int index, Player player, InteractionHand pHand, NGBrewingStandBlockEntity blockEntity ){

        ItemStack useItemStack = player.getItemInHand(pHand);
        ItemStack chestItemStack = blockEntity.getItems().get(index);

        if(useItemStack.isEmpty()&&chestItemStack.isEmpty()) return;

        if (!useItemStack.isEmpty()&&chestItemStack.isEmpty()) {
            blockEntity.getItems().set(index,useItemStack);
            player.setItemInHand(pHand,new ItemStack(Items.AIR));
            Channel.sendToChunk(new S2CMessageHopper(blockEntity.getItems(),blockEntity.getBlockPos()),blockEntity.getLevel().getChunkAt(blockEntity.getBlockPos()));
        }
        if (useItemStack.isEmpty()&&!chestItemStack.isEmpty()) {
            player.setItemInHand(pHand,chestItemStack);
            blockEntity.getItems().set(index,new ItemStack(Items.AIR));
            Channel.sendToChunk(new S2CMessageHopper(blockEntity.getItems(),blockEntity.getBlockPos()),blockEntity.getLevel().getChunkAt(blockEntity.getBlockPos()));
        }
        if (!useItemStack.isEmpty()&&!chestItemStack.isEmpty()) {
            blockEntity.getItems().set(index,useItemStack);
            player.setItemInHand(pHand, chestItemStack);
            Channel.sendToChunk(new S2CMessageHopper(blockEntity.getItems(),blockEntity.getBlockPos()),blockEntity.getLevel().getChunkAt(blockEntity.getBlockPos()));
        }

    }

}
