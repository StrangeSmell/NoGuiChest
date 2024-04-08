package com.strangesmell.noguichest.dispenser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
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

public class NGDispenserEntityRenderer implements BlockEntityRenderer<NGDispenserEntity> {
    private final ItemRenderer itemRenderer;
    private static double onePix =0.0625;
    private static double oneSlot =0.2;

    public NGDispenserEntityRenderer(BlockEntityRendererProvider.Context p_173602_) {
        this.itemRenderer = p_173602_.getItemRenderer();
    }

    @Override
    public void render(NGDispenserEntity ngDispenserEntity, float pPartialTick, PoseStack poseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        Level level = ngDispenserEntity.getLevel();

        NonNullList<ItemStack> nonnulllist = ngDispenserEntity.getItems();
        int i = (int)ngDispenserEntity.getBlockPos().asLong();
        HitResult hitResult = Minecraft.getInstance().player.pick(5, pPartialTick,false);
        Vec3 viewPose =hitResult.getLocation();
        BlockPos blockPos = ngDispenserEntity.getBlockPos();
        double dx =viewPose.get(Direction.Axis.X)-blockPos.getX()-3.2*onePix;
        double dz =viewPose.get(Direction.Axis.Z)-blockPos.getZ()-3.2*onePix;
        int line,row;
        line=(int)(dz/0.2);
        row=(int) (dx/0.2);
        for(int j = 0; j < nonnulllist.size(); ++j) {
            ItemStack itemstack = nonnulllist.get(j);
            if (itemstack != ItemStack.EMPTY) {
                poseStack.pushPose();
                poseStack.translate(0.3, 1.05, 0.3);
                poseStack.translate(0.2*(int)(j%3), 0, 0.2*(int)(j/3));
                poseStack.scale(0.25F, 0.25F, 0.25F);
                poseStack.mulPose(Axis.YP.rotationDegrees(level.getGameTime()));
                if(j==(line*3+row)&&line>=0&&row>=0&&line<4&&row<4){
                    poseStack.scale(1.25f,1.25f,1.25f);
                }
                this.itemRenderer.renderStatic(itemstack, ItemDisplayContext.FIXED,LevelRenderer.getLightColor(level, ngDispenserEntity.getBlockPos().offset(0,1,0)), pPackedOverlay, poseStack, pBuffer, level, j+i);
                poseStack.popPose();
            }
        }
    }
}
