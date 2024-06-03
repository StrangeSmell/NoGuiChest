package com.strangesmell.noguichest.dispenser;

import com.strangesmell.noguichest.channel.Channel;
import com.strangesmell.noguichest.channel.Issues4Message;
import com.strangesmell.noguichest.chest.NGChestEntity;
import com.strangesmell.noguichest.dropper.NGDropperBlockEntity;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.*;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class NGDispenser extends DispenserBlock {
    public static final DirectionProperty FACING = DirectionalBlock.FACING;
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    private static final Map<Item, DispenseItemBehavior> DISPENSER_REGISTRY = Util.make(new Object2ObjectOpenHashMap<>(), (p_52723_) -> {
        p_52723_.defaultReturnValue(new DefaultDispenseItemBehavior());
    });
    private static final int TRIGGER_DURATION = 4;

    public NGDispenser() {
        super(BlockBehaviour.Properties.copy(Blocks.DISPENSER));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, Boolean.FALSE));
    }

    public static void registerBehavior(ItemLike p_52673_, DispenseItemBehavior p_52674_) {
        DISPENSER_REGISTRY.put(p_52673_.asItem(), p_52674_);
    }
    private static double onePix =0.0625;
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos blockPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockentity = pLevel.getBlockEntity(blockPos);
            if (blockentity instanceof NGDispenserEntity ngDispenserEntity) {
                Direction hitFace = pHit.getDirection();
                Vec3 viewPose = pHit.getLocation();
                if(hitFace!=Direction.UP){//不是上面
                    pPlayer.openMenu((DispenserBlockEntity)blockentity);
                    if (blockentity instanceof NGDropperBlockEntity) {
                        pPlayer.awardStat(Stats.INSPECT_DROPPER);
                    } else {
                        pPlayer.awardStat(Stats.INSPECT_DISPENSER);
                    }
                }else{//是上面
                    double dx =viewPose.get(Direction.Axis.X)-blockPos.getX();
                    double dz =viewPose.get(Direction.Axis.Z)-blockPos.getZ();
                    int line,row;
                    line=(int)((dz-3.2*onePix)/0.2);
                    row=(int) ((dx-3.2*onePix)/0.2);
                    if(dx>=3.2*onePix&&dz>=3.2*onePix&&dx<3.2*onePix+0.6&&dz<3.2*onePix+0.6){
                        setItem(line*3+row,pPlayer,pHand,ngDispenserEntity);
                    }else{
                        pPlayer.openMenu((DispenserBlockEntity)blockentity);
                        if (blockentity instanceof NGDropperBlockEntity) {
                            pPlayer.awardStat(Stats.INSPECT_DROPPER);
                        } else {
                            pPlayer.awardStat(Stats.INSPECT_DISPENSER);
                        }
                    }

                }
            }
            return InteractionResult.CONSUME;
        }
    }
    public static void setItem(int index, Player player, InteractionHand pHand, NGDispenserEntity blockEntity ){

        ItemStack useItemStack = player.getItemInHand(pHand);
        ItemStack chestItemStack = blockEntity.getItems().get(index);

        if(useItemStack.isEmpty()&&chestItemStack.isEmpty()) return;

        if (!useItemStack.isEmpty()&&chestItemStack.isEmpty()) {
            blockEntity.getItems().set(index,useItemStack);
            player.setItemInHand(pHand,new ItemStack(Items.AIR));
            Channel.sendToChunk(new S2CMessageDispenser(blockEntity.getItems(),blockEntity.getBlockPos()),blockEntity.getLevel().getChunkAt(blockEntity.getBlockPos()));
        }
        if (useItemStack.isEmpty()&&!chestItemStack.isEmpty()) {
            player.setItemInHand(pHand,chestItemStack);
            blockEntity.getItems().set(index,new ItemStack(Items.AIR));
            Channel.sendToChunk(new S2CMessageDispenser(blockEntity.getItems(),blockEntity.getBlockPos()),blockEntity.getLevel().getChunkAt(blockEntity.getBlockPos()));
        }
        if (!useItemStack.isEmpty()&&!chestItemStack.isEmpty()) {
            blockEntity.getItems().set(index,useItemStack);
            player.setItemInHand(pHand, chestItemStack);
            Channel.sendToChunk(new S2CMessageDispenser(blockEntity.getItems(),blockEntity.getBlockPos()),blockEntity.getLevel().getChunkAt(blockEntity.getBlockPos()));
        }

    }
    protected void dispenseFrom(ServerLevel p_52665_, BlockPos p_52666_) {
        BlockSourceImpl blocksourceimpl = new BlockSourceImpl(p_52665_, p_52666_);
        NGDispenserEntity ngDispenserEntity = blocksourceimpl.getEntity();
        int i = ngDispenserEntity.getRandomSlot(p_52665_.random);
        if (i < 0) {
            p_52665_.levelEvent(1001, p_52666_, 0);
            p_52665_.gameEvent(GameEvent.BLOCK_ACTIVATE, p_52666_, GameEvent.Context.of(ngDispenserEntity.getBlockState()));
        } else {
            ItemStack itemstack = ngDispenserEntity.getItem(i);
            DispenseItemBehavior dispenseitembehavior = this.getDispenseMethod(itemstack);
            if (dispenseitembehavior != DispenseItemBehavior.NOOP) {
                ngDispenserEntity.setItem(i, dispenseitembehavior.dispense(blocksourceimpl, itemstack));
            }

        }
    }

    protected DispenseItemBehavior getDispenseMethod(ItemStack p_52667_) {
        return DISPENSER_REGISTRY.get(p_52667_.getItem());
    }

    public void neighborChanged(BlockState p_52700_, Level p_52701_, BlockPos p_52702_, Block p_52703_, BlockPos p_52704_, boolean p_52705_) {
        boolean flag = p_52701_.hasNeighborSignal(p_52702_) || p_52701_.hasNeighborSignal(p_52702_.above());
        boolean flag1 = p_52700_.getValue(TRIGGERED);
        if (flag && !flag1) {
            p_52701_.scheduleTick(p_52702_, this, 4);
            p_52701_.setBlock(p_52702_, p_52700_.setValue(TRIGGERED, Boolean.valueOf(true)), 4);
        } else if (!flag && flag1) {
            p_52701_.setBlock(p_52702_, p_52700_.setValue(TRIGGERED, Boolean.valueOf(false)), 4);
        }

    }

    public void tick(BlockState p_221075_, ServerLevel p_221076_, BlockPos p_221077_, RandomSource p_221078_) {
        this.dispenseFrom(p_221076_, p_221077_);
    }

    public BlockEntity newBlockEntity(BlockPos p_153162_, BlockState p_153163_) {
        return new NGDispenserEntity(p_153162_, p_153163_);
    }

    public BlockState getStateForPlacement(BlockPlaceContext p_52669_) {
        return this.defaultBlockState().setValue(FACING, p_52669_.getNearestLookingDirection().getOpposite());
    }

    public void setPlacedBy(Level p_52676_, BlockPos p_52677_, BlockState p_52678_, LivingEntity p_52679_, ItemStack p_52680_) {
        if (p_52680_.hasCustomHoverName()) {
            BlockEntity blockentity = p_52676_.getBlockEntity(p_52677_);
            if (blockentity instanceof NGDispenserEntity) {
                ((NGDispenserEntity)blockentity).setCustomName(p_52680_.getHoverName());
            }
        }

    }

    public void onRemove(BlockState p_52707_, Level p_52708_, BlockPos p_52709_, BlockState p_52710_, boolean p_52711_) {
        if (!p_52707_.is(p_52710_.getBlock())) {
            BlockEntity blockentity = p_52708_.getBlockEntity(p_52709_);
            if (blockentity instanceof NGDispenserEntity) {
                Containers.dropContents(p_52708_, p_52709_, (NGDispenserEntity)blockentity);
                p_52708_.updateNeighbourForOutputSignal(p_52709_, this);
            }

            super.onRemove(p_52707_, p_52708_, p_52709_, p_52710_, p_52711_);
        }
    }

    public boolean hasAnalogOutputSignal(BlockState p_52682_) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState p_52689_, Level p_52690_, BlockPos p_52691_) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(p_52690_.getBlockEntity(p_52691_));
    }

    public RenderShape getRenderShape(BlockState p_52725_) {
        return RenderShape.MODEL;
    }

    public BlockState rotate(BlockState p_52716_, Rotation p_52717_) {
        return p_52716_.setValue(FACING, p_52717_.rotate(p_52716_.getValue(FACING)));
    }

    public BlockState mirror(BlockState p_52713_, Mirror p_52714_) {
        return p_52713_.rotate(p_52714_.getRotation(p_52713_.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_52719_) {
        p_52719_.add(FACING, TRIGGERED);
    }
}
