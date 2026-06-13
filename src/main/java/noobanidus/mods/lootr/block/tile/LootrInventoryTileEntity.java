package noobanidus.mods.lootr.block.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class LootrInventoryTileEntity extends LootrChestTileEntity {
    private ItemStack[] customInventory;

    public LootrInventoryTileEntity() {
        super();
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("customInventory") && compound.hasKey("customSize")) {
            int size = compound.getInteger("customSize");
            this.customInventory = new ItemStack[size];
            NBTTagList items = compound.getTagList("customInventory", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < items.tagCount(); i++) {
                NBTTagCompound slot = items.getCompoundTagAt(i);
                byte slotIndex = slot.getByte("Slot");
                if (slotIndex >= 0 && slotIndex < this.customInventory.length) {
                    this.customInventory[slotIndex] = ItemStack.loadItemStackFromNBT(slot);
                }
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (this.customInventory != null) {
            compound.setInteger("customSize", this.customInventory.length);
            NBTTagList items = new NBTTagList();
            for (int i = 0; i < this.customInventory.length; i++) {
                if (this.customInventory[i] != null) {
                    NBTTagCompound slot = new NBTTagCompound();
                    slot.setByte("Slot", (byte) i);
                    this.customInventory[i].writeToNBT(slot);
                    items.appendTag(slot);
                }
            }
            compound.setTag("customInventory", items);
        }
    }

    @Nullable
    public ItemStack[] getCustomInventory() {
        return customInventory;
    }

    public void setCustomInventory(ItemStack[] customInventory) {
        this.customInventory = customInventory;
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound tag = new NBTTagCompound();
        writeToNBT(tag);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 0, tag);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }
}
