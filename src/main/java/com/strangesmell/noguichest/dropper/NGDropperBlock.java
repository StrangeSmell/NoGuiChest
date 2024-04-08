package com.strangesmell.noguichest.dropper;

import com.strangesmell.noguichest.dispenser.NGDispenser;
import com.strangesmell.noguichest.dispenser.NGDispenserEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSourceImpl;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.items.VanillaInventoryCodeHooks;

public class NGDropperBlock extends NGDispenser {

    private static final DispenseItemBehavior DISPENSE_BEHAVIOUR = new DefaultDispenseItemBehavior();

    public NGDropperBlock() {
    }

    protected DispenseItemBehavior getDispenseMethod(ItemStack p_52947_) {
        return DISPENSE_BEHAVIOUR;
    }

    public BlockEntity newBlockEntity(BlockPos p_153179_, BlockState p_153180_) {
        return new NGDropperBlockEntity(p_153179_, p_153180_);
    }
    private static double onePix =0.0625;
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos blockPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockentity = pLevel.getBlockEntity(blockPos);
            if (blockentity instanceof NGDropperBlockEntity ngDropperBlockEntity) {
                Direction hitFace = pHit.getDirection();
                Vec3 viewPose = pHit.getLocation();
                if(hitFace!=Direction.UP){//不是上面
                    pPlayer.openMenu(ngDropperBlockEntity);
                    pPlayer.awardStat(Stats.INSPECT_DROPPER);
                }else{//是上面
                    double dx =viewPose.get(Direction.Axis.X)-blockPos.getX();
                    double dz =viewPose.get(Direction.Axis.Z)-blockPos.getZ();
                    int line,row;
                    line=(int)((dz-3.2*onePix)/0.2);
                    row=(int) ((dx-3.2*onePix)/0.2);
                    if(dx>=3.2*onePix&&dz>=3.2*onePix&&dx<3.2*onePix+0.6&&dz<3.2*onePix+0.6){
                        setItem(line*3+row,pPlayer,pHand,ngDropperBlockEntity);
                    }else{
                        pPlayer.openMenu(ngDropperBlockEntity);
                        pPlayer.awardStat(Stats.INSPECT_DROPPER);

                    }

                }
            }
            return InteractionResult.CONSUME;
        }
    }
    protected void dispenseFrom(ServerLevel p_52944_, BlockPos p_52945_) {
        BlockSourceImpl blocksourceimpl = new BlockSourceImpl(p_52944_, p_52945_);
        NGDispenserEntity dispenserblockentity = (NGDispenserEntity)blocksourceimpl.getEntity();
        int i = dispenserblockentity.getRandomSlot(p_52944_.random);
        if (i < 0) {
            p_52944_.levelEvent(1001, p_52945_, 0);
        } else {
            ItemStack itemstack = dispenserblockentity.getItem(i);
            if (!itemstack.isEmpty() && VanillaInventoryCodeHooks.dropperInsertHook(p_52944_, p_52945_, dispenserblockentity, i, itemstack)) {
                Direction direction = (Direction)p_52944_.getBlockState(p_52945_).getValue(FACING);
                Container container = HopperBlockEntity.getContainerAt(p_52944_, p_52945_.relative(direction));
                ItemStack itemstack1;
                if (container == null) {
                    itemstack1 = DISPENSE_BEHAVIOUR.dispense(blocksourceimpl, itemstack);
                } else {
                    itemstack1 = HopperBlockEntity.addItem(dispenserblockentity, container, itemstack.copy().split(1), direction.getOpposite());
                    if (itemstack1.isEmpty()) {
                        itemstack1 = itemstack.copy();
                        itemstack1.shrink(1);
                    } else {
                        itemstack1 = itemstack.copy();
                    }
                }

                dispenserblockentity.setItem(i, itemstack1);
            }
        }

    }
}
