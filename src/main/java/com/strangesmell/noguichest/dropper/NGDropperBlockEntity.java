package com.strangesmell.noguichest.dropper;

import com.strangesmell.noguichest.NoGuiChest;
import com.strangesmell.noguichest.channel.Channel;
import com.strangesmell.noguichest.dispenser.NGDispenserEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class NGDropperBlockEntity extends NGDispenserEntity {
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    public NGDropperBlockEntity(BlockPos p_155498_, BlockState p_155499_) {
        super(NoGuiChest.NGDropperEntity.get(), p_155498_, p_155499_);
    }
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected Component getDefaultName() {
        return Component.translatable("container.dropper");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        handleUpdateTag( pkt.getTag());
    }

    public void setItems(NonNullList<ItemStack> p_59243_) {
        this.items = p_59243_;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compoundTag = super.getUpdateTag();
        ContainerHelper.saveAllItems(compoundTag, this.items);
        return compoundTag;
    }
    @Override
    public void setChanged() {
        if (this.level != null) {
            setChanged(this.level, this.worldPosition, this.getBlockState());
            needSync();
        }
    }
    private void needSync(){
        assert this.level != null;
        if(level.isClientSide) return;
        //level.sendBlockUpdated(this.getBlockPos(),this.getBlockState(),this.getBlockState(),2);
        Channel.sendToChunk(new S2CMessageDropper(this.getItems(),this.getBlockPos()),this.getLevel().getChunkAt(this.getBlockPos()));
    }
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        ContainerHelper.loadAllItems(tag, this.items);
    }

    public ItemStack removeItem(int p_59613_, int p_59614_) {
        this.unpackLootTable((Player)null);
        ItemStack itemstack = ContainerHelper.removeItem(this.getItems(), p_59613_, p_59614_);
        if (!itemstack.isEmpty()) {
            this.setChanged();
            Channel.sendToChunk(new S2CMessageDropper(this.getItems(),this.getBlockPos()),this.getLevel().getChunkAt(this.getBlockPos()));
        }
        return itemstack;
    }
    public void load(CompoundTag p_155496_) {
        super.load(p_155496_);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(p_155496_)) {
            ContainerHelper.loadAllItems(p_155496_, this.items);
        }

    }

    public int getRandomSlot(RandomSource p_222762_) {
        this.unpackLootTable((Player)null);
        int i = -1;
        int j = 1;

        for(int k = 0; k < this.items.size(); ++k) {
            if (!this.items.get(k).isEmpty() && p_222762_.nextInt(j++) == 0) {
                i = k;
            }
        }

        return i;
    }

    protected void saveAdditional(CompoundTag p_187498_) {
        super.saveAdditional(p_187498_);
        if (!this.trySaveLootTable(p_187498_)) {
            ContainerHelper.saveAllItems(p_187498_, this.items);
        }

    }
}
