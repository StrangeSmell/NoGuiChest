package com.strangesmell.noguichest.enderchest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class NGEnderChest extends EnderChestBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
    private static final Component CONTAINER_TITLE = Component.translatable("container.enderchest");
    public NGEnderChest() {
        super(BlockBehaviour.Properties.copy(Blocks.ENDER_CHEST));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    public DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> combine(BlockState p_53149_, Level p_53150_, BlockPos p_53151_, boolean p_53152_) {
        return DoubleBlockCombiner.Combiner::acceptNone;
    }

    public VoxelShape getShape(BlockState p_53171_, BlockGetter p_53172_, BlockPos p_53173_, CollisionContext p_53174_) {
        return SHAPE;
    }

    public RenderShape getRenderShape(BlockState p_53169_) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    public BlockState getStateForPlacement(BlockPlaceContext p_53128_) {
        FluidState fluidstate = p_53128_.getLevel().getFluidState(p_53128_.getClickedPos());
        return this.defaultBlockState().setValue(FACING, p_53128_.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
    }

    public InteractionResult use(BlockState p_53137_, Level p_53138_, BlockPos p_53139_, Player p_53140_, InteractionHand p_53141_, BlockHitResult p_53142_) {
        PlayerEnderChestContainer playerenderchestcontainer = p_53140_.getEnderChestInventory();
        BlockEntity blockentity = p_53138_.getBlockEntity(p_53139_);
        if (playerenderchestcontainer != null && blockentity instanceof EnderChestBlockEntity) {
            BlockPos blockpos = p_53139_.above();
            if (p_53138_.getBlockState(blockpos).isRedstoneConductor(p_53138_, blockpos)) {
                return InteractionResult.sidedSuccess(p_53138_.isClientSide);
            } else if (p_53138_.isClientSide) {
                return InteractionResult.SUCCESS;
            } else {
                EnderChestBlockEntity enderchestblockentity = (EnderChestBlockEntity)blockentity;
                playerenderchestcontainer.setActiveChest(enderchestblockentity);
                p_53140_.openMenu(new SimpleMenuProvider((p_53124_, p_53125_, p_53126_) -> {
                    return ChestMenu.threeRows(p_53124_, p_53125_, playerenderchestcontainer);
                }, CONTAINER_TITLE));
                p_53140_.awardStat(Stats.OPEN_ENDERCHEST);
                PiglinAi.angerNearbyPiglins(p_53140_, true);
                return InteractionResult.CONSUME;
            }
        } else {
            return InteractionResult.sidedSuccess(p_53138_.isClientSide);
        }
    }

    public BlockEntity newBlockEntity(BlockPos p_153208_, BlockState p_153209_) {
        return new NGEnderChestBlockEntity(p_153208_, p_153209_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_153199_, BlockState p_153200_, BlockEntityType<T> p_153201_) {
        return p_153199_.isClientSide ? createTickerHelper(p_153201_, BlockEntityType.ENDER_CHEST, EnderChestBlockEntity::lidAnimateTick) : null;
    }

    public void animateTick(BlockState p_221117_, Level p_221118_, BlockPos p_221119_, RandomSource p_221120_) {
        for(int i = 0; i < 3; ++i) {
            int j = p_221120_.nextInt(2) * 2 - 1;
            int k = p_221120_.nextInt(2) * 2 - 1;
            double d0 = (double)p_221119_.getX() + 0.5D + 0.25D * (double)j;
            double d1 = (double)((float)p_221119_.getY() + p_221120_.nextFloat());
            double d2 = (double)p_221119_.getZ() + 0.5D + 0.25D * (double)k;
            double d3 = (double)(p_221120_.nextFloat() * (float)j);
            double d4 = ((double)p_221120_.nextFloat() - 0.5D) * 0.125D;
            double d5 = (double)(p_221120_.nextFloat() * (float)k);
            p_221118_.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
        }

    }

    public BlockState rotate(BlockState p_53157_, Rotation p_53158_) {
        return p_53157_.setValue(FACING, p_53158_.rotate(p_53157_.getValue(FACING)));
    }

    public BlockState mirror(BlockState p_53154_, Mirror p_53155_) {
        return p_53154_.rotate(p_53155_.getRotation(p_53154_.getValue(FACING)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_53167_) {
        p_53167_.add(FACING, WATERLOGGED);
    }

    public FluidState getFluidState(BlockState p_53177_) {
        return p_53177_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_53177_);
    }

    public BlockState updateShape(BlockState p_53160_, Direction p_53161_, BlockState p_53162_, LevelAccessor p_53163_, BlockPos p_53164_, BlockPos p_53165_) {
        if (p_53160_.getValue(WATERLOGGED)) {
            p_53163_.scheduleTick(p_53164_, Fluids.WATER, Fluids.WATER.getTickDelay(p_53163_));
        }

        return super.updateShape(p_53160_, p_53161_, p_53162_, p_53163_, p_53164_, p_53165_);
    }

    public boolean isPathfindable(BlockState p_53132_, BlockGetter p_53133_, BlockPos p_53134_, PathComputationType p_53135_) {
        return false;
    }

    public void tick(BlockState p_221112_, ServerLevel p_221113_, BlockPos p_221114_, RandomSource p_221115_) {
        BlockEntity blockentity = p_221113_.getBlockEntity(p_221114_);
        if (blockentity instanceof EnderChestBlockEntity) {
            ((EnderChestBlockEntity)blockentity).recheckOpen();
        }

    }

}
