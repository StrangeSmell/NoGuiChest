package com.strangesmell.noguichest;

import com.mojang.datafixers.DSL;
import com.strangesmell.noguichest.brewing.NGBrewingStandBlock;
import com.strangesmell.noguichest.brewing.NGBrewingStandBlockEntity;
import com.strangesmell.noguichest.channel.Channel;
import com.strangesmell.noguichest.chest.*;
import com.strangesmell.noguichest.dispenser.NGDispenser;
import com.strangesmell.noguichest.dispenser.NGDispenserEntity;
import com.strangesmell.noguichest.dropper.NGDropperBlock;
import com.strangesmell.noguichest.dropper.NGDropperBlockEntity;
import com.strangesmell.noguichest.enderchest.NGEnderChest;
import com.strangesmell.noguichest.enderchest.NGEnderChestBlockEntity;
import com.strangesmell.noguichest.enderchest.NGEnderItem;
import com.strangesmell.noguichest.hopper.*;
import com.strangesmell.noguichest.hopper.NGHopperBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.CreativeModeTabRegistry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(NoGuiChest.MODID)
public class NoGuiChest
{
    public static final String MODID = "noguichest";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<Block> NGChest = BLOCKS.register("no_gui_chest",NGChest::new);
    public static final RegistryObject<Block> NGEnder = BLOCKS.register("no_gui_ender", NGEnderChest::new);
    public static final RegistryObject<Block> NGHopper = BLOCKS.register("no_gui_hopper",NGHopper::new);
    public static final RegistryObject<Block> NGBrew = BLOCKS.register("no_gui_brew", NGBrewingStandBlock::new);
    public static final RegistryObject<Block> NGDispenser = BLOCKS.register("no_gui_dispenser", NGDispenser::new);
    public static final RegistryObject<Block> NGDropper = BLOCKS.register("no_gui_dropper", NGDropperBlock::new);

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final RegistryObject<Item> NGChestItem = ITEMS.register("no_gui_chest_item",()->new NGChestItem(NGChest.get(),new Item.Properties()));
    public static final RegistryObject<Item> NGEnderItem = ITEMS.register("no_gui_ender_item",()->new NGEnderItem(NGEnder.get(),new Item.Properties()));
    public static final RegistryObject<Item> NGHopperItem = ITEMS.register("no_gui_hopper_item",()->new BlockItem(NGHopper.get(),new Item.Properties()));
    public static final RegistryObject<Item> NGBrewItem = ITEMS.register("no_gui_brew_item",()->new BlockItem(NGBrew.get(),new Item.Properties()));
    public static final RegistryObject<Item> NGDispenserItem = ITEMS.register("no_gui_dispenser_item",()->new BlockItem(NGDispenser.get(),new Item.Properties()));
    public static final RegistryObject<Item> NGDropperItem = ITEMS.register("no_gui_dropper_item",()->new BlockItem(NGDropper.get(),new Item.Properties()));

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);
    public static final RegistryObject<BlockEntityType<NGChestEntity>> NGChestEntity = BLOCK_ENTITIES.register("no_gui_chest_entity", () -> BlockEntityType.Builder.of(NGChestEntity::new, NGChest.get()).build(DSL.remainderType()));
    public static final RegistryObject<BlockEntityType<NGEnderChestBlockEntity>> NGEnderEntity = BLOCK_ENTITIES.register("no_gui_ender_entity", () -> BlockEntityType.Builder.of(NGEnderChestBlockEntity::new, NGEnder.get()).build(DSL.remainderType()));
    public static final RegistryObject<BlockEntityType<NGHopperBlockEntity>> NGHopperEntity = BLOCK_ENTITIES.register("no_gui_hopper_entity", () -> BlockEntityType.Builder.of(NGHopperBlockEntity::new, NGHopper.get()).build(DSL.remainderType()));
    public static final RegistryObject<BlockEntityType<NGBrewingStandBlockEntity>> NGBrewEntity = BLOCK_ENTITIES.register("no_gui_brew_entity", () -> BlockEntityType.Builder.of(NGBrewingStandBlockEntity::new, NGBrew.get()).build(DSL.remainderType()));
    public static final RegistryObject<BlockEntityType<NGDispenserEntity>> NGDispenserEntity = BLOCK_ENTITIES.register("no_gui_dispenser_entity", () -> BlockEntityType.Builder.of(NGDispenserEntity::new, NGDispenser.get()).build(DSL.remainderType()));
    public static final RegistryObject<BlockEntityType<NGDropperBlockEntity>> NGDropperEntity = BLOCK_ENTITIES.register("no_gui_dropper_entity", () -> BlockEntityType.Builder.of(NGDropperBlockEntity::new, NGDropper.get()).build(DSL.remainderType()));

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final RegistryObject<CreativeModeTab> NO_GUI_TAB = CREATIVE_MODE_TABS.register("no_gui_tab", () -> CreativeModeTab.builder()
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> NGChestItem.get().getDefaultInstance())
            .title( Component.translatable("NoGuiChest"))
            .displayItems((parameters, output) -> {
                output.accept(NGChestItem.get());
                //output.accept(NGEnderItem.get());
                output.accept(NGBrewItem.get());
                output.accept(NGHopperItem.get());
                output.accept(NGDispenserItem.get());
                output.accept(NGDropperItem.get());
            }).build());



    public NoGuiChest()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        CREATIVE_MODE_TABS.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::clientSetup);
        Channel.register();
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(NoGuiChest.NGBrew.get(), RenderType.cutout());
        });
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS){
            event.accept(NGChestItem);
            event.accept(NGBrewItem);
        }

        if(event.getTabKey()==CreativeModeTabs.REDSTONE_BLOCKS)
            event.accept(NGHopperItem);
    }

}
