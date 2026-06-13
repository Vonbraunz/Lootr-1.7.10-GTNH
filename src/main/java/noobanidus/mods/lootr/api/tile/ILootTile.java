package noobanidus.mods.lootr.api.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import java.util.Set;
import java.util.UUID;

public interface ILootTile {
    void fillWithLoot(EntityPlayer player, IInventory inventory);

    Set<UUID> getOpeners();

    UUID getTileId();

    void updatePacketViaState();
}
