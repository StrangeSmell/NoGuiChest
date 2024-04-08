package com.strangesmell.noguichest.brewing;

import com.strangesmell.noguichest.NoGuiChest;
import com.strangesmell.noguichest.channel.Channel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class NGBrewingStandBlock extends BrewingStandBlock {
    protected static final VoxelShape SHAPE = Shapes.or(Block.box(1.0D, 0.0D, 1.0D, 15.0D, 2.0D, 15.0D), Block.box(7.0D, 0.0D, 7.0D, 9.0D, 14.0D, 9.0D));

    public NGBrewingStandBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.BREWING_STAND));
        this.registerDefaultState(this.stateDefinition.any().setValue(HAS_BOTTLE[0], Boolean.valueOf(false)).setValue(HAS_BOTTLE[1], Boolean.valueOf(false)).setValue(HAS_BOTTLE[2], Boolean.valueOf(false)));

    }

    public BlockEntity newBlockEntity(BlockPos p_152698_, BlockState p_152699_) {
        return new NGBrewingStandBlockEntity(p_152698_, p_152699_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_152694_, BlockState p_152695_, BlockEntityType<T> p_152696_) {
        return p_152694_.isClientSide ? null : createTickerHelper(p_152696_, NoGuiChest.NGBrewEntity.get(), NGBrewingStandBlockEntity::serverTick);
    }
    private static double onePix =0.0625;
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos blockPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockentity = pLevel.getBlockEntity(blockPos);
            if (blockentity instanceof NGBrewingStandBlockEntity ngBrewingStandBlockEntity) {
                Vec3 viewPose = pHit.getLocation();
                double dx =viewPose.get(Direction.Axis.X)-blockPos.getX();
                double dy =viewPose.get(Direction.Axis.Y)-blockPos.getY();
                double dz =viewPose.get(Direction.Axis.Z)-blockPos.getZ();
                if(dy<=2*onePix){
                    if(dx<onePix*6&&dz<onePix*6&&dx>onePix&&dz>onePix){
                        setItem(0,pPlayer,pHand,ngBrewingStandBlockEntity);
                        return InteractionResult.CONSUME;
                    }
                    if(dx<onePix*6&&dz<onePix*15&&dx>onePix&&dz>10*onePix){
                        setItem(1,pPlayer,pHand,ngBrewingStandBlockEntity);
                        return InteractionResult.CONSUME;
                    }
                    if(dx<onePix*15&&dz<onePix*11&&dx>10*onePix&&dz>5*onePix){
                        setItem(2,pPlayer,pHand,ngBrewingStandBlockEntity);
                        return InteractionResult.CONSUME;
                    }
                    pPlayer.openMenu((NGBrewingStandBlockEntity)blockentity);
                    pPlayer.awardStat(Stats.INTERACT_WITH_BREWINGSTAND);
                }else {
                    if(dy<onePix*6){
                        setItem(4,pPlayer,pHand,ngBrewingStandBlockEntity);
                    }else{
                        setItem(3,pPlayer,pHand,ngBrewingStandBlockEntity);
                    }
                }
            }

            return InteractionResult.CONSUME;
        }
    }
    public static void setItem(int index, Player player, InteractionHand pHand, NGBrewingStandBlockEntity blockEntity ){

        ItemStack useItemStack = player.getItemInHand(pHand);
        ItemStack chestItemStack = blockEntity.getItems().get(index);

        if(useItemStack.isEmpty()&&chestItemStack.isEmpty()) return;

        if (!useItemStack.isEmpty()&&chestItemStack.isEmpty()) {
            blockEntity.getItems().set(index,useItemStack);
            player.setItemInHand(pHand,new ItemStack(Items.AIR));
            Channel.sendToChunk(new S2CMessageBrewing(blockEntity.getItems(),blockEntity.getBlockPos()),blockEntity.getLevel().getChunkAt(blockEntity.getBlockPos()));
        }
        if (useItemStack.isEmpty()&&!chestItemStack.isEmpty()) {
            player.setItemInHand(pHand,chestItemStack);
            blockEntity.getItems().set(index,new ItemStack(Items.AIR));
            Channel.sendToChunk(new S2CMessageBrewing(blockEntity.getItems(),blockEntity.getBlockPos()),blockEntity.getLevel().getChunkAt(blockEntity.getBlockPos()));
        }
        if (!useItemStack.isEmpty()&&!chestItemStack.isEmpty()) {
            blockEntity.getItems().set(index,useItemStack);
            player.setItemInHand(pHand, chestItemStack);
            Channel.sendToChunk(new S2CMessageBrewing(blockEntity.getItems(),blockEntity.getBlockPos()),blockEntity.getLevel().getChunkAt(blockEntity.getBlockPos()));
        }

    }
    public void setPlacedBy(Level p_50913_, BlockPos p_50914_, BlockState p_50915_, LivingEntity p_50916_, ItemStack p_50917_) {
        if (p_50917_.hasCustomHoverName()) {
            BlockEntity blockentity = p_50913_.getBlockEntity(p_50914_);
            if (blockentity instanceof NGBrewingStandBlockEntity) {
                ((NGBrewingStandBlockEntity)blockentity).setCustomName(p_50917_.getHoverName());
            }
        }

    }

    public void animateTick(BlockState p_220883_, Level p_220884_, BlockPos p_220885_, RandomSource p_220886_) {
        double d0 = (double)p_220885_.getX() + 0.4D + (double)p_220886_.nextFloat() * 0.2D;
        double d1 = (double)p_220885_.getY() + 0.7D + (double)p_220886_.nextFloat() * 0.3D;
        double d2 = (double)p_220885_.getZ() + 0.4D + (double)p_220886_.nextFloat() * 0.2D;
        //p_220884_.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
    }

    public void onRemove(BlockState p_50937_, Level p_50938_, BlockPos p_50939_, BlockState p_50940_, boolean p_50941_) {
        if (!p_50937_.is(p_50940_.getBlock())) {
            BlockEntity blockentity = p_50938_.getBlockEntity(p_50939_);
            if (blockentity instanceof NGBrewingStandBlockEntity) {
                Containers.dropContents(p_50938_, p_50939_, (NGBrewingStandBlockEntity)blockentity);
            }

            super.onRemove(p_50937_, p_50938_, p_50939_, p_50940_, p_50941_);
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_50948_) {
        p_50948_.add(HAS_BOTTLE[0], HAS_BOTTLE[1], HAS_BOTTLE[2]);
    }
}
