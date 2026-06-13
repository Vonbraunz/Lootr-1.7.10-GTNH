package noobanidus.mods.lootr.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.WorldSavedData;
import noobanidus.mods.lootr.api.LootFiller;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChestData extends WorldSavedData {
    private int dimension;
    private UUID entityId;
    private UUID tileId;
    private ItemStack[] reference;
    private Map<UUID, SpecialChestInventory> inventories = new HashMap<>();

    public UUID getEntityId() {
        return entityId;
    }

    public static String ID(UUID id) {
        String idString = id.toString();
        return "lootr/" + idString.charAt(0) + "/" + idString.substring(0, 2) + "/" + idString;
    }

    public ChestData(String ID) {
        super(ID);
    }

    public ChestData(int dimension, UUID id, @Nullable ItemStack[] base) {
        super(ID(id));
        this.dimension = dimension;
        this.tileId = id;
        this.reference = base;
    }

    public ChestData(UUID entityId) {
        super(ID(entityId));
        this.entityId = entityId;
    }

    public Map<UUID, SpecialChestInventory> getInventories() {
        return inventories;
    }

    public boolean clearInventory(UUID uuid) {
        return inventories.remove(uuid) != null;
    }

    @Nullable
    public SpecialChestInventory getInventory(EntityPlayerMP player) {
        return inventories.get(player.getUniqueID());
    }

    public LootFiller customInventory() {
        return (player, inventory) -> {
            if (reference != null) {
                for (int i = 0; i < reference.length && i < inventory.getSizeInventory(); i++) {
                    if (reference[i] != null) {
                        inventory.setInventorySlotContents(i, reference[i].copy());
                    }
                }
            }
        };
    }

    public SpecialChestInventory createInventory(EntityPlayerMP player, LootFiller filler, @Nullable TileEntityChest tile) {
        SpecialChestInventory result;
        IChatComponent name;
        int size = 27;

        if (entityId != null) {
            Entity initial = player.worldObj.func_152378_a(entityId);
            if (!(initial instanceof LootrChestMinecartEntity)) return null;
            LootrChestMinecartEntity cart = (LootrChestMinecartEntity) initial;
            name = new ChatComponentText(cart.getInventoryName());
            result = new SpecialChestInventory(this, new ItemStack[size], name, 0, 0, 0);
            cart.addLoot(player, result);
        } else {
            if (tile == null) return null;
            name = tile.hasCustomInventoryName() ? new ChatComponentText(tile.getInventoryName()) : new ChatComponentText("Chest");
            ItemStack[] items = new ItemStack[tile.getSizeInventory()];
            result = new SpecialChestInventory(this, items, name, tile.xCoord, tile.yCoord, tile.zCoord);
            filler.fillWithLoot(player, result);
        }

        inventories.put(player.getUniqueID(), result);
        this.markDirty();
        return result;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        inventories.clear();
        dimension = compound.getInteger("dimension");
        if (compound.hasKey("entityIdMost")) entityId = new UUID(compound.getLong("entityIdMost"), compound.getLong("entityIdLeast"));
        if (compound.hasKey("tileIdMost")) tileId = new UUID(compound.getLong("tileIdMost"), compound.getLong("tileIdLeast"));
        if (compound.hasKey("reference") && compound.hasKey("referenceSize")) {
            int size = compound.getInteger("referenceSize");
            reference = new ItemStack[size];
            NBTTagList items = compound.getTagList("reference", 10);
            for (int i = 0; i < items.tagCount(); i++) {
                NBTTagCompound slot = items.getCompoundTagAt(i);
                byte idx = slot.getByte("Slot");
                if (idx >= 0 && idx < size) reference[idx] = ItemStack.loadItemStackFromNBT(slot);
            }
        }
        NBTTagList compounds = compound.getTagList("inventories", 10);
        for (int i = 0; i < compounds.tagCount(); i++) {
            NBTTagCompound thisTag = compounds.getCompoundTagAt(i);
            NBTTagCompound items = thisTag.getCompoundTag("chest");
            String name = thisTag.getString("name");
            UUID uuid = new UUID(thisTag.getLong("uuidMost"), thisTag.getLong("uuidLeast"));
            inventories.put(uuid, new SpecialChestInventory(this, items, name, 0, 0, 0));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        compound.setInteger("dimension", dimension);
        if (entityId != null) {
            compound.setLong("entityIdMost", entityId.getMostSignificantBits());
            compound.setLong("entityIdLeast", entityId.getLeastSignificantBits());
        }
        if (tileId != null) {
            compound.setLong("tileIdMost", tileId.getMostSignificantBits());
            compound.setLong("tileIdLeast", tileId.getLeastSignificantBits());
        }
        if (reference != null) {
            compound.setInteger("referenceSize", reference.length);
            NBTTagList items = new NBTTagList();
            for (int i = 0; i < reference.length; i++) {
                if (reference[i] != null) {
                    NBTTagCompound slot = new NBTTagCompound();
                    slot.setByte("Slot", (byte) i);
                    reference[i].writeToNBT(slot);
                    items.appendTag(slot);
                }
            }
            compound.setTag("reference", items);
        }
        NBTTagList compounds = new NBTTagList();
        for (Map.Entry<UUID, SpecialChestInventory> entry : inventories.entrySet()) {
            NBTTagCompound thisTag = new NBTTagCompound();
            thisTag.setLong("uuidMost", entry.getKey().getMostSignificantBits());
            thisTag.setLong("uuidLeast", entry.getKey().getLeastSignificantBits());
            thisTag.setTag("chest", entry.getValue().writeItems());
            thisTag.setString("name", entry.getValue().writeName());
            compounds.appendTag(thisTag);
        }
        compound.setTag("inventories", compounds);
    }

    public void clear() {
        inventories.clear();
    }
}
