package com.strangesmell.noguichest;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPacketHandler {

    public static void handlePacket(S2CMassage msg, Supplier<NetworkEvent.Context> ctx) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        assert clientLevel != null;
        BlockEntity blockEntity = clientLevel.getBlockEntity(msg.getBlockPos());
        if(blockEntity instanceof NGChestEntity ngChestEntity){
            ngChestEntity.setItems(msg.getItems());
            ngChestEntity.setOpenState(msg.getOpenState());
        }

    }
}
