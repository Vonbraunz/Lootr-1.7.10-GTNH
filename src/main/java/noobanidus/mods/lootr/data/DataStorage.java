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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataStorage {
    public static final String DECAY = "lootr/Lootr-DecayData";
    public static final String REFRESH = "lootr/Lootr-RefreshData";

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

    // Decay tracking
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

    // Refresh tracking
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

    // Chest data
    public static ChestData getInstanceUuid(WorldServer world, UUID id, int x, int y, int z) {
        String key = ChestData.ID(id);
        return computeIfAbsentManager(getWorldServer().mapStorage, new ChestData(world.provider.dimensionId, id, null), key);
    }

    public static ChestData getInstance(WorldServer world, UUID id) {
        String key = ChestData.ID(id);
        return computeIfAbsentManager(getWorldServer().mapStorage, new ChestData(id), key);
    }

    public static ChestData getInstanceInventory(WorldServer world, UUID id, ItemStack[] base) {
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
        if (inventory != null) inventory.setBlockPos(x, y, z);
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
        if (inventory != null) inventory.setBlockPos(x, y, z);
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

    @Nullable
    public static void refreshInventory(World world, UUID uuid, EntityPlayerMP player, int x, int y, int z) {
        if (world.isRemote || !(world instanceof WorldServer)) return;
        ChestData data = getInstanceUuid((WorldServer) world, uuid, x, y, z);
        data.clear();
        data.markDirty();
    }

    @Nullable
    public static void refreshInventory(World world, LootrChestMinecartEntity cart, EntityPlayerMP player, int x, int y, int z) {
        if (world.isRemote || !(world instanceof WorldServer)) return;
        ChestData data = getInstance((WorldServer) world, cart.getUniqueID());
        data.clear();
        data.markDirty();
    }

    public static boolean clearInventories(UUID uuid) {
        WorldServer world = getWorldServer();
        MapStorage data = world.mapStorage;
        File dataPath = new File(world.getSaveHandler().getWorldDirectory(), "data/lootr");
        if (!dataPath.exists()) return false;

        List<String> ids = new ArrayList<>();
        for (File f : dataPath.listFiles()) {
            if (f.isFile() && f.getName().endsWith(".dat")) {
                String name = f.getName().replace(".dat", "");
                if (name.startsWith("Lootr-")) continue;
                ids.add("lootr/" + name.charAt(0) + "/" + name.substring(0, 2) + "/" + name);
            }
        }

        int cleared = 0;
        for (String id : ids) {
            ChestData chestData = (ChestData) data.loadData(ChestData.class, id);
            if (chestData != null && chestData.clearInventory(uuid)) {
                cleared++;
                chestData.markDirty();
            }
        }
        Lootr.LOG.info("Cleared " + cleared + " inventories for player UUID " + uuid);
        return cleared != 0;
    }
}
