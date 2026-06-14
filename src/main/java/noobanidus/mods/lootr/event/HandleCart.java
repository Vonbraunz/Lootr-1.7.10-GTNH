package noobanidus.mods.lootr.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.MathHelper;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.ChunkEvent;
import noobanidus.mods.lootr.config.ConfigManager;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;
import noobanidus.mods.lootr.networking.OpenCart;
import noobanidus.mods.lootr.networking.PacketHandler;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

public class HandleCart {
    private static Set<Chunk> loadedChunks = Collections.newSetFromMap(new WeakHashMap<Chunk, Boolean>());

    @SubscribeEvent
    public void chunkLoad(ChunkEvent.Load event) {
        loadedChunks.add(event.getChunk());
    }

    @SubscribeEvent
    public void chunkUnload(ChunkEvent.Unload event) {
        loadedChunks.remove(event.getChunk());
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityMinecartChest) {
            EntityMinecartChest chest = (EntityMinecartChest) event.entity;
            if (!chest.worldObj.isRemote && ConfigManager.CONVERT_MINESHAFTS) {
                LootrChestMinecartEntity lootr = new LootrChestMinecartEntity(chest.worldObj, chest.posX, chest.posY, chest.posZ);
                for (int i = 0; i < chest.getSizeInventory() && i < lootr.getSizeInventory(); i++) {
                    ItemStack stack = chest.getStackInSlot(i);
                    if (stack != null) lootr.setInventorySlotContents(i, stack.copy());
                }
                int chunkX = MathHelper.floor_double(chest.posX) >> 4;
                int chunkZ = MathHelper.floor_double(chest.posZ) >> 4;
                Chunk chunk = chest.worldObj.getChunkFromChunkCoords(chunkX, chunkZ);
                if (loadedChunks.contains(chunk)) {
                    chest.worldObj.spawnEntityInWorld(lootr);
                } else {
                    chunk.addEntity(lootr);
                }
                event.setCanceled(true);
                chest.setDead();
            }
        }
    }

    @SubscribeEvent
    public void onEntityTrack(PlayerEvent.StartTracking event) {
        Entity target = event.target;
        if (target instanceof LootrChestMinecartEntity) {
            EntityPlayer player = event.entityPlayer;
            if (((LootrChestMinecartEntity) event.target).getOpeners().contains(player.getUniqueID())) {
                OpenCart cart = new OpenCart(event.target.getEntityId());
                PacketHandler.sendToInternal(cart, (EntityPlayerMP) player);
            }
        }
    }
}
