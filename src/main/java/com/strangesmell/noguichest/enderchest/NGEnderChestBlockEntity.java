package com.strangesmell.noguichest.enderchest;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public class NGEnderChestBlockEntity extends EnderChestBlockEntity{
    private final ChestLidController chestLidController = new ChestLidController();
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        protected void onOpen(Level p_155531_, BlockPos p_155532_, BlockState p_155533_) {
            p_155531_.playSound((Player)null, (double)p_155532_.getX() + 0.5D, (double)p_155532_.getY() + 0.5D, (double)p_155532_.getZ() + 0.5D, SoundEvents.ENDER_CHEST_OPEN, SoundSource.BLOCKS, 0.5F, p_155531_.random.nextFloat() * 0.1F + 0.9F);
        }

        protected void onClose(Level p_155541_, BlockPos p_155542_, BlockState p_155543_) {
            p_155541_.playSound((Player)null, (double)p_155542_.getX() + 0.5D, (double)p_155542_.getY() + 0.5D, (double)p_155542_.getZ() + 0.5D, SoundEvents.ENDER_CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, p_155541_.random.nextFloat() * 0.1F + 0.9F);
        }

        protected void openerCountChanged(Level p_155535_, BlockPos p_155536_, BlockState p_155537_, int p_155538_, int p_155539_) {
            p_155535_.blockEvent(NGEnderChestBlockEntity.this.worldPosition, Blocks.ENDER_CHEST, 1, p_155539_);
        }

        protected boolean isOwnContainer(Player p_155529_) {
            return p_155529_.getEnderChestInventory().isActiveChest(NGEnderChestBlockEntity.this);
        }
    };

    public NGEnderChestBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super( p_155229_, p_155230_);
    }


    public static void lidAnimateTick(Level p_155518_, BlockPos p_155519_, BlockState p_155520_, NGEnderChestBlockEntity p_155521_) {
        p_155521_.chestLidController.tickLid();
    }

    public boolean triggerEvent(int p_59285_, int p_59286_) {
        if (p_59285_ == 1) {
            this.chestLidController.shouldBeOpen(p_59286_ > 0);
            return true;
        } else {
            return super.triggerEvent(p_59285_, p_59286_);
        }
    }

    public void startOpen(Player p_155516_) {
        if (!this.remove && !p_155516_.isSpectator()) {
            this.openersCounter.incrementOpeners(p_155516_, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public void stopOpen(Player p_155523_) {
        if (!this.remove && !p_155523_.isSpectator()) {
            this.openersCounter.decrementOpeners(p_155523_, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public boolean stillValid(Player p_59283_) {
        return Container.stillValidBlockEntity(this, p_59283_);
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public float getOpenNess(float p_59281_) {
        return this.chestLidController.getOpenness(p_59281_);
    }

    public Map<Integer, Float> indexCount = new HashMap<Integer, Float>();
    public final float maxIndexCount = 30;
    public void increaseIndexCount(int index){
        if(indexCount.get(index)>=maxIndexCount) return;
        indexCount.put(index, indexCount.get(index)+1);
    }
    public void decreaseIndexCount(int index){
        if (indexCount.get(index)<=0) return;
        indexCount.put(index, indexCount.get(index)-1);
    }

}
