package com.strangesmell.noguichest.brewing;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.strangesmell.noguichest.util.AbstractNGRenderer;
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
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class NGBrewingStandBlockEntityRenderer implements BlockEntityRenderer<NGBrewingStandBlockEntity> {
    private final ItemRenderer itemRenderer;
    private static double onePix =0.0625;

    public NGBrewingStandBlockEntityRenderer(BlockEntityRendererProvider.Context p_173602_) {
        this.itemRenderer = p_173602_.getItemRenderer();
    }

    @Override
    public void render(NGBrewingStandBlockEntity ngBrewingStandBlockEntity , float pPartialTick, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level level = ngBrewingStandBlockEntity.getLevel();
        NonNullList<ItemStack> nonnulllist = ngBrewingStandBlockEntity.getItems();
        int i = (int)ngBrewingStandBlockEntity.getBlockPos().asLong();
        HitResult hitResult = Minecraft.getInstance().player.pick(5, pPartialTick,false);
        Vec3 viewPose =hitResult.getLocation();
        BlockPos blockPos = ngBrewingStandBlockEntity.getBlockPos();
        double dx =viewPose.get(Direction.Axis.X)-blockPos.getX();
        double dz =viewPose.get(Direction.Axis.Z)-blockPos.getZ();
        double dy =viewPose.get(Direction.Axis.Y)-blockPos.getY();
        for(int j = 0; j < nonnulllist.size(); ++j) {
            ItemStack itemstack = nonnulllist.get(j);
            if (itemstack != ItemStack.EMPTY) {
                poseStack.pushPose();
                int sum=i+j;
                switch (j){
                    case 0->{
                        poseStack.pushPose();
                        poseStack.translate(0.25, 0.3, 0.25);
                        poseStack.scale(0.375F, 0.375F, 0.375F);
                        poseStack.mulPose(Axis.YP.rotationDegrees(level.getGameTime()));
                        if(dx<onePix*6&&dz<onePix*6&&dx>onePix&&dz>onePix){
                            poseStack.scale(1.25f,1.25f,1.25f);
                        }
                        this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, poseStack, pBuffer, level, sum);
                        poseStack.popPose();
                    }
                    case 1->{
                        poseStack.pushPose();
                        poseStack.translate(0.25, 0.3, 0.75);
                        poseStack.scale(0.375F, 0.375F, 0.375F);
                        poseStack.mulPose(Axis.YP.rotationDegrees(level.getGameTime()));
                        if(dx<onePix*6&&dz<onePix*15&&dx>onePix&&dz>10*onePix){
                            poseStack.scale(1.25f,1.25f,1.25f);
                        }
                        this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, poseStack, pBuffer, level, sum);
                        poseStack.popPose();
                    }
                    case 2->{
                        poseStack.pushPose();
                        poseStack.translate(0.75, 0.3, 0.5F);
                        poseStack.scale(0.375F, 0.375F, 0.375F);
                        poseStack.mulPose(Axis.YP.rotationDegrees(level.getGameTime()));
                        if(dx<onePix*15&&dz<onePix*11&&dx>10*onePix&&dz>5*onePix){
                            poseStack.scale(1.25f,1.25f,1.25f);
                        }
                        this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, poseStack, pBuffer, level, sum);
                        poseStack.popPose();
                    }
                    case 3->{
                        poseStack.pushPose();
                        poseStack.translate(0.5, 1, 0.5);
                        poseStack.scale(0.375F, 0.375F, 0.375F);
                        poseStack.mulPose(Axis.YP.rotationDegrees(level.getGameTime()));
                        if(dx<onePix*10&&dz<onePix*10&&dx>6*onePix&&dz>6*onePix&&dy>6*onePix){
                            poseStack.scale(1.25f,1.25f,1.25f);
                        }
                        this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, poseStack, pBuffer, level, sum);
                        poseStack.popPose();
                        /*
                        poseStack.pushPose();
                        poseStack.translate(0.5, (ngBrewingStandBlockEntity.dataAccess.get(0)*0.75)/400+0.25, 0.5);
                        poseStack.scale(0.375F, 0.375F, 0.375F);
                        poseStack.mulPose(Axis.YP.rotationDegrees(level.getGameTime()));
                        if(dx<onePix*10&&dz<onePix*10&&dx>6*onePix&&dz>6*onePix&&dy>6*onePix){
                            poseStack.scale(1.25f,1.25f,1.25f);
                        }
                        this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, poseStack, pBuffer, level, sum);
                        poseStack.popPose();
                        if(nonnulllist.get(3).getCount()>1){
                            poseStack.pushPose();
                            poseStack.translate(0.5, 1, 0.5);
                            poseStack.scale(0.375F, 0.375F, 0.375F);
                            poseStack.mulPose(Axis.YP.rotationDegrees(level.getGameTime()));
                            if(dx<onePix*10&&dz<onePix*10&&dx>6*onePix&&dz>6*onePix&&dy>6*onePix){
                                poseStack.scale(1.25f,1.25f,1.25f);
                            }
                            this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, poseStack, pBuffer, level, sum);
                            poseStack.popPose();
                        }
*/

                    }
                    case 4->{
                        poseStack.pushPose();
                        poseStack.translate(0.5, 0.25, 0.5);
                        poseStack.scale(0.375F, 0.375F, 0.375F);
                        poseStack.mulPose(Axis.YP.rotationDegrees(level.getGameTime()));
                        if(dx<onePix*10&&dz<onePix*10&&dx>6*onePix&&dz>6*onePix&&dy<6*onePix){
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
