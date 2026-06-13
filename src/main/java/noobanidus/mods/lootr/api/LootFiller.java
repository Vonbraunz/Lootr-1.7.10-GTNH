package noobanidus.mods.lootr.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;

@FunctionalInterface
public interface LootFiller {
    void fillWithLoot(EntityPlayer player, IInventory inventory);
}
