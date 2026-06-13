package noobanidus.mods.lootr.init;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.block.Block;
import noobanidus.mods.lootr.Lootr;
import noobanidus.mods.lootr.block.LootrChestBlock;
import noobanidus.mods.lootr.block.TrophyBlock;

import java.util.HashSet;
import java.util.Set;

public class ModBlocks {
    public static LootrChestBlock CHEST;
    public static LootrChestBlock INVENTORY;
    public static LootrChestBlock TRAPPED_CHEST;
    public static Block TROPHY;
    public static final int INVENTORY_OFFSET = 2;
    private static final Set<Block> LOOT_CONTAINERS = new HashSet<>();

    public static void init() {
        CHEST = new LootrChestBlock(0, false);
        TRAPPED_CHEST = new LootrChestBlock(1, false);
        INVENTORY = new LootrChestBlock(2, true);
        TROPHY = new TrophyBlock();

        GameRegistry.registerBlock(CHEST, "lootr_chest");
        GameRegistry.registerBlock(TRAPPED_CHEST, "lootr_trapped_chest");
        GameRegistry.registerBlock(INVENTORY, "lootr_inventory");
        GameRegistry.registerBlock(TROPHY, "trophy");

        LOOT_CONTAINERS.add(CHEST);
        LOOT_CONTAINERS.add(TRAPPED_CHEST);
        LOOT_CONTAINERS.add(INVENTORY);
    }

    public static boolean isLootContainer(Block block) {
        return LOOT_CONTAINERS.contains(block);
    }
}
