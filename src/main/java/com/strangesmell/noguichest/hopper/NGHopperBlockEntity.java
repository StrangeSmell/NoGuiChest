package com.strangesmell.noguichest.hopper;

import com.strangesmell.noguichest.NoGuiChest;
import com.strangesmell.noguichest.channel.Channel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.HopperMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NGHopperBlockEntity extends RandomizableContainerBlockEntity implements Hopper{
    public static final int MOVE_ITEM_SPEED = 8;
    public static final int HOPPER_CONTAINER_SIZE = 5;
    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int cooldownTime = -1;
    private long tickedGameTime;

    public NGHopperBlockEntity(BlockPos p_155550_, BlockState p_155551_) {
        super(NoGuiChest.NGHopperEntity.get(),p_155550_, p_155551_);
    }

    public void load(CompoundTag p_155588_) {
        super.load(p_155588_);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(p_155588_)) {
            ContainerHelper.loadAllItems(p_155588_, this.items);
        }

        this.cooldownTime = p_155588_.getInt("NGTransferCooldown");
    }

    protected void saveAdditional(CompoundTag p_187502_) {
        super.saveAdditional(p_187502_);
        if (!this.trySaveLootTable(p_187502_)) {
            ContainerHelper.saveAllItems(p_187502_, this.items);
        }
        p_187502_.putInt("NGTransferCooldown", this.cooldownTime);
    }

    public int getContainerSize() {
        return this.items.size();
    }

    public ItemStack removeItem(int p_59309_, int p_59310_) {
        this.unpackLootTable((Player)null);
        return ContainerHelper.removeItem(this.getItems(), p_59309_, p_59310_);
    }

    public void setItem(int p_59315_, ItemStack p_59316_) {
        this.unpackLootTable((Player)null);
        this.getItems().set(p_59315_, p_59316_);
        if (p_59316_.getCount() > this.getMaxStackSize()) {
            p_59316_.setCount(this.getMaxStackSize());
        }

    }
    protected Component getDefaultName() {
        return Component.translatable("container.NGHopper");
    }

    public static void pushItemsTick(Level p_155574_, BlockPos p_155575_, BlockState p_155576_, NGHopperBlockEntity p_155577_) {
        --p_155577_.cooldownTime;
        p_155577_.tickedGameTime = p_155574_.getGameTime();
        if (!p_155577_.isOnCooldown()) {
            p_155577_.setCooldown(0);
            tryMoveItems(p_155574_, p_155575_, p_155576_, p_155577_, () -> {
                return suckInItems(p_155574_, p_155577_);
            });
        }

    }


    private static boolean tryMoveItems(Level p_155579_, BlockPos p_155580_, BlockState p_155581_, NGHopperBlockEntity p_155582_, BooleanSupplier p_155583_) {
        if (p_155579_.isClientSide) {
            return false;
        } else {
            if (!p_155582_.isOnCooldown() && p_155581_.getValue(NGHopper.ENABLED)) {
                boolean flag = false;
                if (!p_155582_.isEmpty()) {
                    flag = ejectItems(p_155579_, p_155580_, p_155581_, p_155582_);
                }

                if (!p_155582_.inventoryFull()) {
                    flag |= p_155583_.getAsBoolean();
                }

                if (flag) {
                    p_155582_.setCooldown(8);
                    setChanged(p_155579_, p_155580_, p_155581_);
                    return true;
                }
            }

            return false;
        }
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
        level.sendBlockUpdated(this.getBlockPos(),this.getBlockState(),this.getBlockState(),2);
        //Channel.sendToChunk(new S2CMessageHopper(this.getItems(),this.getBlockPos()),this.getLevel().getChunkAt(this.getBlockPos()));
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

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt)
    {
        handleUpdateTag( pkt.getTag());
    }

    private static boolean isFull(IItemHandler itemHandler)
    {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
            if (stackInSlot.isEmpty() || stackInSlot.getCount() < itemHandler.getSlotLimit(slot))
            {
                return false;
            }
        }
        return true;
    }
    private static boolean isEmpty(IItemHandler itemHandler)
    {
        for (int slot = 0; slot < itemHandler.getSlots(); slot++)
        {
            ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
            if (stackInSlot.getCount() > 0)
            {
                return false;
            }
        }
        return true;
    }
    private static ItemStack insertStack(BlockEntity source, Object destination, IItemHandler destInventory, ItemStack stack, int slot)
    {
        ItemStack itemstack = destInventory.getStackInSlot(slot);

        if (destInventory.insertItem(slot, stack, true).isEmpty())
        {
            boolean insertedItem = false;
            boolean inventoryWasEmpty = isEmpty(destInventory);

            if (itemstack.isEmpty())
            {
                destInventory.insertItem(slot, stack, false);
                stack = ItemStack.EMPTY;
                insertedItem = true;
            }
            else if (ItemHandlerHelper.canItemStacksStack(itemstack, stack))
            {
                int originalSize = stack.getCount();
                stack = destInventory.insertItem(slot, stack, false);
                insertedItem = originalSize < stack.getCount();
            }

            if (insertedItem)
            {
                if (inventoryWasEmpty && destination instanceof HopperBlockEntity)
                {
                    HopperBlockEntity destinationHopper = (HopperBlockEntity)destination;

                    if (!destinationHopper.isOnCustomCooldown())
                    {
                        int k = 0;
                        if (source instanceof HopperBlockEntity)
                        {
                            if (destinationHopper.getLastUpdateTime() >= ((HopperBlockEntity) source).getLastUpdateTime())
                            {
                                k = 1;
                            }
                        }
                        destinationHopper.setCooldown(8 - k);
                    }
                }
            }
        }

        return stack;
    }
    private static ItemStack putStackInInventoryAllSlots(BlockEntity source, Object destination, IItemHandler destInventory, ItemStack stack)
    {
        for (int slot = 0; slot < destInventory.getSlots() && !stack.isEmpty(); slot++)
        {
            stack = insertStack(source, destination, destInventory, stack, slot);
        }
        return stack;
    }
    public static boolean insertHook(NGHopperBlockEntity hopper)
    {
        Direction hopperFacing = hopper.getBlockState().getValue(NGHopper.FACING);
        return getItemHandler(hopper.getLevel(), hopper, hopperFacing)
                .map(destinationResult -> {
                    IItemHandler itemHandler = destinationResult.getKey();
                    Object destination = destinationResult.getValue();
                    if (isFull(itemHandler))
                    {
                        return false;
                    }
                    else
                    {
                        for (int i = 0; i < hopper.getContainerSize(); ++i)
                        {
                            if (!hopper.getItem(i).isEmpty())
                            {
                                ItemStack originalSlotContents = hopper.getItem(i).copy();
                                ItemStack insertStack = hopper.removeItem(i, 1);
                                ItemStack remainder = putStackInInventoryAllSlots(hopper, destination, itemHandler, insertStack);

                                if (remainder.isEmpty())
                                {
                                    Channel.sendToChunk(new S2CMessageHopper(hopper.getItems(),hopper.getBlockPos()),hopper.getLevel().getChunkAt(hopper.getBlockPos()));
                                    return true;
                                }

                                hopper.setItem(i, originalSlotContents);
                            }
                        }

                        return false;
                    }
                })
                .orElse(false);
    }

    private static boolean ejectItems(Level p_155563_, BlockPos p_155564_, BlockState p_155565_, NGHopperBlockEntity p_155566_) {
        if (insertHook(p_155566_)) return true;
        Container container = getAttachedContainer(p_155563_, p_155564_, p_155565_);
        if (container == null) {
            return false;
        } else {
            Direction direction = p_155565_.getValue(NGHopper.FACING).getOpposite();
            if (isFullContainer(container, direction)) {
                return false;
            } else {
                for(int i = 0; i < p_155566_.getContainerSize(); ++i) {
                    if (!p_155566_.getItem(i).isEmpty()) {
                        ItemStack itemstack = p_155566_.getItem(i).copy();
                        ItemStack itemstack1 = addItem(p_155566_, container, p_155566_.removeItem(i, 1), direction);
                        if (itemstack1.isEmpty()) {
                            container.setChanged();
                            return true;
                        }

                        p_155566_.setItem(i, itemstack);

                    }
                }

                return false;
            }
        }
    }

    private boolean inventoryFull() {
        for(ItemStack itemstack : this.items) {
            if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize()) {
                return false;
            }
        }

        return true;
    }

    private static IntStream getSlots(Container p_59340_, Direction p_59341_) {
        return p_59340_ instanceof WorldlyContainer ? IntStream.of(((WorldlyContainer)p_59340_).getSlotsForFace(p_59341_)) : IntStream.range(0, p_59340_.getContainerSize());
    }

    private static boolean isFullContainer(Container p_59386_, Direction p_59387_) {
        return getSlots(p_59386_, p_59387_).allMatch((p_59379_) -> {
            ItemStack itemstack = p_59386_.getItem(p_59379_);
            return itemstack.getCount() >= itemstack.getMaxStackSize();
        });
    }

    private static boolean isEmptyContainer(Container p_59398_, Direction p_59399_) {
        return getSlots(p_59398_, p_59399_).allMatch((p_59319_) -> {
            return p_59398_.getItem(p_59319_).isEmpty();
        });
    }

    public static Optional<Pair<IItemHandler, Object>> getItemHandler(Level worldIn, double x, double y, double z, final Direction side)
    {
        int i = Mth.floor(x);
        int j = Mth.floor(y);
        int k = Mth.floor(z);
        BlockPos blockpos = new BlockPos(i, j, k);
        net.minecraft.world.level.block.state.BlockState state = worldIn.getBlockState(blockpos);

        if (state.hasBlockEntity())
        {
            BlockEntity blockEntity = worldIn.getBlockEntity(blockpos);
            if (blockEntity != null)
            {
                return blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, side)
                        .map(capability -> ImmutablePair.<IItemHandler, Object>of(capability, blockEntity));
            }
        }

        return Optional.empty();
    }

    private static Optional<Pair<IItemHandler, Object>> getItemHandler(Level level, Hopper hopper, Direction hopperFacing)
    {
        double x = hopper.getLevelX() + (double) hopperFacing.getStepX();
        double y = hopper.getLevelY() + (double) hopperFacing.getStepY();
        double z = hopper.getLevelZ() + (double) hopperFacing.getStepZ();
        return getItemHandler(level, x, y, z, hopperFacing.getOpposite());
    }

    public static Boolean extractHook(Level level, Hopper dest)
    {
        return getItemHandler(level, dest, Direction.UP)
                .map(itemHandlerResult -> {
                    IItemHandler handler = itemHandlerResult.getKey();

                    for (int i = 0; i < handler.getSlots(); i++)
                    {
                        ItemStack extractItem = handler.extractItem(i, 1, true);
                        if (!extractItem.isEmpty())
                        {
                            for (int j = 0; j < dest.getContainerSize(); j++)
                            {
                                ItemStack destStack = dest.getItem(j);
                                if (dest.canPlaceItem(j, extractItem) && (destStack.isEmpty() || destStack.getCount() < destStack.getMaxStackSize() && destStack.getCount() < dest.getMaxStackSize() && ItemHandlerHelper.canItemStacksStack(extractItem, destStack)))
                                {
                                    extractItem = handler.extractItem(i, 1, false);
                                    if (destStack.isEmpty())
                                        dest.setItem(j, extractItem);
                                    else
                                    {
                                        destStack.grow(1);
                                        dest.setItem(j, destStack);
                                    }
                                    dest.setChanged();
                                    return true;
                                }
                            }
                        }
                    }

                    return false;
                })
                .orElse(null); // TODO bad null
    }

    public static boolean suckInItems(Level p_155553_, Hopper p_155554_) {
        Boolean ret = extractHook(p_155553_, p_155554_);
        if (ret != null) return ret;
        Container container = getSourceContainer(p_155553_, p_155554_);
        if (container != null) {
            Direction direction = Direction.DOWN;
            return !isEmptyContainer(container, direction) && getSlots(container, direction).anyMatch((p_59363_) -> {
                return tryTakeInItemFromSlot(p_155554_, container, p_59363_, direction);
            });
        } else {
            for(ItemEntity itementity : getItemsAtAndAbove(p_155553_, p_155554_)) {
                if (addItem(p_155554_, itementity)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean tryTakeInItemFromSlot(Hopper p_59355_, Container p_59356_, int p_59357_, Direction p_59358_) {
        ItemStack itemstack = p_59356_.getItem(p_59357_);
        if (!itemstack.isEmpty() && canTakeItemFromContainer(p_59355_, p_59356_, itemstack, p_59357_, p_59358_)) {
            ItemStack itemstack1 = itemstack.copy();
            ItemStack itemstack2 = addItem(p_59356_, p_59355_, p_59356_.removeItem(p_59357_, 1), (Direction)null);
            if (itemstack2.isEmpty()) {
                p_59356_.setChanged();
                return true;
            }

            p_59356_.setItem(p_59357_, itemstack1);
        }

        return false;
    }

    public static boolean addItem(Container p_59332_, ItemEntity p_59333_) {
        boolean flag = false;
        ItemStack itemstack = p_59333_.getItem().copy();
        ItemStack itemstack1 = addItem((Container)null, p_59332_, itemstack, (Direction)null);
        if (itemstack1.isEmpty()) {
            flag = true;
            p_59333_.discard();
        } else {
            p_59333_.setItem(itemstack1);
        }

        return flag;
    }

    public static ItemStack addItem(@Nullable Container p_59327_, Container p_59328_, ItemStack p_59329_, @Nullable Direction p_59330_) {
        if (p_59328_ instanceof WorldlyContainer worldlycontainer) {
            if (p_59330_ != null) {
                int[] aint = worldlycontainer.getSlotsForFace(p_59330_);

                for(int k = 0; k < aint.length && !p_59329_.isEmpty(); ++k) {
                    p_59329_ = tryMoveInItem(p_59327_, p_59328_, p_59329_, aint[k], p_59330_);
                }

                return p_59329_;
            }
        }

        int i = p_59328_.getContainerSize();

        for(int j = 0; j < i && !p_59329_.isEmpty(); ++j) {
            p_59329_ = tryMoveInItem(p_59327_, p_59328_, p_59329_, j, p_59330_);
        }

        return p_59329_;
    }

    private static boolean canPlaceItemInContainer(Container p_59335_, ItemStack p_59336_, int p_59337_, @Nullable Direction p_59338_) {
        if (!p_59335_.canPlaceItem(p_59337_, p_59336_)) {
            return false;
        } else {
            if (p_59335_ instanceof WorldlyContainer) {
                WorldlyContainer worldlycontainer = (WorldlyContainer)p_59335_;
                if (!worldlycontainer.canPlaceItemThroughFace(p_59337_, p_59336_, p_59338_)) {
                    return false;
                }
            }

            return true;
        }
    }

    private static boolean canTakeItemFromContainer(Container p_273433_, Container p_273542_, ItemStack p_273400_, int p_273519_, Direction p_273088_) {
        if (!p_273542_.canTakeItem(p_273433_, p_273519_, p_273400_)) {
            return false;
        } else {
            if (p_273542_ instanceof WorldlyContainer) {
                WorldlyContainer worldlycontainer = (WorldlyContainer)p_273542_;
                if (!worldlycontainer.canTakeItemThroughFace(p_273519_, p_273400_, p_273088_)) {
                    return false;
                }
            }

            return true;
        }
    }

    private static ItemStack tryMoveInItem(@Nullable Container p_59321_, Container p_59322_, ItemStack p_59323_, int p_59324_, @Nullable Direction p_59325_) {
        ItemStack itemstack = p_59322_.getItem(p_59324_);
        if (canPlaceItemInContainer(p_59322_, p_59323_, p_59324_, p_59325_)) {
            boolean flag = false;
            boolean flag1 = p_59322_.isEmpty();
            if (itemstack.isEmpty()) {
                p_59322_.setItem(p_59324_, p_59323_);
                p_59323_ = ItemStack.EMPTY;
                flag = true;
            } else if (canMergeItems(itemstack, p_59323_)) {
                int i = p_59323_.getMaxStackSize() - itemstack.getCount();
                int j = Math.min(p_59323_.getCount(), i);
                p_59323_.shrink(j);
                itemstack.grow(j);
                flag = j > 0;
            }

            if (flag) {
                if (flag1 && p_59322_ instanceof NGHopperBlockEntity) {
                    NGHopperBlockEntity hopperblockentity1 = (NGHopperBlockEntity)p_59322_;
                    if (!hopperblockentity1.isOnCustomCooldown()) {
                        int k = 0;
                        if (p_59321_ instanceof NGHopperBlockEntity) {
                            NGHopperBlockEntity hopperblockentity = (NGHopperBlockEntity)p_59321_;
                            if (hopperblockentity1.tickedGameTime >= hopperblockentity.tickedGameTime) {
                                k = 1;
                            }
                        }

                        hopperblockentity1.setCooldown(8 - k);
                    }
                }

                p_59322_.setChanged();
            }
        }

        return p_59323_;
    }

    @Nullable
    private static Container getAttachedContainer(Level p_155593_, BlockPos p_155594_, BlockState p_155595_) {
        Direction direction = p_155595_.getValue(NGHopper.FACING);
        return getContainerAt(p_155593_, p_155594_.relative(direction));
    }

    @Nullable
    private static Container getSourceContainer(Level p_155597_, Hopper p_155598_) {
        return getContainerAt(p_155597_, p_155598_.getLevelX(), p_155598_.getLevelY() + 1.0D, p_155598_.getLevelZ());
    }

    public static List<ItemEntity> getItemsAtAndAbove(Level p_155590_, Hopper p_155591_) {
        return p_155591_.getSuckShape().toAabbs().stream().flatMap((p_155558_) -> {
            return p_155590_.getEntitiesOfClass(ItemEntity.class, p_155558_.move(p_155591_.getLevelX() - 0.5D, p_155591_.getLevelY() - 0.5D, p_155591_.getLevelZ() - 0.5D), EntitySelector.ENTITY_STILL_ALIVE).stream();
        }).collect(Collectors.toList());
    }

    @Nullable
    public static Container getContainerAt(Level p_59391_, BlockPos p_59392_) {
        return getContainerAt(p_59391_, (double)p_59392_.getX() + 0.5D, (double)p_59392_.getY() + 0.5D, (double)p_59392_.getZ() + 0.5D);
    }

    @Nullable
    private static Container getContainerAt(Level p_59348_, double p_59349_, double p_59350_, double p_59351_) {
        Container container = null;
        BlockPos blockpos = BlockPos.containing(p_59349_, p_59350_, p_59351_);
        BlockState blockstate = p_59348_.getBlockState(blockpos);
        Block block = blockstate.getBlock();
        if (block instanceof WorldlyContainerHolder) {
            container = ((WorldlyContainerHolder)block).getContainer(blockstate, p_59348_, blockpos);
        } else if (blockstate.hasBlockEntity()) {
            BlockEntity blockentity = p_59348_.getBlockEntity(blockpos);
            if (blockentity instanceof Container) {
                container = (Container)blockentity;
                if (container instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    container = ChestBlock.getContainer((ChestBlock)block, blockstate, p_59348_, blockpos, true);
                }
            }
        }

        if (container == null) {
            List<Entity> list = p_59348_.getEntities((Entity)null, new AABB(p_59349_ - 0.5D, p_59350_ - 0.5D, p_59351_ - 0.5D, p_59349_ + 0.5D, p_59350_ + 0.5D, p_59351_ + 0.5D), EntitySelector.CONTAINER_ENTITY_SELECTOR);
            if (!list.isEmpty()) {
                container = (Container)list.get(p_59348_.random.nextInt(list.size()));
            }
        }

        return container;
    }


    private static boolean canMergeItems(ItemStack p_59345_, ItemStack p_59346_) {
        return p_59345_.getCount() <= p_59345_.getMaxStackSize() && ItemStack.isSameItemSameTags(p_59345_, p_59346_);
    }

    public double getLevelX() {
        return (double)this.worldPosition.getX() + 0.5D;
    }

    public double getLevelY() {
        return (double)this.worldPosition.getY() + 0.5D;
    }

    public double getLevelZ() {
        return (double)this.worldPosition.getZ() + 0.5D;
    }


    public void setCooldown(int p_59396_) {
        this.cooldownTime = p_59396_;
    }

    private boolean isOnCooldown() {
        return this.cooldownTime > 0;
    }

    public boolean isOnCustomCooldown() {
        return this.cooldownTime > 8;
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public void setItems(NonNullList<ItemStack> p_59371_) {
        this.items = p_59371_;
    }

    public static void entityInside(Level p_155568_, BlockPos p_155569_, BlockState p_155570_, Entity p_155571_, NGHopperBlockEntity p_155572_) {
        if (p_155571_ instanceof ItemEntity && Shapes.joinIsNotEmpty(Shapes.create(p_155571_.getBoundingBox().move((double)(-p_155569_.getX()), (double)(-p_155569_.getY()), (double)(-p_155569_.getZ()))), p_155572_.getSuckShape(), BooleanOp.AND)) {
            tryMoveItems(p_155568_, p_155569_, p_155570_, p_155572_, () -> {
                return addItem(p_155572_, (ItemEntity)p_155571_);
            });
        }

    }

    protected AbstractContainerMenu createMenu(int p_59312_, Inventory p_59313_) {
        return new HopperMenu(p_59312_, p_59313_, this);
    }

    @Override
    protected IItemHandler createUnSidedHandler() {
        return new NGHopperItemHandler(this);
    }

    public long getLastUpdateTime() {
        return this.tickedGameTime;
    }


}
