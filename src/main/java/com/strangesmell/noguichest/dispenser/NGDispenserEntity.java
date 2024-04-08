package com.strangesmell.noguichest.dispenser;

import com.strangesmell.noguichest.NoGuiChest;
import com.strangesmell.noguichest.channel.Channel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DispenserMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class NGDispenserEntity extends DispenserBlockEntity {
    public static final int CONTAINER_SIZE = 9;
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    public NGDispenserEntity(BlockPos p_155490_, BlockState p_155491_) {
        super(NoGuiChest.NGDispenserEntity.get(), p_155490_, p_155491_);
    }

    public NGDispenserEntity(BlockEntityType<?> p_155489_, BlockPos p_155490_, BlockState p_155491_) {
        super(p_155489_, p_155490_, p_155491_);
    }

    public int getContainerSize() {
        return 9;
    }

    public ItemStack removeItem(int p_59613_, int p_59614_) {
        this.unpackLootTable((Player)null);
        ItemStack itemstack = ContainerHelper.removeItem(this.getItems(), p_59613_, p_59614_);
        if (!itemstack.isEmpty()) {
            this.setChanged();
            Channel.sendToChunk(new S2CMessageDispenser(this.getItems(),this.getBlockPos()),this.getLevel().getChunkAt(this.getBlockPos()));
        }

        return itemstack;
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
        Channel.sendToChunk(new S2CMessageDispenser(this.getItems(),this.getBlockPos()),this.getLevel().getChunkAt(this.getBlockPos()));
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

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compoundTag = super.getUpdateTag();
        ContainerHelper.saveAllItems(compoundTag, this.items);
        return compoundTag;
    }
    @Override
    public void handleUpdateTag(CompoundTag tag) {
        ContainerHelper.loadAllItems(tag, this.items);
    }

    public int getRandomSlot(RandomSource p_222762_) {
        this.unpackLootTable((Player)null);
        int i = -1;
        int j = 1;

        for(int k = 0; k < this.items.size(); ++k) {
            if (!this.getItem(k).isEmpty() && p_222762_.nextInt(j++) == 0) {
                i = k;
            }
        }

        return i;
    }

    public int addItem(ItemStack p_59238_) {
        for(int i = 0; i < this.items.size(); ++i) {
            if (this.items.get(i).isEmpty()) {
                this.setItem(i, p_59238_);
                return i;
            }
        }

        return -1;
    }

    protected Component getDefaultName() {
        return Component.translatable("container.dispenser");
    }

    public void load(CompoundTag p_155496_) {
        super.load(p_155496_);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(p_155496_)) {
            ContainerHelper.loadAllItems(p_155496_, this.items);
        }

    }

    protected void saveAdditional(CompoundTag p_187498_) {
        super.saveAdditional(p_187498_);
        if (!this.trySaveLootTable(p_187498_)) {
            ContainerHelper.saveAllItems(p_187498_, this.items);
        }

    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public void setItems(NonNullList<ItemStack> p_59243_) {
        this.items = p_59243_;
    }

    protected AbstractContainerMenu createMenu(int p_59235_, Inventory p_59236_) {
        return new DispenserMenu(p_59235_, p_59236_, this);
    }
}
