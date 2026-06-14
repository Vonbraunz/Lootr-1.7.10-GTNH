package noobanidus.mods.lootr.setup;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.common.MinecraftForge;
import noobanidus.mods.lootr.Lootr;
import noobanidus.mods.lootr.config.ConfigManager;
import noobanidus.mods.lootr.data.DataStorage;
import noobanidus.mods.lootr.event.HandleBreak;
import noobanidus.mods.lootr.event.HandleCart;
import noobanidus.mods.lootr.event.HandleWorldGen;
import noobanidus.mods.lootr.init.ModBlocks;
import noobanidus.mods.lootr.init.ModEntities;
import noobanidus.mods.lootr.init.ModTiles;
import noobanidus.mods.lootr.networking.PacketHandler;

public abstract class CommonSetup {
    public void preInit(FMLPreInitializationEvent event) {
        ConfigManager.init(event.getSuggestedConfigurationFile());
        ModBlocks.init();
        ModTiles.init();
        ModEntities.init();
        MinecraftForge.EVENT_BUS.register(new HandleBreak());
        MinecraftForge.EVENT_BUS.register(new HandleCart());
        HandleWorldGen worldGen = new HandleWorldGen();
        MinecraftForge.EVENT_BUS.register(worldGen);
        FMLCommonHandler.instance().bus().register(worldGen);
    }

    public abstract EntityPlayer getPlayer();

    public void init(FMLInitializationEvent event) {
        PacketHandler.registerMessages();
        FMLCommonHandler.instance().bus().register(this);
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent e) {
        if (e.phase == TickEvent.Phase.END) {
            DataStorage.doDecay();
            DataStorage.doRefresh();
        }
    }

    public abstract void changeCartStatus(int entityId, boolean status);
}
