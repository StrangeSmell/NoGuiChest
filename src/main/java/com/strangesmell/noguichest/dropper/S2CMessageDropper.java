package com.strangesmell.noguichest.dropper;

import com.strangesmell.noguichest.channel.ClientPacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CMessageDropper {
    private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
    private BlockPos blockPos ;
    public S2CMessageDropper(NonNullList<ItemStack> items, BlockPos blockPos) {
        this.blockPos=blockPos;
        this.items = items;
    }

    public NonNullList<ItemStack> getItems(){
        return this.items;
    }

    public BlockPos getBlockPos(){
        return this.blockPos;
    }


    public S2CMessageDropper(FriendlyByteBuf buf) {
        blockPos=buf.readBlockPos();
        for(int i = 0 ; i < items.size() ; i++){
            items.set(i,buf.readItem());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(blockPos);
        for(int i = 0 ; i < items.size() ; i++){
            buf.writeItemStack(items.get(i),true);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // 确保其仅在物理客户端上执行
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handlePacketDropper(new S2CMessageDropper(items,blockPos), supplier));
        });
        context.setPacketHandled(true);
        return true;
    }
}
