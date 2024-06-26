package com.strangesmell.noguichest.hopper;

import com.strangesmell.noguichest.NoGuiChest;
import com.strangesmell.noguichest.channel.Channel;
import com.strangesmell.noguichest.channel.Issues4Message;
import com.strangesmell.noguichest.dropper.NGDropperBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.stats.Stats;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class NGHopper extends HopperBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING_HOPPER;
    public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
    private static final VoxelShape TOP = Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
    private static final VoxelShape FUNNEL = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
    private static final VoxelShape CONVEX_BASE = Shapes.or(FUNNEL, TOP);
    private static final VoxelShape BASE = Shapes.join(CONVEX_BASE, Hopper.INSIDE, BooleanOp.ONLY_FIRST);
    private static final VoxelShape DOWN_SHAPE = Shapes.or(BASE, Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D));
    private static final VoxelShape EAST_SHAPE = Shapes.or(BASE, Block.box(12.0D, 4.0D, 6.0D, 16.0D, 8.0D, 10.0D));
    private static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, Block.box(6.0D, 4.0D, 0.0D, 10.0D, 8.0D, 4.0D));
    private static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, Block.box(6.0D, 4.0D, 12.0D, 10.0D, 8.0D, 16.0D));
    private static final VoxelShape WEST_SHAPE = Shapes.or(BASE, Block.box(0.0D, 4.0D, 6.0D, 4.0D, 8.0D, 10.0D));
    private static final VoxelShape DOWN_INTERACTION_SHAPE = Hopper.INSIDE;
    private static final VoxelShape EAST_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(12.0D, 8.0D, 6.0D, 16.0D, 10.0D, 10.0D));
    private static final VoxelShape NORTH_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(6.0D, 8.0D, 0.0D, 10.0D, 10.0D, 4.0D));
    private static final VoxelShape SOUTH_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(6.0D, 8.0D, 12.0D, 10.0D, 10.0D, 16.0D));
    private static final VoxelShape WEST_INTERACTION_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(0.0D, 8.0D, 6.0D, 4.0D, 10.0D, 10.0D));


    public NGHopper() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.0F, 4.8F).sound(SoundType.METAL).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(ENABLED, Boolean.valueOf(true)));
    }

    public VoxelShape getShape(BlockState p_54105_, BlockGetter p_54106_, BlockPos p_54107_, CollisionContext p_54108_) {
        switch ((Direction)p_54105_.getValue(FACING)) {
            case DOWN:
                return DOWN_SHAPE;
            case NORTH:
                return NORTH_SHAPE;
            case SOUTH:
                return SOUTH_SHAPE;
            case WEST:
                return WEST_SHAPE;
            case EAST:
                return EAST_SHAPE;
            default:
                return BASE;
        }
    }

    public VoxelShape getInteractionShape(BlockState p_54099_, BlockGetter p_54100_, BlockPos p_54101_) {
        switch ((Direction)p_54099_.getValue(FACING)) {
            case DOWN:
                return DOWN_INTERACTION_SHAPE;
            case NORTH:
                return NORTH_INTERACTION_SHAPE;
            case SOUTH:
                return SOUTH_INTERACTION_SHAPE;
            case WEST:
                return WEST_INTERACTION_SHAPE;
            case EAST:
                return EAST_INTERACTION_SHAPE;
            default:
                return Hopper.INSIDE;
        }
    }

    public BlockState getStateForPlacement(BlockPlaceContext p_54041_) {
        Direction direction = p_54041_.getClickedFace().getOpposite();
        return this.defaultBlockState().setValue(FACING, direction.getAxis() == Direction.Axis.Y ? Direction.DOWN : direction).setValue(ENABLED, Boolean.valueOf(true));
    }

    public BlockEntity newBlockEntity(BlockPos p_153382_, BlockState p_153383_) {
        return new NGHopperBlockEntity(p_153382_, p_153383_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153378_, BlockState p_153379_, BlockEntityType<T> p_153380_) {
        return p_153378_.isClientSide ? null: createTickerHelper(p_153380_, NoGuiChest.NGHopperEntity.get(), NGHopperBlockEntity::pushItemsTick);
    }

    public void setPlacedBy(Level p_54049_, BlockPos p_54050_, BlockState p_54051_, LivingEntity p_54052_, ItemStack p_54053_) {
        if (p_54053_.hasCustomHoverName()) {
            BlockEntity blockentity = p_54049_.getBlockEntity(p_54050_);
            if (blockentity instanceof NGHopperBlockEntity) {
                ((NGHopperBlockEntity)blockentity).setCustomName(p_54053_.getHoverName());
            }
        }

    }

    public void onPlace(BlockState p_54110_, Level p_54111_, BlockPos p_54112_, BlockState p_54113_, boolean p_54114_) {
        if (!p_54113_.is(p_54110_.getBlock())) {
            this.checkPoweredState(p_54111_, p_54112_, p_54110_, 2);
        }
    }
    private static double onePix =0.0625;
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos blockPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            //tried to fix issues#4
            NGHopperBlockEntity blockEntity = (NGHopperBlockEntity) pLevel.getBlockEntity(blockPos);
            if(blockEntity.getItems() == null){
                Channel.sendToServer(new Issues4Message(blockPos));
            }
            //end
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockentity = pLevel.getBlockEntity(blockPos);
            if (blockentity instanceof NGHopperBlockEntity ngHopperBlockEntity) {

                Direction hitFace = pHit.getDirection();
                Vec3 viewPose = pHit.getLocation();
                if(hitFace!=Direction.UP){//不是上面
                    pPlayer.openMenu(ngHopperBlockEntity);
                    pPlayer.awardStat(Stats.INSPECT_HOPPER);
                }else{//是上面
                    double dx =viewPose.get(Direction.Axis.X)-blockPos.getX();
                    double dz =viewPose.get(Direction.Axis.Z)-blockPos.getZ();
                    int index = (int) ((dx-2*onePix)/(onePix*4))+((int)((dz-2*onePix)/(onePix*4))*3);
                    setItem(index,pPlayer,pHand,ngHopperBlockEntity);
                }
            }
            return InteractionResult.CONSUME;
        }
    }

    public static void setItem(int index, Player player, InteractionHand pHand, NGHopperBlockEntity blockEntity ){
        switch (index){
            case 0->index = 0;
            case 2->index = 1;
            case 4->index = 2;
            case 6->index = 3;
            case 8->index = 4;
            default -> {
                return;
            }
        }
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

    public void neighborChanged(BlockState p_54078_, Level p_54079_, BlockPos p_54080_, Block p_54081_, BlockPos p_54082_, boolean p_54083_) {
        this.checkPoweredState(p_54079_, p_54080_, p_54078_, 4);
    }

    private void checkPoweredState(Level p_275499_, BlockPos p_275298_, BlockState p_275611_, int p_275625_) {
        boolean flag = !p_275499_.hasNeighborSignal(p_275298_);
        if (flag != p_275611_.getValue(ENABLED)) {
            p_275499_.setBlock(p_275298_, p_275611_.setValue(ENABLED, Boolean.valueOf(flag)), p_275625_);
        }

    }

    public void onRemove(BlockState p_54085_, Level p_54086_, BlockPos p_54087_, BlockState p_54088_, boolean p_54089_) {
        if (!p_54085_.is(p_54088_.getBlock())) {
            BlockEntity blockentity = p_54086_.getBlockEntity(p_54087_);
            if (blockentity instanceof NGHopperBlockEntity) {
                Containers.dropContents(p_54086_, p_54087_, (NGHopperBlockEntity)blockentity);
                p_54086_.updateNeighbourForOutputSignal(p_54087_, this);
            }

            super.onRemove(p_54085_, p_54086_, p_54087_, p_54088_, p_54089_);
        }
    }

    public RenderShape getRenderShape(BlockState p_54103_) {
        return RenderShape.MODEL;
    }

    public boolean hasAnalogOutputSignal(BlockState p_54055_) {
        return true;
    }

    public int getAnalogOutputSignal(BlockState p_54062_, Level p_54063_, BlockPos p_54064_) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(p_54063_.getBlockEntity(p_54064_));
    }

    public BlockState rotate(BlockState p_54094_, Rotation p_54095_) {
        return p_54094_.setValue(FACING, p_54095_.rotate(p_54094_.getValue(FACING)));
    }

    public BlockState mirror(BlockState p_54091_, Mirror p_54092_) {
        return p_54091_.rotate(p_54092_.getRotation(p_54091_.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_54097_) {
        p_54097_.add(FACING, ENABLED);
    }

    public void entityInside(BlockState p_54066_, Level p_54067_, BlockPos p_54068_, Entity p_54069_) {
        BlockEntity blockentity = p_54067_.getBlockEntity(p_54068_);
        if (blockentity instanceof NGHopperBlockEntity) {
            NGHopperBlockEntity.entityInside(p_54067_, p_54068_, p_54066_, p_54069_, (NGHopperBlockEntity)blockentity);
        }

    }

    public boolean isPathfindable(BlockState p_54057_, BlockGetter p_54058_, BlockPos p_54059_, PathComputationType p_54060_) {
        return false;
    }


}
