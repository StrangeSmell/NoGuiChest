package com.strangesmell.noguichest.channel;

import com.strangesmell.noguichest.brewing.NGBrewingStandBlockEntity;
import com.strangesmell.noguichest.brewing.S2CMessageBrewing;
import com.strangesmell.noguichest.chest.NGChestEntity;
import com.strangesmell.noguichest.chest.S2CMessage;
import com.strangesmell.noguichest.dispenser.NGDispenserEntity;
import com.strangesmell.noguichest.dispenser.S2CMessageDispenser;
import com.strangesmell.noguichest.dropper.NGDropperBlockEntity;
import com.strangesmell.noguichest.dropper.S2CMessageDropper;
import com.strangesmell.noguichest.enderchest.S2CMessageEnder;
import com.strangesmell.noguichest.hopper.NGHopperBlockEntity;
import com.strangesmell.noguichest.hopper.S2CMessageHopper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPacketHandler {

    public static void handlePacket(S2CMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        assert clientLevel != null;
        BlockEntity blockEntity = clientLevel.getBlockEntity(msg.getBlockPos());
        if(blockEntity instanceof NGChestEntity ngChestEntity){
            ngChestEntity.setItems(msg.getItems());
            ngChestEntity.setOpenState(msg.getOpenState());
        }

    }
    public static void handlePacket(S2CMessageHopper msg, Supplier<NetworkEvent.Context> ctx) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        assert clientLevel != null;
        BlockEntity blockEntity = clientLevel.getBlockEntity(msg.getBlockPos());
        if(blockEntity instanceof NGHopperBlockEntity ngHopperBlockEntity){
            ngHopperBlockEntity.setItems(msg.getItems());
        }

    }
    public static void handlePacketBrew(S2CMessageBrewing msg, Supplier<NetworkEvent.Context> ctx) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        assert clientLevel != null;
        BlockEntity blockEntity = clientLevel.getBlockEntity(msg.getBlockPos());
        if(blockEntity instanceof NGBrewingStandBlockEntity ngBrewingStandBlockEntity){
            ngBrewingStandBlockEntity.setItems(msg.getItems());
        }

    }

    public static void handlePacketDispenser(S2CMessageDispenser msg, Supplier<NetworkEvent.Context> ctx) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        assert clientLevel != null;
        BlockEntity blockEntity = clientLevel.getBlockEntity(msg.getBlockPos());
        if(blockEntity instanceof NGDispenserEntity ngDispenserEntity){
            ngDispenserEntity.setItems(msg.getItems());
        }

    }
    public static void handlePacketDropper(S2CMessageDropper msg, Supplier<NetworkEvent.Context> ctx) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        assert clientLevel != null;
        BlockEntity blockEntity = clientLevel.getBlockEntity(msg.getBlockPos());
        if(blockEntity instanceof NGDropperBlockEntity ngDropperBlockEntity){
            ngDropperBlockEntity.setItems(msg.getItems());
        }

    }

    public static void handlePacketEnder(S2CMessageEnder msg, Supplier<NetworkEvent.Context> ctx) {
        LocalPlayer player = Minecraft.getInstance().player;
        assert player != null;
        for(int index =0 ;index<=msg.getItems().size();index++){
            player.getEnderChestInventory().setItem(index,msg.getItems().get(index));
        }
    }

    public static void handlePacket(Issues4Message msg, Supplier<NetworkEvent.Context> ctx) {
        BlockEntity blockEntity = ctx.get().getSender().level().getBlockEntity(msg.getBlockPos());
        if(blockEntity instanceof NGChestEntity ngChestEntity){
            ngChestEntity.setChanged();
        }
    }
}
