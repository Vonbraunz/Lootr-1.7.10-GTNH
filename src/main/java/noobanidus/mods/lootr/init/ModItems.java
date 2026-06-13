package noobanidus.mods.lootr.init;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.Item;
import noobanidus.mods.lootr.Lootr;

public class ModItems {
    public static ItemBlock CHEST;
    public static ItemBlock TRAPPED_CHEST;
    public static ItemBlock TROPHY;

    public static void init() {
        CHEST = (ItemBlock) Item.getItemFromBlock(ModBlocks.CHEST);
        TRAPPED_CHEST = (ItemBlock) Item.getItemFromBlock(ModBlocks.TRAPPED_CHEST);
        TROPHY = (ItemBlock) Item.getItemFromBlock(ModBlocks.TROPHY);
    }
}
