package noobanidus.mods.lootr.data;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.MapStorage;
import noobanidus.mods.lootr.Lootr;
import noobanidus.mods.lootr.api.LootFiller;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;

import javax.annotation.Nullable;
import java.io.File;
import java.util.UUID;

public class DataStorage {
    public static final String DECAY = "lootr/Lootr-DecayData";
    public static final String REFRESH = "lootr/Lootr-RefreshData";

    private static final java.util.Set<UUID> knownChestIds = java.util.Collections.synchronizedSet(new java.util.LinkedHashSet<>());

    private static void ensureChestDataDir(WorldServer world) {
        File dir = new File(world.getSaveHandler().getWorldDirectory(), "data/lootr");
        dir.mkdirs();
    }

    private static <T extends WorldSavedData> T computeIfAbsentManager(MapStorage manager, T template, String key) {
        @SuppressWarnings("unchecked")
        T value = (T) manager.loadData(template.getClass(), key);
        if (value == null) {
            manager.setData(key, template);
            value = (T) manager.loadData(template.getClass(), key);
        }
        return value;
    }

    public static WorldServer getWorldServer() {
        return FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(0);
    }

    public static int getDecayValue(UUID id) {
        TickingData data = computeIfAbsentManager(getWorldServer().mapStorage, new TickingData(DECAY), DECAY);
        return data.getValue(id);
    }

    public static boolean isDecayed(UUID id) {
        TickingData data = computeIfAbsentManager(getWorldServer().mapStorage, new TickingData(DECAY), DECAY);
        return data.isDone(id);
    }

    public static void setDecaying(UUID id, int decay) {
        TickingData data = computeIfAbsentManager(getWorldServer().mapStorage, new TickingData(DECAY), DECAY);
        data.setValue(id, decay);
        data.markDirty();
    }

    public static void removeDecayed(UUID id) {
        TickingData data = computeIfAbsentManager(getWorldServer().mapStorage, new TickingData(DECAY), DECAY);
        data.removeDone(id);
        data.markDirty();
    }

    public static void doDecay() {
        TickingData data = computeIfAbsentManager(getWorldServer().mapStorage, new TickingData(DECAY), DECAY);
        if (data.tick()) data.markDirty();
    }

    public static int getRefreshValue(UUID id) {
        TickingData data = computeIfAbsentManager(getWorldServer().mapStorage, new TickingData(REFRESH), REFRESH);
        return data.getValue(id);
    }

    public static boolean isRefreshed(UUID id) {
        TickingData data = computeIfAbsentManager(getWorldServer().mapStorage, new TickingData(REFRESH), REFRESH);
        return data.isDone(id);
    }

    public static void setRefreshing(UUID id, int decay) {
        TickingData data = computeIfAbsentManager(getWorldServer().mapStorage, new TickingData(REFRESH), REFRESH);
        data.setValue(id, decay);
        data.markDirty();
    }

    public static void removeRefreshed(UUID id) {
        TickingData data = computeIfAbsentManager(getWorldServer().mapStorage, new TickingData(REFRESH), REFRESH);
        data.removeDone(id);
        data.markDirty();
    }

    public static void doRefresh() {
        TickingData data = computeIfAbsentManager(getWorldServer().mapStorage, new TickingData(REFRESH), REFRESH);
        if (data.tick()) data.markDirty();
    }

    public static ChestData getInstanceUuid(WorldServer world, UUID id, int x, int y, int z) {
        ensureChestDataDir(world);
        knownChestIds.add(id);
        String key = ChestData.ID(id);
        return computeIfAbsentManager(getWorldServer().mapStorage, new ChestData(world.provider.dimensionId, id, null), key);
    }

    public static ChestData getInstance(WorldServer world, UUID id) {
        ensureChestDataDir(world);
        knownChestIds.add(id);
        String key = ChestData.ID(id);
        return computeIfAbsentManager(getWorldServer().mapStorage, new ChestData(id), key);
    }

    public static ChestData getInstanceInventory(WorldServer world, UUID id, ItemStack[] base) {
        ensureChestDataDir(world);
        knownChestIds.add(id);
        String key = ChestData.ID(id);
        return computeIfAbsentManager(getWorldServer().mapStorage, new ChestData(world.provider.dimensionId, id, base), key);
    }

    @Nullable
    public static SpecialChestInventory getInventory(World world, UUID uuid, int x, int y, int z, EntityPlayerMP player, @Nullable TileEntityChest tile, LootFiller filler) {
        if (world.isRemote || !(world instanceof WorldServer)) return null;
        ChestData data = getInstanceUuid((WorldServer) world, uuid, x, y, z);
        SpecialChestInventory inventory = data.getInventory(player);
        if (inventory == null) {
            inventory = data.createInventory(player, filler, tile);
        }
        if (inventory != null) {
            inventory.setBlockPos(x, y, z);
            if (tile instanceof noobanidus.mods.lootr.block.tile.LootrChestTileEntity)
                inventory.setTileRef((noobanidus.mods.lootr.block.tile.LootrChestTileEntity) tile);
        }
        return inventory;
    }

    @Nullable
    public static SpecialChestInventory getInventory(World world, UUID uuid, ItemStack[] base, EntityPlayerMP player, int x, int y, int z, TileEntityChest tile) {
        if (world.isRemote || !(world instanceof WorldServer)) return null;
        ChestData data = getInstanceInventory((WorldServer) world, uuid, base);
        SpecialChestInventory inventory = data.getInventory(player);
        if (inventory == null) {
            inventory = data.createInventory(player, data.customInventory(), tile);
        }
        if (inventory != null) {
            inventory.setBlockPos(x, y, z);
            if (tile instanceof noobanidus.mods.lootr.block.tile.LootrChestTileEntity)
                inventory.setTileRef((noobanidus.mods.lootr.block.tile.LootrChestTileEntity) tile);
        }
        return inventory;
    }

    @Nullable
    public static SpecialChestInventory getInventory(World world, LootrChestMinecartEntity cart, EntityPlayerMP player, LootFiller filler, int x, int y, int z) {
        if (world.isRemote || !(world instanceof WorldServer)) return null;
        ChestData data = getInstance((WorldServer) world, cart.getUniqueID());
        SpecialChestInventory inventory = data.getInventory(player);
        if (inventory == null) {
            inventory = data.createInventory(player, filler, null);
        }
        if (inventory != null) inventory.setBlockPos(x, y, z);
        return inventory;
    }

    public static void refreshInventory(World world, UUID uuid, EntityPlayerMP player, int x, int y, int z) {
        if (world.isRemote || !(world instanceof WorldServer)) return;
        ChestData data = getInstanceUuid((WorldServer) world, uuid, x, y, z);
        data.clear();
        data.markDirty();
    }

    public static void refreshInventory(World world, LootrChestMinecartEntity cart, EntityPlayerMP player, int x, int y, int z) {
        if (world.isRemote || !(world instanceof WorldServer)) return;
        ChestData data = getInstance((WorldServer) world, cart.getUniqueID());
        data.clear();
        data.markDirty();
    }

    public static boolean clearInventories(UUID uuid) {
        WorldServer world = getWorldServer();
        MapStorage storage = world.mapStorage;

        java.util.Set<String> keys = new java.util.LinkedHashSet<>();
        for (UUID id : knownChestIds) {
            keys.add(ChestData.ID(id));
        }

        storage.saveAllData();

        File dataPath = new File(world.getSaveHandler().getWorldDirectory(), "data/lootr");
        Lootr.LOG.info("[Lootr] Scanning: " + dataPath.getAbsolutePath() + " exists=" + dataPath.exists());
        if (dataPath.exists()) {
            File[] files = dataPath.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && f.getName().endsWith(".dat") && !f.getName().startsWith("Lootr-")) {
                        keys.add("lootr/" + f.getName().replace(".dat", ""));
                    }
                }
            }
        }

        Lootr.LOG.info("[Lootr] Found " + keys.size() + " chest data entries");

        int cleared = 0;
        for (String key : keys) {
            ChestData chestData = (ChestData) storage.loadData(ChestData.class, key);
            if (chestData != null && chestData.clearInventory(uuid)) {
                cleared++;
                chestData.markDirty();
            }
        }
        storage.saveAllData();
        Lootr.LOG.info("[Lootr] Cleared " + cleared + " inventories for player UUID " + uuid);
        return cleared != 0;
    }
}
