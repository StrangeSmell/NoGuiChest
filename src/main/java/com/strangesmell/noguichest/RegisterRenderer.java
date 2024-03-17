package com.strangesmell.noguichest;

import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)

public class RegisterRenderer {
    @SubscribeEvent
    public static void  registerRenderer(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(NoGuiChest.NGChestEntity.get(),NGChestRenderer::new);
    }
}
