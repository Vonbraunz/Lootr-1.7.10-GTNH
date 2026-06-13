package noobanidus.mods.lootr.init;

import cpw.mods.fml.common.registry.GameRegistry;
import noobanidus.mods.lootr.Lootr;
import noobanidus.mods.lootr.block.tile.LootrChestTileEntity;
import noobanidus.mods.lootr.block.tile.LootrInventoryTileEntity;
import noobanidus.mods.lootr.block.tile.TrappedLootrChestTileEntity;

public class ModTiles {
    public static void init() {
        GameRegistry.registerTileEntity(LootrChestTileEntity.class, Lootr.MODID + ":special_loot_chest");
        GameRegistry.registerTileEntity(LootrInventoryTileEntity.class, Lootr.MODID + ":special_loot_inventory");
        GameRegistry.registerTileEntity(TrappedLootrChestTileEntity.class, Lootr.MODID + ":special_trapped_loot_chest");
    }
}
