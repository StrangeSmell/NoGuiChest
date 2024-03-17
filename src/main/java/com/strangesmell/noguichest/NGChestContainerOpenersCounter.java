package com.strangesmell.noguichest;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;

public abstract class NGChestContainerOpenersCounter {
    private int openCount;
    protected abstract void onOpen(Level pLevel, BlockPos pPos, BlockState pState);
    protected abstract void onClose(Level pLevel, BlockPos pPos, BlockState pState);
    protected abstract void openerCountChanged(Level pLevel, BlockPos pPos, BlockState pState, int pCount, int pOpenCount);
    protected abstract boolean isOwnContainer(Player p_155451_);

    public void incrementOpeners(Player pPlayer, Level pLevel, BlockPos pPos, BlockState pState) {
        int i = this.openCount++;
        if (i == 0) {
            this.onOpen(pLevel, pPos, pState);
            pLevel.gameEvent(pPlayer, GameEvent.CONTAINER_OPEN, pPos);
            NGChestEntity ngChestEntity = (NGChestEntity) pLevel.getBlockEntity(pPos);
            ngChestEntity.getOpenState();
            //scheduleRecheck(pLevel, pPos, pState);
        }

        this.openerCountChanged(pLevel, pPos, pState, i, this.openCount);
    }

    public void decrementOpeners(Player pPlayer, Level pLevel, BlockPos pPos, BlockState pState) {
        int i = this.openCount--;
        if (this.openCount == 0) {
            this.onClose(pLevel, pPos, pState);
            pLevel.gameEvent(pPlayer, GameEvent.CONTAINER_CLOSE, pPos);
        }

        this.openerCountChanged(pLevel, pPos, pState, i, this.openCount);
    }

    private int getOpenCount(Level pLevel, BlockPos pPos) {
        int i = pPos.getX();
        int j = pPos.getY();
        int k = pPos.getZ();
        float f = 5.0F;
        AABB aabb = new AABB((double)((float)i - 5.0F), (double)((float)j - 5.0F), (double)((float)k - 5.0F), (double)((float)(i + 1) + 5.0F), (double)((float)(j + 1) + 5.0F), (double)((float)(k + 1) + 5.0F));
        return pLevel.getEntities(EntityTypeTest.forClass(Player.class), aabb, this::isOwnContainer).size();
    }

    public void recheckOpeners(Level pLevel, BlockPos pPos, BlockState pState) {
        int i = this.getOpenCount(pLevel, pPos);
        int j = this.openCount;
        if (j != i) {
            boolean flag = i != 0;
            boolean flag1 = j != 0;
            if (flag && !flag1) {
                this.onOpen(pLevel, pPos, pState);
                pLevel.gameEvent((Entity)null, GameEvent.CONTAINER_OPEN, pPos);
            } else if (!flag) {
                this.onClose(pLevel, pPos, pState);
                pLevel.gameEvent((Entity)null, GameEvent.CONTAINER_CLOSE, pPos);
            }

            this.openCount = i;
        }

        this.openerCountChanged(pLevel, pPos, pState, j, i);
        if (i > 0) {
            scheduleRecheck(pLevel, pPos, pState);
        }

    }

    public int getOpenerCount() {
        return this.openCount;
    }

    public static void scheduleRecheck(Level pLevel, BlockPos pPos, BlockState pState) {
        pLevel.scheduleTick(pPos, pState.getBlock(), 5);
    }
}
