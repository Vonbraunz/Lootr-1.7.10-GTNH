package noobanidus.mods.lootr.config;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.config.Configuration;
import noobanidus.mods.lootr.Lootr;
import noobanidus.mods.lootr.api.tile.ILootTile;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;
import noobanidus.mods.lootr.init.ModBlocks;

import java.io.File;
import java.util.*;

public class ConfigManager {
    public static Configuration config;

    public static boolean RANDOMISE_SEED = true;
    public static boolean CONVERT_MINESHAFTS = true;
    public static boolean DISABLE_BREAK = false;
    public static boolean ENABLE_FAKE_PLAYER_BREAK = false;
    public static int DECAY_VALUE = 5 * 60 * 20;
    public static int REFRESH_VALUE = 20 * 60 * 20;
    public static boolean DECAY_ALL = false;
    public static boolean REFRESH_ALL = false;
    public static String[] ADDITIONAL_CHESTS = new String[0];
    public static String[] ADDITIONAL_TRAPPED_CHESTS = new String[0];
    public static int[] DIMENSION_WHITELIST = new int[0];
    public static int[] DIMENSION_BLACKLIST = new int[0];
    public static String[] LOOT_TABLE_BLACKLIST = new String[0];
    public static String[] LOOT_MODID_BLACKLIST = new String[0];
    public static String[] DECAY_MODIDS = new String[0];
    public static String[] DECAY_LOOT_TABLES = new String[0];
    public static int[] DECAY_DIMENSIONS = new int[0];
    public static String[] REFRESH_MODIDS = new String[0];
    public static String[] REFRESH_LOOT_TABLES = new String[0];
    public static int[] REFRESH_DIMENSIONS = new int[0];
    public static boolean VANILLA_TEXTURES = false;
    public static boolean CONVERT_WORLDGEN_INVENTORIES = false;
    public static boolean BLAST_RESISTANT = false;
    public static boolean BLAST_IMMUNE = false;
    public static boolean ZERO_COMPARATOR = false;
    public static boolean DISABLE_NOTIFICATIONS = false;
    public static int NOTIFICATION_DELAY = 30 * 20;

    private static Set<Integer> DIM_WHITELIST = null;
    private static Set<Integer> DIM_BLACKLIST = null;
    private static Set<Integer> DECAY_DIMS = null;
    private static Set<Integer> REFRESH_DIMS = null;
    private static Set<String> DECAY_MODS = null;
    private static Set<String> REFRESH_MODS = null;
    private static Map<Block, Block> replacements = null;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        syncConfig();
    }

    @SubscribeEvent
    public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(Lootr.MODID)) {
            syncConfig();
        }
    }

    public static void syncConfig() {
        RANDOMISE_SEED = config.getBoolean("randomise_seed", Configuration.CATEGORY_GENERAL, true, "Randomise loot per player instead of using the provided seed");
        CONVERT_MINESHAFTS = config.getBoolean("convert_mineshafts", Configuration.CATEGORY_GENERAL, true, "Convert mineshaft chest minecarts to loot chests");
        DISABLE_BREAK = config.getBoolean("disable_break", Configuration.CATEGORY_GENERAL, false, "Prevent destruction of Lootr chests except while sneaking in creative");
        ENABLE_FAKE_PLAYER_BREAK = config.getBoolean("enable_fake_player_break", Configuration.CATEGORY_GENERAL, false, "Allow fake players to destroy Lootr chests");
        DECAY_VALUE = config.getInt("decay_value", Configuration.CATEGORY_GENERAL, 5 * 60 * 20, 0, Integer.MAX_VALUE, "Ticks before a decaying container decays");
        REFRESH_VALUE = config.getInt("refresh_value", Configuration.CATEGORY_GENERAL, 20 * 60 * 20, 0, Integer.MAX_VALUE, "Ticks before a container refreshes");
        DECAY_ALL = config.getBoolean("decay_all", Configuration.CATEGORY_GENERAL, false, "All chests will decay");
        REFRESH_ALL = config.getBoolean("refresh_all", Configuration.CATEGORY_GENERAL, false, "All chests will refresh");
        ADDITIONAL_CHESTS = config.getStringList("additional_chests", Configuration.CATEGORY_GENERAL, new String[0], "Additional chests to convert (modid:name)");
        ADDITIONAL_TRAPPED_CHESTS = config.getStringList("additional_trapped_chests", Configuration.CATEGORY_GENERAL, new String[0], "Additional trapped chests to convert");
        DIMENSION_WHITELIST = config.get("general", "dimension_whitelist", new int[0]).getIntList();
        DIMENSION_BLACKLIST = config.get("general", "dimension_blacklist", new int[0]).getIntList();
        LOOT_TABLE_BLACKLIST = config.getStringList("loot_table_blacklist", Configuration.CATEGORY_GENERAL, new String[0], "Loot tables to not convert");
        LOOT_MODID_BLACKLIST = config.getStringList("loot_modid_blacklist", Configuration.CATEGORY_GENERAL, new String[0], "Mod IDs to not convert");
        DECAY_MODIDS = config.getStringList("decay_modids", Configuration.CATEGORY_GENERAL, new String[0], "Mod IDs whose loot tables decay");
        DECAY_LOOT_TABLES = config.getStringList("decay_loot_tables", Configuration.CATEGORY_GENERAL, new String[0], "Loot tables which decay");
        DECAY_DIMENSIONS = config.get("general", "decay_dimensions", new int[0]).getIntList();
        REFRESH_MODIDS = config.getStringList("refresh_modids", Configuration.CATEGORY_GENERAL, new String[0], "Mod IDs whose loot tables refresh");
        REFRESH_LOOT_TABLES = config.getStringList("refresh_loot_tables", Configuration.CATEGORY_GENERAL, new String[0], "Loot tables which refresh");
        REFRESH_DIMENSIONS = config.get("general", "refresh_dimensions", new int[0]).getIntList();
        VANILLA_TEXTURES = config.getBoolean("vanilla_textures", Configuration.CATEGORY_GENERAL, false, "Use vanilla chest textures");
        CONVERT_WORLDGEN_INVENTORIES = config.getBoolean("convert_worldgen_inventories", Configuration.CATEGORY_GENERAL, false, "Convert chests without loot tables but with items");
        BLAST_RESISTANT = config.getBoolean("blast_resistant", Configuration.CATEGORY_GENERAL, false, "Lootr chests resist explosions");
        BLAST_IMMUNE = config.getBoolean("blast_immune", Configuration.CATEGORY_GENERAL, false, "Lootr chests immune to explosions");
        ZERO_COMPARATOR = config.getBoolean("zero_comparator", Configuration.CATEGORY_GENERAL, false, "Output 0 from comparator");
        DISABLE_NOTIFICATIONS = config.getBoolean("disable_notifications", Configuration.CATEGORY_GENERAL, false, "Disable decay/refresh notifications");
        NOTIFICATION_DELAY = config.getInt("notification_delay", Configuration.CATEGORY_GENERAL, 30 * 20, -1, Integer.MAX_VALUE, "Max ticks remaining before notification");

        if (config.hasChanged()) {
            config.save();
        }

        replacements = null;
        DIM_WHITELIST = null;
        DIM_BLACKLIST = null;
        DECAY_DIMS = null;
        REFRESH_DIMS = null;
        DECAY_MODS = null;
        REFRESH_MODS = null;
    }

    public static Set<Integer> getDimensionWhitelist() {
        if (DIM_WHITELIST == null) {
            DIM_WHITELIST = new HashSet<>();
            for (int i : DIMENSION_WHITELIST) DIM_WHITELIST.add(i);
        }
        return DIM_WHITELIST;
    }

    public static Set<Integer> getDimensionBlacklist() {
        if (DIM_BLACKLIST == null) {
            DIM_BLACKLIST = new HashSet<>();
            for (int i : DIMENSION_BLACKLIST) DIM_BLACKLIST.add(i);
        }
        return DIM_BLACKLIST;
    }

    public static Set<Integer> getDecayDimensions() {
        if (DECAY_DIMS == null) {
            DECAY_DIMS = new HashSet<>();
            for (int i : DECAY_DIMENSIONS) DECAY_DIMS.add(i);
        }
        return DECAY_DIMS;
    }

    public static Set<Integer> getRefreshDimensions() {
        if (REFRESH_DIMS == null) {
            REFRESH_DIMS = new HashSet<>();
            for (int i : REFRESH_DIMENSIONS) REFRESH_DIMS.add(i);
        }
        return REFRESH_DIMS;
    }

    public static Set<String> getDecayMods() {
        if (DECAY_MODS == null) {
            DECAY_MODS = new HashSet<>(Arrays.asList(DECAY_MODIDS));
        }
        return DECAY_MODS;
    }

    public static Set<String> getRefreshMods() {
        if (REFRESH_MODS == null) {
            REFRESH_MODS = new HashSet<>(Arrays.asList(REFRESH_MODIDS));
        }
        return REFRESH_MODS;
    }

    public static boolean isDimensionBlocked(int key) {
        return (!getDimensionWhitelist().isEmpty() && !getDimensionWhitelist().contains(key))
                || getDimensionBlacklist().contains(key);
    }

    public static boolean isDimensionDecaying(int key) {
        return getDecayDimensions().contains(key);
    }

    public static boolean isDimensionRefreshing(int key) {
        return getRefreshDimensions().contains(key);
    }

    public static boolean isDecaying(World world, ILootTile tile) {
        if (DECAY_ALL) return true;
        return isDimensionDecaying(world.provider.dimensionId);
    }

    public static boolean isRefreshing(World world, ILootTile tile) {
        if (REFRESH_ALL) return true;
        return isDimensionRefreshing(world.provider.dimensionId);
    }

    public static boolean isDecaying(World world, LootrChestMinecartEntity entity) {
        if (DECAY_ALL) return true;
        return isDimensionDecaying(world.provider.dimensionId);
    }

    public static boolean isRefreshing(World world, LootrChestMinecartEntity entity) {
        if (REFRESH_ALL) return true;
        return isDimensionRefreshing(world.provider.dimensionId);
    }

    public static int getDecayValue() {
        return DECAY_VALUE;
    }

    public static int getRefreshValue() {
        return REFRESH_VALUE;
    }

    public static boolean shouldNotify(int remaining) {
        return !DISABLE_NOTIFICATIONS && (NOTIFICATION_DELAY == -1 || remaining <= NOTIFICATION_DELAY);
    }

    public static boolean isVanillaTextures() {
        return VANILLA_TEXTURES;
    }

    private static void addReplacement(Block original, Block replacement) {
        if (replacements == null) replacements = new HashMap<>();
        if (original != null) replacements.put(original, replacement);
    }

    public static Block replacement(Block original) {
        if (replacements == null) {
            replacements = new HashMap<>();
            replacements.put(Blocks.chest, ModBlocks.CHEST);
            replacements.put(Blocks.trapped_chest, ModBlocks.TRAPPED_CHEST);
        }
        return replacements.get(original);
    }

    public static boolean isBlacklisted(String name) {
        for (String s : LOOT_TABLE_BLACKLIST) if (s.equals(name)) return true;
        for (String s : LOOT_MODID_BLACKLIST) if (s.equalsIgnoreCase(name)) return true;
        return false;
    }
}
