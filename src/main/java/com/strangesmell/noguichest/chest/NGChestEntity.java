package com.strangesmell.noguichest.chest;

import com.strangesmell.noguichest.channel.Channel;
import com.strangesmell.noguichest.NoGuiChest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.HashMap;
import java.util.Map;

public class NGChestEntity extends ChestBlockEntity implements LidBlockEntity {
    private boolean openState = false;
    public void difOpenStateSingle(){
        openState= !openState;
    }
    public void difOpenState(){
        ChestType chesttype = this.getBlockState().hasProperty(NGChest.TYPE) ? this.getBlockState().getValue(NGChest.TYPE) : ChestType.SINGLE;
        if(chesttype!=ChestType.SINGLE){
            switch (this.getBlockState().getValue(NGChest.FACING)){
                case EAST ->{
                    if(chesttype==ChestType.LEFT) difOpenState2(this,0,0,1);
                    else difOpenState2(this,0,0,-1);
                    break;
                }
                case WEST -> {
                    if(chesttype==ChestType.LEFT) difOpenState2(this,0,0,-1);
                    else difOpenState2(this,0,0,1);
                    break;
                }
                case SOUTH -> {
                    if(chesttype==ChestType.LEFT) difOpenState2(this,-1,0,0);
                    else difOpenState2(this,1,0,0);
                    break;
                }
                case NORTH -> {
                    if(chesttype==ChestType.LEFT) difOpenState2(this,1,0,0);
                    else difOpenState2(this,-1,0,0);
                    break;
                }
                default -> {}
            }
        }
        openState= !openState;
    }

    public void difOpenState2(NGChestEntity ngChestEntity,int dx,int dy,int dz){
        assert ngChestEntity.level != null;
        ((NGChestEntity)(ngChestEntity.level.getBlockEntity(ngChestEntity.getBlockPos().offset(dx,dy,dz)))).difOpenStateSingle();
    }

    public void setOpenState(boolean openState){
        this.openState=openState;
    }
    public boolean getOpenState(){
        return this.openState;
    }
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);

    private final NGChestContainerOpenersCounter openersCounter = new NGChestContainerOpenersCounter() {
        protected void onOpen(Level p_155357_, BlockPos p_155358_, BlockState p_155359_) {
            NGChestEntity.playSound(p_155357_, p_155358_, p_155359_, SoundEvents.CHEST_OPEN);
        }

        protected void onClose(Level p_155367_, BlockPos p_155368_, BlockState p_155369_) {
            NGChestEntity.playSound(p_155367_, p_155368_, p_155369_, SoundEvents.CHEST_CLOSE);
        }

        protected void openerCountChanged(Level p_155361_, BlockPos p_155362_, BlockState p_155363_, int p_155364_, int p_155365_) {
            NGChestEntity.this.signalOpenCount(p_155361_, p_155362_, p_155363_, p_155364_, p_155365_);
        }

        protected boolean isOwnContainer(Player p_155355_) {
            if (!(p_155355_.containerMenu instanceof ChestMenu)) {
                return false;
            } else {
                Container container = ((ChestMenu)p_155355_.containerMenu).getContainer();
                return container == NGChestEntity.this || container instanceof CompoundContainer && ((CompoundContainer)container).contains(NGChestEntity.this);
            }
        }
    };

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
        Channel.sendToChunk(new S2CMessage(this.getItems(),this.getBlockPos(), this.getOpenState()),this.getLevel().getChunkAt(this.getBlockPos()));
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public void startOpen(Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.incrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
            this.getUpdateTag();
            this.getUpdatePacket();
        }
    }


    public void stopOpen(Player pPlayer) {
        if (!this.remove && !pPlayer.isSpectator()) {
            this.openersCounter.decrementOpeners(pPlayer, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    @Override
    public ItemStack removeItem(int pIndex, int pCount) {
        this.unpackLootTable((Player)null);
        ItemStack itemstack = ContainerHelper.removeItem(this.getItems(), pIndex, pCount);
        if (!itemstack.isEmpty()) {
            this.setChanged();
        }
        this.setChanged();
        //Channel.sendToChunk(new S2CMassage(this.getItems(),this.getBlockPos(), this.getOpenState()),this.getLevel().getChunkAt(this.getBlockPos()));
        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int pIndex) {
        this.unpackLootTable((Player)null);
        return ContainerHelper.takeItem(this.getItems(), pIndex);
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

    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.items.clear();
        ContainerHelper.loadAllItems(pTag, this.items);
    }

    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        ContainerHelper.saveAllItems(pTag, this.items);
        needSync();
    }

    @Override
    public void setBlockState(BlockState pBlockState) {
         
        super.setBlockState(pBlockState);
        if (this.chestHandler != null) {
            net.minecraftforge.common.util.LazyOptional<?> oldHandler = this.chestHandler;
            this.chestHandler = null;
            oldHandler.invalidate();
        }
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
    public NGChestEntity(BlockPos pPos, BlockState pBlockState) {
        super(NoGuiChest.NGChestEntity.get(), pPos, pBlockState);
        for(int index = 0; index < 25 ; index++){
            indexCount.put(index,0f);
        }
    }
    protected Component getDefaultName() {
        NonNullList<ItemStack> items=null;
        if(!this.level.isClientSide){
             items = this.getItems();
        }
        if(level.isClientSide) {
            this.setItems(items);
        }
        return Component.translatable("container.NGChest");

    }

    static void playSound(Level pLevel, BlockPos pPos, BlockState pState, SoundEvent pSound) {
        ChestType chesttype = pState.getValue(ChestBlock.TYPE);
        if (chesttype != ChestType.LEFT) {
            double d0 = (double)pPos.getX() + 0.5D;
            double d1 = (double)pPos.getY() + 0.5D;
            double d2 = (double)pPos.getZ() + 0.5D;
            if (chesttype == ChestType.RIGHT) {
                Direction direction = ChestBlock.getConnectedDirection(pState);
                d0 += (double)direction.getStepX() * 0.5D;
                d2 += (double)direction.getStepZ() * 0.5D;
            }

            pLevel.playSound((Player)null, d0, d1, d2, pSound, SoundSource.BLOCKS, 0.5F, pLevel.random.nextFloat() * 0.1F + 0.9F);
        }
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public void setItems(NonNullList<ItemStack> pItems) {
        this.items = pItems;
    }

    protected AbstractContainerMenu createMenu(int pId, Inventory pPlayer) {
        return ChestMenu.threeRows(pId, pPlayer, this);
    }
    private net.minecraftforge.common.util.LazyOptional<net.minecraftforge.items.IItemHandlerModifiable> chestHandler;

    private net.minecraftforge.items.IItemHandlerModifiable createHandler() {
        BlockState state = this.getBlockState();
        if (!(state.getBlock() instanceof NGChest)) {
            return new net.minecraftforge.items.wrapper.InvWrapper(this);
        }
        Container inv = ChestBlock.getContainer((NGChest) state.getBlock(), state, getLevel(), getBlockPos(), true);
        return new net.minecraftforge.items.wrapper.InvWrapper(inv == null ? this : inv);
    }


    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, Direction side) {
        if (!this.remove && cap == net.minecraftforge.common.capabilities.ForgeCapabilities.ITEM_HANDLER) {
            if (this.chestHandler == null)
                this.chestHandler = net.minecraftforge.common.util.LazyOptional.of(this::createHandler);
            return this.chestHandler.cast();
        }
        return super.getCapability(cap, side);
    }


}
