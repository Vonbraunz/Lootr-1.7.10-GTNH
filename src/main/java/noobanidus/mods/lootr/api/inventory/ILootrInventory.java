package noobanidus.mods.lootr.api.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nullable;

public interface ILootrInventory extends IInventory {
    @Nullable
    TileEntity getTile();

    int getPosX();
    int getPosY();
    int getPosZ();

    ItemStack[] getInventoryContents();
}
