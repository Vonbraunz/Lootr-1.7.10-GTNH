package noobanidus.mods.lootr.event;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.block.BlockChest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import noobanidus.mods.lootr.Lootr;
import noobanidus.mods.lootr.api.tile.ILootTile;
import noobanidus.mods.lootr.block.tile.LootrInventoryTileEntity;
import noobanidus.mods.lootr.config.ConfigManager;
import noobanidus.mods.lootr.init.ModBlocks;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.ref.WeakReference;
import java.util.*;

public class HandleWorldGen {
    private static LinkedList<Pair<WeakReference<World>, int[]>> generatedChunks = new LinkedList<>();
    private static boolean stopItemSpawning = false;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onGenerate(PopulateChunkEvent.Post event) {
        if (ConfigManager.CONVERT_WORLDGEN_INVENTORIES) {
            generatedChunks.add(Pair.of(new WeakReference<>(event.world), new int[]{event.chunkX, event.chunkZ}));
        }
    }

    @SubscribeEvent
    public void onItemJoin(EntityJoinWorldEvent event) {
        if (stopItemSpawning && event.entity instanceof EntityItem) {
            event.setCanceled(true);
        }
    }

    private static TileEntity replaceOldLootBlockAt(Chunk chunk, int x, int y, int z) {
        World world = chunk.worldObj;
        stopItemSpawning = true;
        try {
            world.setBlock(x, y, z, ModBlocks.INVENTORY, 0, 2);
            world.removeTileEntity(x, y, z);
            return chunk.func_150806_e(x, y, z);
        } catch (RuntimeException e) {
            Lootr.LOG.error("Couldn't replace loot block", e);
            return null;
        } finally {
            stopItemSpawning = false;
        }
    }

    private static void processChunkForWorldgen(Chunk chunk) {
        List<Map.Entry<net.minecraft.world.ChunkPosition, TileEntity>> entries = new ArrayList<>(chunk.chunkTileEntityMap.entrySet());
        for (Map.Entry<net.minecraft.world.ChunkPosition, TileEntity> entry : entries) {
            TileEntity te = entry.getValue();
            if (te instanceof TileEntityChest && !(te instanceof ILootTile)) {
                TileEntityChest teChest = (TileEntityChest) te;
                int size = teChest.getSizeInventory();
                List<ItemStack> newInventory = new ArrayList<>();
                for (int slot = 0; slot < size; slot++) {
                    ItemStack stack = teChest.getStackInSlot(slot);
                    if (stack != null && (size <= 27 || stack.stackSize > 0)) {
                        newInventory.add(stack.copy());
                    }
                    teChest.setInventorySlotContents(slot, null);
                }
                if (!newInventory.isEmpty()) {
                    int x = teChest.xCoord, y = teChest.yCoord, z = teChest.zCoord;
                    int meta = chunk.getBlockMetadata(x, y, z);
                    int facing = meta & 3;
                    teChest.closeInventory();
                    int newMeta = ModBlocks.INVENTORY_OFFSET + facing;
                    te = replaceOldLootBlockAt(chunk, x, y, z);
                    if (te instanceof LootrInventoryTileEntity) {
                        LootrInventoryTileEntity inventory = (LootrInventoryTileEntity) te;
                        inventory.setCustomInventory(newInventory.toArray(new ItemStack[0]));
                        inventory.markDirty();
                    } else {
                        Lootr.LOG.error("replacement TE " + te + " is not a LootrInventoryTileEntity dim " + chunk.worldObj.provider.dimensionId + " at " + x + "," + y + "," + z);
                    }
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (ConfigManager.CONVERT_WORLDGEN_INVENTORIES && event.phase == TickEvent.Phase.END && !generatedChunks.isEmpty()) {
            List<Chunk> processing = new ArrayList<>();
            Iterator<Pair<WeakReference<World>, int[]>> iter = generatedChunks.iterator();
            while (iter.hasNext()) {
                Pair<WeakReference<World>, int[]> pair = iter.next();
                World world = pair.getLeft().get();
                if (world == null) {
                    iter.remove();
                    continue;
                }
                int[] coords = pair.getRight();
                Chunk chunk = world.getChunkProvider().provideChunk(coords[0], coords[1]);
                if (chunk == null) continue;
                processing.add(chunk);
                iter.remove();
            }
            for (Chunk chunk : processing) {
                processChunkForWorldgen(chunk);
            }
        }
    }
}
