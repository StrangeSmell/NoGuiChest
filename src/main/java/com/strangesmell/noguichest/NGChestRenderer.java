package com.strangesmell.noguichest;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Calendar;

@OnlyIn(Dist.CLIENT)
public class NGChestRenderer<T extends BlockEntity & LidBlockEntity> implements BlockEntityRenderer<T> {
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ModelPart doubleLeftLid;
    private final ModelPart doubleLeftBottom;
    private final ModelPart doubleLeftLock;
    private final ModelPart doubleRightLid;
    private final ModelPart doubleRightBottom;
    private final ModelPart doubleRightLock;
    private boolean xmasTextures;
    private final ItemRenderer itemRenderer;
    private static double oneSlot =0.175;
    private static double onePix =0.0625;
    public NGChestRenderer(BlockEntityRendererProvider.Context pContext) {
        Calendar calendar = Calendar.getInstance();
        this.itemRenderer = pContext.getItemRenderer();
        if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
            this.xmasTextures = true;
        }

        ModelPart modelpart = pContext.bakeLayer(ModelLayers.CHEST);
        this.bottom = modelpart.getChild("bottom");
        this.lid = modelpart.getChild("lid");
        this.lock = modelpart.getChild("lock");
        ModelPart modelpart1 = pContext.bakeLayer(ModelLayers.DOUBLE_CHEST_LEFT);
        this.doubleLeftBottom = modelpart1.getChild("bottom");
        this.doubleLeftLid = modelpart1.getChild("lid");
        this.doubleLeftLock = modelpart1.getChild("lock");
        ModelPart modelpart2 = pContext.bakeLayer(ModelLayers.DOUBLE_CHEST_RIGHT);
        this.doubleRightBottom = modelpart2.getChild("bottom");
        this.doubleRightLid = modelpart2.getChild("lid");
        this.doubleRightLock = modelpart2.getChild("lock");
    }

    public void render(T pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level level = pBlockEntity.getLevel();
        NGChestEntity ngChestEntity =(NGChestEntity) pBlockEntity;
        boolean flag = level != null;
        BlockState blockstate = flag ? pBlockEntity.getBlockState() : Blocks.CHEST.defaultBlockState().setValue(NGChest.FACING, Direction.SOUTH);
        ChestType chesttype = blockstate.hasProperty(NGChest.TYPE) ? blockstate.getValue(NGChest.TYPE) : ChestType.SINGLE;
        Block block = blockstate.getBlock();
        if (block instanceof AbstractChestBlock<?> abstractchestblock) {
            boolean flag1 = chesttype != ChestType.SINGLE;//若是大箱子
            pPoseStack.pushPose();
            float f = blockstate.getValue(NGChest.FACING).toYRot();
            pPoseStack.translate(0.5F, 0.5F, 0.5F);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(-f));
            pPoseStack.translate(-0.5F, -0.5F, -0.5F);
            DoubleBlockCombiner.NeighborCombineResult<? extends ChestBlockEntity> neighborcombineresult;
            if (flag) {
                neighborcombineresult = abstractchestblock.combine(blockstate, level, pBlockEntity.getBlockPos(), true);
            } else {
                neighborcombineresult = DoubleBlockCombiner.Combiner::acceptNone;
            }

            float f1 = neighborcombineresult.apply(NGChest.opennessCombiner(pBlockEntity)).get(pPartialTick);
            f1 = 1.0F - f1;
            f1 = 1.0F - f1 * f1 * f1;
            int i = neighborcombineresult.apply(new BrightnessCombiner<>()).applyAsInt(pPackedLight);
            Material material = this.getMaterial(pBlockEntity, chesttype);
            VertexConsumer vertexconsumer = material.buffer(pBuffer, RenderType::entityCutout);
            if (flag1) {
                if (chesttype == ChestType.LEFT) {//左箱子
                    this.render(pPoseStack, vertexconsumer, this.doubleLeftLid, this.doubleLeftLock, this.doubleLeftBottom, f1, i, pPackedOverlay);
                } else {//右箱子
                    this.render(pPoseStack, vertexconsumer, this.doubleRightLid, this.doubleRightLock, this.doubleRightBottom, f1, i, pPackedOverlay);
                }
            } else {//单个箱子
                this.render(pPoseStack, vertexconsumer, this.lid, this.lock, this.bottom, f1, i, pPackedOverlay);
            }

            pPoseStack.popPose();

            pPoseStack.pushPose();//
            if( f1!=0){
                HitResult hitResult = Minecraft.getInstance().player.pick(5, pPartialTick,false);
                //if (hitResult.getType() == HitResult.Type.BLOCK)
                {
                    Vec3 viewPose =hitResult.getLocation();
                    // 在这里根据 blockPos 来改变渲染的大小或其他操作
                    BlockPos blockPos = ngChestEntity.getBlockPos();

                    switch (ngChestEntity.getBlockState().getValue(NGChest.FACING)){
                        case EAST ->{
                            double dx =blockPos.getZ() +1- viewPose.get(Direction.Axis.Z)-onePix;
                            double dz =viewPose.get(Direction.Axis.X)-blockPos.getX()-onePix;
                            renderItem(f,f1,ngChestEntity,flag1,chesttype,pPoseStack,pBuffer,pPackedLight,pPackedOverlay,dx,dz);
                            break;
                        }
                        case WEST -> {
                            double dx =viewPose.get(Direction.Axis.Z)-blockPos.getZ()-onePix;
                            double dz =blockPos.getX()+1-viewPose.get(Direction.Axis.X)-onePix;
                            renderItem(f,f1,ngChestEntity,flag1,chesttype,pPoseStack,pBuffer,pPackedLight,pPackedOverlay,dx,dz);
                            break;
                        }
                        case SOUTH -> {
                            double dx =viewPose.get(Direction.Axis.X)-blockPos.getX()-onePix;
                            double dz =viewPose.get(Direction.Axis.Z)-blockPos.getZ()-onePix;
                            renderItem(f,f1,ngChestEntity,flag1,chesttype,pPoseStack,pBuffer,pPackedLight,pPackedOverlay,dx,dz);
                            break;
                        }
                        case NORTH -> {
                            double dx =blockPos.getX()+1- viewPose.get(Direction.Axis.X)-onePix;
                            double dz =blockPos.getZ()+1-viewPose.get(Direction.Axis.Z)-onePix;
                            renderItem(f,f1,ngChestEntity,flag1,chesttype,pPoseStack,pBuffer,pPackedLight,pPackedOverlay,dx,dz);
                            break;
                        }
                        default -> {}
                    }
                }
            }
            pPoseStack.popPose();
        }
    }

    private void renderItem(float f,float f1,  NGChestEntity ngChestEntity, boolean flag1 , ChestType chesttype , PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay,double dx,double dz){
        boolean shouldBig = dx<0.875&&dz<0.875&&dx>=0.125&&dz>=0.125;
        dx = (int)(dx/oneSlot);
        dz = (int)(dz/oneSlot);
        dz=dz*5;
        int index2 = (int)(dx+dz);
        NonNullList<ItemStack> itemStacks = ngChestEntity.getItems();

        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(-f+180));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);

        pPoseStack.scale(0.2f,0.2f,0.2f);
        pPoseStack.translate(4.5F, 3.2F+f1, 4.5F);

        int count ;
        Minecraft minecraft = Minecraft.getInstance();
        for(int index = 0 ; index < 25 ; index++){
            ItemStack itemStack = itemStacks.get(index);
            if(itemStack == ItemStack.EMPTY) continue;
            int row = index % 5;
            int line = index / 5;
            count = itemStack.getCount();
            pPoseStack.translate(-row,0,-line);

            pPoseStack.scale(f1,f1,f1);
            //指向的item变大
            if(index==index2&&shouldBig){
                pPoseStack.scale(1.25f,1.25f,1.25f);
                this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, ngChestEntity.getLevel(), index);
                pPoseStack.scale(0.8f,0.8f,0.8f);
            }else {
                this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, ngChestEntity.getLevel(), index);
            }
            pPoseStack.scale(1/f1,1/f1,1/f1);

            String text = String.valueOf(count);
            if(count>1) renderCount(pPoseStack,minecraft,text,pBuffer,pPackedLight);
            pPoseStack.translate(row,0,line);
        }
    }
    //先这样吧
    public void renderCount(PoseStack pPoseStack,Minecraft minecraft,String text,MultiBufferSource pBuffer, int pPackedLight){
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(180));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);

        pPoseStack.scale(0.025f,0.025f,0.025f);
        pPoseStack.translate(50F, 40F, -15);
        minecraft.font.drawInBatch(text,0,0,0xFFFFFF, false,pPoseStack.last().pose(),pBuffer, Font.DisplayMode.NORMAL,0x00FFFFFF,pPackedLight );
        pPoseStack.translate(-50F, -40F, 15);
        pPoseStack.scale(40,40,40);

        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
        pPoseStack.mulPose(Axis.XP.rotationDegrees(180));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);
    }

    private void render(PoseStack pPoseStack, VertexConsumer pConsumer, ModelPart pLidPart, ModelPart pLockPart, ModelPart pBottomPart, float pLidAngle, int pPackedLight, int pPackedOverlay) {
        pLidPart.xRot = -(pLidAngle * ((float)Math.PI / 2F));
        pLockPart.xRot = pLidPart.xRot;
        pLidPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
        pLockPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
        pBottomPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
    }

    protected Material getMaterial(T blockEntity, ChestType chestType) {
        return Sheets.chooseMaterial(blockEntity, chestType, this.xmasTextures);
    }
}
