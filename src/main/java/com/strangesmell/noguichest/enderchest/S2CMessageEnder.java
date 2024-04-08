package com.strangesmell.noguichest.enderchest;

import com.strangesmell.noguichest.channel.ClientPacketHandler;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CMessageEnder {
    private NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
    public S2CMessageEnder(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public NonNullList<ItemStack> getItems(){
        return this.items;
    }

    public S2CMessageEnder(FriendlyByteBuf buf) {
        for(int i = 0 ; i < items.size() ; i++){
            items.set(i,buf.readItem());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        for(int i = 0 ; i < items.size() ; i++){
            buf.writeItemStack(items.get(i),true);
        }
    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // 确保其仅在物理客户端上执行
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientPacketHandler.handlePacketEnder(new S2CMessageEnder(items), supplier));
        });
        context.setPacketHandled(true);
        return true;
    }
}
