package com.strangesmell.noguichest.hopper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class NGHopperRenderer implements BlockEntityRenderer<NGHopperBlockEntity> {
    private final ItemRenderer itemRenderer;
    private static double onePix =0.0625;
    public NGHopperRenderer(BlockEntityRendererProvider.Context p_173602_) {
        this.itemRenderer = p_173602_.getItemRenderer();
    }
    @Override
    public void render(NGHopperBlockEntity ngHopperBlockEntity, float pPartialTick, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level level = ngHopperBlockEntity.getLevel();
        NonNullList<ItemStack> nonnulllist = ngHopperBlockEntity.getItems();
        int i = (int)ngHopperBlockEntity.getBlockPos().asLong();
        HitResult hitResult = Minecraft.getInstance().player.pick(5, pPartialTick,false);
        Vec3 viewPose =hitResult.getLocation();
        BlockPos blockPos = ngHopperBlockEntity.getBlockPos();
        double dx =viewPose.get(Direction.Axis.X)-blockPos.getX();
        double dz =viewPose.get(Direction.Axis.Z)-blockPos.getZ();

        for(int j = 0; j < nonnulllist.size(); ++j) {
            ItemStack itemstack = nonnulllist.get(j);
            if (itemstack != ItemStack.EMPTY) {
                poseStack.pushPose();
                int sum=i+j;
                switch (j){
                    case 0->{
                        poseStack.pushPose();
                        poseStack.translate(0, 1, 0);
                        poseStack.translate(0.25, 0, 0.25);
                        poseStack.scale(0.375F, 0.375F, 0.375F);
                        poseStack.translate(0, -0.2, 0);
                        if(dx<onePix*6&&dz<onePix*6&&dx>2*onePix&&dz>2*onePix){
                            poseStack.scale(1.25f,1.25f,1.25f);
                        }
                        this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, poseStack, pBuffer, level, sum);
                        poseStack.popPose();
                    }
                    case 1->{
                        poseStack.pushPose();
                        poseStack.translate(1, 1, 0);
                        poseStack.translate(-0.25, 0, 0.25);
                        poseStack.scale(0.375F, 0.375F, 0.375F);
                        poseStack.translate(0, -0.2, 0);
                        if(dx<onePix*14&&dz<onePix*6&&dx>10*onePix&&dz>2*onePix){
                            poseStack.scale(1.25f,1.25f,1.25f);
                        }
                        this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, poseStack, pBuffer, level, sum);
                        poseStack.popPose();
                    }
                    case 2->{
                        poseStack.pushPose();
                        poseStack.translate(0.5, 1, 0.5F);
                        poseStack.scale(0.375F, 0.375F, 0.375F);
                        poseStack.translate(0, -0.2, 0);
                        if(dx<onePix*10&&dz<onePix*10&&dx>6*onePix&&dz>6*onePix){
                            poseStack.scale(1.25f,1.25f,1.25f);
                        }
                        this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, poseStack, pBuffer, level, sum);
                        poseStack.popPose();
                    }
                    case 3->{

                        poseStack.pushPose();
                        poseStack.translate(0, 1, 1);
                        poseStack.translate(0.25, 0, -0.25F);
                        poseStack.scale(0.375F, 0.375F, 0.375F);
                        poseStack.translate(0, -0.2, 0);
                        if(dx<onePix*6&&dz<onePix*14&&dx>2*onePix&&dz>10*onePix){
                            poseStack.scale(1.25f,1.25f,1.25f);
                        }
                        this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, poseStack, pBuffer, level, sum);
                        poseStack.popPose();}
                    case 4->{
                        poseStack.pushPose();
                        poseStack.translate(1, 1, 1);
                        poseStack.translate(-0.25, 0, -0.25);
                        poseStack.scale(0.375F, 0.375F, 0.375F);
                        poseStack.translate(0, -0.2, 0);
                        if(dx<onePix*14&&dz<onePix*14&&dx>10*onePix&&dz>10*onePix){
                            poseStack.scale(1.25f,1.25f,1.25f);
                        }
                        this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, poseStack, pBuffer, level, sum);
                        poseStack.popPose();
                    }
                }
                poseStack.popPose();
            }
        }

    }
}
