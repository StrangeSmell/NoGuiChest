package com.strangesmell.noguichest.channel;


import com.strangesmell.noguichest.NoGuiChest;
import com.strangesmell.noguichest.brewing.S2CMessageBrewing;
import com.strangesmell.noguichest.chest.S2CMessage;
import com.strangesmell.noguichest.dispenser.S2CMessageDispenser;
import com.strangesmell.noguichest.dropper.S2CMessageDropper;
import com.strangesmell.noguichest.enderchest.S2CMessageEnder;
import com.strangesmell.noguichest.hopper.S2CMessageHopper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class Channel {
    private static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }


    public static void register() {
    SimpleChannel net = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(NoGuiChest.MODID, "messages"))
            .networkProtocolVersion(() -> "1.0")
            .clientAcceptedVersions(s -> true)
            .serverAcceptedVersions(s -> true)
            .simpleChannel();

    INSTANCE =net;

        net.messageBuilder(S2CMessage.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CMessage::new)
                .encoder(S2CMessage::toBytes)
                .consumerMainThread(S2CMessage::handle)
                .add();
        net.messageBuilder(S2CMessageHopper.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CMessageHopper::new)
                .encoder(S2CMessageHopper::toBytes)
                .consumerMainThread(S2CMessageHopper::handle)
                .add();
        net.messageBuilder(S2CMessageBrewing.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CMessageBrewing::new)
                .encoder(S2CMessageBrewing::toBytes)
                .consumerMainThread(S2CMessageBrewing::handle)
                .add();
        net.messageBuilder(S2CMessageDispenser.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CMessageDispenser::new)
                .encoder(S2CMessageDispenser::toBytes)
                .consumerMainThread(S2CMessageDispenser::handle)
                .add();
        net.messageBuilder(S2CMessageDropper.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CMessageDropper::new)
                .encoder(S2CMessageDropper::toBytes)
                .consumerMainThread(S2CMessageDropper::handle)
                .add();
        net.messageBuilder(S2CMessageEnder.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(S2CMessageEnder::new)
                .encoder(S2CMessageEnder::toBytes)
                .consumerMainThread(S2CMessageEnder::handle)
                .add();
        net.messageBuilder(Issues4Message.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(Issues4Message::new)
                .encoder(Issues4Message::toBytes)
                .consumerMainThread(Issues4Message::handle)
                .add();


    }

    public static <MSG> void sendToServer(MSG message) {
        INSTANCE.sendToServer(message);
    }

    public static <MSG> void sendToPlayer(MSG message, ServerPlayer player) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), message);
    }

    public static <MSG> void sendToChunk(MSG message , LevelChunk levelChunk) {
       INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(()->levelChunk), message);
    }

    public static <MSG> void sendToNear(MSG message ) {
        INSTANCE.send(PacketDistributor.NEAR.noArg(), message);
    }

    public static <MSG> void sendToClients(MSG message) {
        INSTANCE.send(PacketDistributor.ALL.noArg(), message);
    }


}

