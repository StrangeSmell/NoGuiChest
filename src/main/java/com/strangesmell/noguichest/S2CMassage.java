package com.strangesmell.noguichest;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CMassage {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    private BlockPos blockPos ;
    private boolean openState;
    public S2CMassage(NonNullList<ItemStack> items,BlockPos blockPos,boolean openState) {
        this.blockPos=blockPos;
        this.items = items;
        this.openState=openState;
    }

    public NonNullList<ItemStack> getItems(){
        return this.items;
    }

    public BlockPos getBlockPos(){
        return this.blockPos;
    }

    public boolean getOpenState(){
        return this.openState;
    }

    public S2CMassage(FriendlyByteBuf buf) {
        blockPos=buf.readBlockPos();
        for(int i = 0 ; i < items.size() ; i++){
            items.set(i,buf.readItem());
        }
        openState=buf.readBoolean();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        for(int i = 0 ; i < items.size() ; i++){
            buf.writeItemStack(items.get(i),true);
        }
        buf.writeBoolean(openState);
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // 确保其仅在物理客户端上执行
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handlePacket(new S2CMassage(items,blockPos,openState), supplier));
        });
        context.setPacketHandled(true);
        return true;
    }


}
