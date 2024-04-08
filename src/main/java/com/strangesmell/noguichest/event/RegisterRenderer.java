package com.strangesmell.noguichest.event;

import com.strangesmell.noguichest.NoGuiChest;
import com.strangesmell.noguichest.brewing.NGBrewingStandBlockEntityRenderer;
import com.strangesmell.noguichest.chest.NGChestRenderer;
import com.strangesmell.noguichest.dispenser.NGDispenserEntityRenderer;
import com.strangesmell.noguichest.dropper.NGDropperBlockEntityRenderer;
import com.strangesmell.noguichest.enderchest.NGEnderRenderer;
import com.strangesmell.noguichest.hopper.NGHopperRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)

public class RegisterRenderer {
    @SubscribeEvent
    public static void  registerRenderer(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(NoGuiChest.NGChestEntity.get(), NGChestRenderer::new);
        event.registerBlockEntityRenderer(NoGuiChest.NGEnderEntity.get(), NGEnderRenderer::new);
        event.registerBlockEntityRenderer(NoGuiChest.NGHopperEntity.get(), NGHopperRenderer::new);
        event.registerBlockEntityRenderer(NoGuiChest.NGBrewEntity.get(), NGBrewingStandBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(NoGuiChest.NGDispenserEntity.get(), NGDispenserEntityRenderer::new);
        event.registerBlockEntityRenderer(NoGuiChest.NGDropperEntity.get(), NGDropperBlockEntityRenderer::new);
    }
}
