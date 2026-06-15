package noobanidus.mods.lootr.setup;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import noobanidus.mods.lootr.Lootr;
import noobanidus.mods.lootr.client.ClientGetter;
import noobanidus.mods.lootr.client.block.SpecialLootChestTileRenderer;
import noobanidus.mods.lootr.client.entity.LootrMinecartRenderer;
import noobanidus.mods.lootr.block.tile.LootrChestTileEntity;
import noobanidus.mods.lootr.block.tile.LootrInventoryTileEntity;
import noobanidus.mods.lootr.block.tile.TrappedLootrChestTileEntity;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;

public class ClientSetup extends CommonSetup {
    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);

        ClientRegistry.bindTileEntitySpecialRenderer(LootrChestTileEntity.class, new SpecialLootChestTileRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(LootrInventoryTileEntity.class, new SpecialLootChestTileRenderer());
        ClientRegistry.bindTileEntitySpecialRenderer(TrappedLootrChestTileEntity.class, new SpecialLootChestTileRenderer());

        RenderingRegistry.registerEntityRenderingHandler(LootrChestMinecartEntity.class, new LootrMinecartRenderer());
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);
    }

    @Override
    public EntityPlayer getPlayer() {
        return ClientGetter.getPlayer();
    }

    @Override
    public void changeCartStatus(int entityId, boolean status) {
        Minecraft.getMinecraft().func_152344_a(() -> {
            World world = Minecraft.getMinecraft().theWorld;
            if (world == null) {
                Lootr.LOG.info("Unable to mark entity with id '" + entityId + "' as opened as world is null.");
                return;
            }
            Entity cart = world.getEntityByID(entityId);
            if (cart == null) {
                Lootr.LOG.info("Unable to mark entity with id '" + entityId + "' as opened as entity is null.");
                return;
            }
            if (!(cart instanceof LootrChestMinecartEntity)) {
                Lootr.LOG.info("Unable to mark entity with id '" + entityId + "' as opened as entity is not a Lootr minecart.");
                return;
            }
            if (status)
                ((LootrChestMinecartEntity) cart).setOpened();
            else
                ((LootrChestMinecartEntity) cart).setClosed();
        });
    }
}
