package com.strangesmell.noguichest.chest;

import com.strangesmell.noguichest.channel.Channel;
import com.strangesmell.noguichest.NoGuiChest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;


public class NGChest extends ChestBlock implements SimpleWaterloggedBlock  {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static DirectionProperty getFacing(){
        return FACING;
    }
    public static final EnumProperty<ChestType> TYPE = BlockStateProperties.CHEST_TYPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    protected static final VoxelShape NORTH_AABB = Block.box(1.0D, 0.0D, 0.0D, 15.0D, 14.0D, 15.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(0.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
    protected static final VoxelShape EAST_AABB = Block.box(1.0D, 0.0D, 1.0D, 16.0D, 14.0D, 15.0D);
    protected static final VoxelShape AABB = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 14.0D, 15.0D);
    private static double oneSlot =0.175;
    private static double onePix =0.0625;
    public NGChest() {

        super(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS).strength(2.5F).sound(SoundType.WOOD).ignitedByLava(),
                NoGuiChest.NGChestEntity::get);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TYPE, ChestType.SINGLE).setValue(WATERLOGGED, Boolean.FALSE));

    }

    public InteractionResult use(BlockState pState, Level pLevel, BlockPos blockPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            NGChestEntity ngChestEntity = (NGChestEntity) pLevel.getBlockEntity(blockPos);
            Direction face = ngChestEntity.getBlockState().getValue(NGChest.FACING);
            Direction hitFace = pHit.getDirection();
            Vec3 viewPose = pHit.getLocation();

            boolean openState = ngChestEntity.getOpenState();

            if(hitFace!=Direction.UP){//不是上面
                if(openState){//不是上面且开启了 关箱子
                    ChestType chesttype = ngChestEntity.getBlockState().hasProperty(NGChest.TYPE) ? ngChestEntity.getBlockState().getValue(NGChest.TYPE) : ChestType.SINGLE;
                    if(chesttype!=ChestType.SINGLE){
                        getOtherChest(ngChestEntity).stopOpen(pPlayer);
                    }
                    ngChestEntity.stopOpen(pPlayer);
                    ngChestEntity.difOpenState();
                }else{//未开启  打开gui
                    MenuProvider menuprovider = this.getMenuProvider(pState, pLevel, blockPos);
                    if (menuprovider != null) {
                        pPlayer.openMenu(menuprovider);
                        pPlayer.awardStat(this.getOpenChestStat());
                        PiglinAi.angerNearbyPiglins(pPlayer, true);
                    }
                }
            }else{//是上面
                if(openState){//是上面且开启了 setItem
                    //setItem
                    difFaceSetItem( blockPos, ngChestEntity, face,  viewPose , pPlayer,  pHand,pState ,  pLevel,  blockPos,  pHit);
                }else{//是上面但没开启  开启
                    ChestType chesttype = ngChestEntity.getBlockState().hasProperty(NGChest.TYPE) ? ngChestEntity.getBlockState().getValue(NGChest.TYPE) : ChestType.SINGLE;
                    if(chesttype!=ChestType.SINGLE){
                        getOtherChest(ngChestEntity).startOpen(pPlayer);
                    }
                    ngChestEntity.startOpen(pPlayer);
                    ngChestEntity.difOpenState();
                }
            }
            return InteractionResult.CONSUME;
        }
    }

    public NGChestEntity getOtherChest(NGChestEntity ngChestEntity){
        ChestType chesttype = ngChestEntity.getBlockState().hasProperty(NGChest.TYPE) ? ngChestEntity.getBlockState().getValue(NGChest.TYPE) : ChestType.SINGLE;

            switch (ngChestEntity.getBlockState().getValue(NGChest.FACING)){
                case EAST ->{
                    if(chesttype==ChestType.LEFT)
                        return  ((NGChestEntity)(ngChestEntity.getLevel().getBlockEntity(ngChestEntity.getBlockPos().offset(0,0,1))));
                    else
                        return  ((NGChestEntity)(ngChestEntity.getLevel().getBlockEntity(ngChestEntity.getBlockPos().offset(0,0,-1))));

                }
                case WEST -> {
                    if(chesttype==ChestType.LEFT)
                        return  ((NGChestEntity)(ngChestEntity.getLevel().getBlockEntity(ngChestEntity.getBlockPos().offset(0,0,-1))));

                    else
                        return  ((NGChestEntity)(ngChestEntity.getLevel().getBlockEntity(ngChestEntity.getBlockPos().offset(0,0,1))));

                }
                case SOUTH -> {
                    if(chesttype==ChestType.LEFT)
                        return  ((NGChestEntity)(ngChestEntity.getLevel().getBlockEntity(ngChestEntity.getBlockPos().offset(-1,0,0))));
                    else
                        return  ((NGChestEntity)(ngChestEntity.getLevel().getBlockEntity(ngChestEntity.getBlockPos().offset(1,0,0))));

                }
                case NORTH -> {
                    if(chesttype==ChestType.LEFT)
                        return  ((NGChestEntity)(ngChestEntity.getLevel().getBlockEntity(ngChestEntity.getBlockPos().offset(1,0,0))));

                    else
                        return  ((NGChestEntity)(ngChestEntity.getLevel().getBlockEntity(ngChestEntity.getBlockPos().offset(-1,0,0))));
                }
                default -> {return null; }
            }


    }

    private void difFaceSetItem(BlockPos blockPos,NGChestEntity ngChestEntity,Direction face, Vec3 viewPose ,Player pPlayer, InteractionHand pHand,BlockState pState, Level pLevel, BlockPos pPos, BlockHitResult pHit){
        if(pHit.getDirection() == Direction.UP){
            switch (face){
                case EAST ->{
                    int dx = (int)((blockPos.getZ() + 1 - viewPose.get(Direction.Axis.Z)-onePix)/oneSlot);
                    int dz = (int)((viewPose.get(Direction.Axis.X)-blockPos.getX()-onePix)/oneSlot);
                    dz=dz*5;
                    setItem(dx+dz,pPlayer,pHand,ngChestEntity);
                    break;
                }
                case WEST -> {
                    int dx = (int)((viewPose.get(Direction.Axis.Z)-blockPos.getZ()-onePix)/oneSlot);
                    int dz = (int)(( blockPos.getX() + 1 - viewPose.get(Direction.Axis.X) - onePix ) / oneSlot );
                    dz=dz*5;
                    setItem(dx+dz,pPlayer,pHand,ngChestEntity);
                    break;
                }
                case SOUTH -> {
                    int dx = (int)((viewPose.get(Direction.Axis.X)-blockPos.getX()-onePix)/oneSlot);
                    int dz = (int)((viewPose.get(Direction.Axis.Z)-blockPos.getZ()-onePix)/oneSlot);
                    dz=dz*5;
                    setItem(dx+dz,pPlayer,pHand,ngChestEntity);
                    break;
                }
                case NORTH -> {
                    int dx = (int)((blockPos.getX()+1- viewPose.get(Direction.Axis.X)-onePix)/oneSlot);
                    int dz = (int)((blockPos.getZ()+1-viewPose.get(Direction.Axis.Z)-onePix)/oneSlot);
                    dz=dz*5;
                    setItem(dx+dz,pPlayer,pHand,ngChestEntity);
                    break;
                }
                default -> {}
            }
        }else {

            MenuProvider menuprovider = this.getMenuProvider(pState, pLevel, pPos);
            if (menuprovider != null) {
                pPlayer.openMenu(menuprovider);
                pPlayer.awardStat(this.getOpenChestStat());
                PiglinAi.angerNearbyPiglins(pPlayer, true);

            }
        }
    }


    public static void setItem(int index, Player player, InteractionHand pHand, NGChestEntity ngChestEntity ){

        ItemStack useItemStack = player.getItemInHand(pHand);
        ItemStack chestItemStack = ngChestEntity.getItems().get(index);

        if(useItemStack.isEmpty()&&chestItemStack.isEmpty()) return;

        if (!useItemStack.isEmpty()&&chestItemStack.isEmpty()) {
            ngChestEntity.getItems().set(index,useItemStack);
            player.setItemInHand(pHand,new ItemStack(Items.AIR));
            Channel.sendToChunk(new S2CMessage(ngChestEntity.getItems(),ngChestEntity.getBlockPos(), ngChestEntity.getOpenState()),ngChestEntity.getLevel().getChunkAt(ngChestEntity.getBlockPos()));
        }
        if (useItemStack.isEmpty()&&!chestItemStack.isEmpty()) {
            player.setItemInHand(pHand,chestItemStack);
            ngChestEntity.getItems().set(index,new ItemStack(Items.AIR));
            Channel.sendToChunk(new S2CMessage(ngChestEntity.getItems(),ngChestEntity.getBlockPos(), ngChestEntity.getOpenState()),ngChestEntity.getLevel().getChunkAt(ngChestEntity.getBlockPos()));
        }
        if (!useItemStack.isEmpty()&&!chestItemStack.isEmpty()) {
            ngChestEntity.getItems().set(index,useItemStack);
            player.setItemInHand(pHand, chestItemStack);
            Channel.sendToChunk(new S2CMessage(ngChestEntity.getItems(),ngChestEntity.getBlockPos(), ngChestEntity.getOpenState()),ngChestEntity.getLevel().getChunkAt(ngChestEntity.getBlockPos()));
        }

    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        if (pState.getValue(TYPE) == ChestType.SINGLE) {
            return AABB;
        } else {
            switch (getConnectedDirection(pState)) {
                case NORTH:
                default:
                    return NORTH_AABB;
                case SOUTH:
                    return SOUTH_AABB;
                case WEST:
                    return WEST_AABB;
                case EAST:
                    return EAST_AABB;
            }
        }
    }



    private static final DoubleBlockCombiner.Combiner<NGChestEntity, Optional<MenuProvider>> MENU_PROVIDER_COMBINER = new DoubleBlockCombiner.Combiner<NGChestEntity, Optional<MenuProvider>>() {
        public Optional<MenuProvider> acceptDouble(final NGChestEntity ngChestEntityFirst, final NGChestEntity ngChestEntity) {
            final Container container = new CompoundContainer(ngChestEntityFirst, ngChestEntity);
            return Optional.of(new MenuProvider() {
                @Nullable
                public AbstractContainerMenu createMenu(int pContainerId, Inventory inventory, Player player) {
                    if (ngChestEntityFirst.canOpen(player) && ngChestEntity.canOpen(player)) {
                        ngChestEntityFirst.unpackLootTable(inventory.player);
                        ngChestEntity.unpackLootTable(inventory.player);
                        return ChestMenu.sixRows(pContainerId, inventory, container);
                    } else {
                        return null;
                    }
                }

                public Component getDisplayName() {
                    if (ngChestEntityFirst.hasCustomName()) {
                        return ngChestEntityFirst.getDisplayName();
                    } else {
                        return (ngChestEntity.hasCustomName() ? ngChestEntity.getDisplayName() : Component.translatable("container.NGChestDouble"));
                    }
                }
            });
        }

        public Optional<MenuProvider> acceptSingle(NGChestEntity ngChestEntity) {
            return Optional.of(ngChestEntity);
        }

        public Optional<MenuProvider> acceptNone() {
            return Optional.empty();
        }
    };


    @Nullable
    public MenuProvider getMenuProvider(BlockState pState, Level pLevel, BlockPos pPos) {
        return this.combine(pState, pLevel, pPos, false).apply(MENU_PROVIDER_COMBINER).orElse((MenuProvider)null);
    }

    public Stat<ResourceLocation> getOpenChestStat() {
        return Stats.CUSTOM.get(Stats.OPEN_CHEST);
    }

    public static boolean isChestBlockedAt(LevelAccessor p_51509_, BlockPos p_51510_) {
        return isBlockedChestByBlock(p_51509_, p_51510_) || isCatSittingOnChest(p_51509_, p_51510_);
    }

    private static boolean isBlockedChestByBlock(BlockGetter pLevel, BlockPos pPos) {
        BlockPos blockpos = pPos.above();
        return pLevel.getBlockState(blockpos).isRedstoneConductor(pLevel, blockpos);
    }

    private static boolean isCatSittingOnChest(LevelAccessor pLevel, BlockPos pPos) {
        List<Cat> list = pLevel.getEntitiesOfClass(Cat.class, new AABB((double)pPos.getX(), (double)(pPos.getY() + 1), (double)pPos.getZ(), (double)(pPos.getX() + 1), (double)(pPos.getY() + 2), (double)(pPos.getZ() + 1)));
        if (!list.isEmpty()) {
            for(Cat cat : list) {
                if (cat.isInSittingPose()) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public DoubleBlockCombiner.NeighborCombineResult<? extends NGChestEntity> combine(BlockState pState, Level pLevel, BlockPos pPos, boolean pOverride) {
        BiPredicate<LevelAccessor, BlockPos> bipredicate;
        if (pOverride) {
            bipredicate = (p_51578_, p_51579_) -> {
                return false;
            };
        } else {
            bipredicate = NGChest::isChestBlockedAt;
        }

        return (DoubleBlockCombiner.NeighborCombineResult<? extends NGChestEntity>) DoubleBlockCombiner.combineWithNeigbour(this.blockEntityType.get(), NGChest::getBlockType, NGChest::getConnectedDirection, FACING, pState, pLevel, pPos, bipredicate);
    }

    public static DoubleBlockCombiner.BlockType getBlockType(BlockState p_51583_) {
        ChestType chesttype = p_51583_.getValue(TYPE);
        if (chesttype == ChestType.SINGLE) {
            return DoubleBlockCombiner.BlockType.SINGLE;
        } else {
            return chesttype == ChestType.RIGHT ? DoubleBlockCombiner.BlockType.FIRST : DoubleBlockCombiner.BlockType.SECOND;
        }
    }
    public static Direction getConnectedDirection(BlockState p_51585_) {
        Direction direction = p_51585_.getValue(FACING);
        return p_51585_.getValue(TYPE) == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new NGChestEntity(pPos, pState);
    }

    public void tick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
        BlockEntity blockentity = pLevel.getBlockEntity(pPos);
        if (blockentity instanceof NGChestEntity ngChestEntity) {
            ngChestEntity.recheckOpen();
            //这个tick怎么不调用。。。
            /*
            BlockPos blockPos = ngChestEntity.getBlockPos();

            if(ngChestEntity.getLevel().getNearestPlayer(blockPos.getX(),blockPos.getY(),blockPos.getZ(),5,false)==null&&ngChestEntity.getOpenState()){
                ngChestEntity.difOpenState();
                assert Minecraft.getInstance().player != null;
                ngChestEntity.stopOpen(Minecraft.getInstance().player);
                getOtherChest(ngChestEntity).stopOpen(Minecraft.getInstance().player);
            }*/
        }

    }

    public BlockEntityType<? extends ChestBlockEntity> blockEntityType() {
        return this.blockEntityType.get();
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return pLevel.isClientSide ? createTickerHelper(pBlockEntityType, this.blockEntityType(), NGChestEntity::lidAnimateTick) : null;
    }

}
