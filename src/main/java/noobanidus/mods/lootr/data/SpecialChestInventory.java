package noobanidus.mods.lootr.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import noobanidus.mods.lootr.api.LootFiller;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpecialChestInventory extends net.minecraft.inventory.InventoryBasic {
    private final Object chestData;
    private int posX, posY, posZ;
    private noobanidus.mods.lootr.block.tile.LootrChestTileEntity tileRef;

    public SpecialChestInventory(Object chestData, ItemStack[] contents, IChatComponent name, int x, int y, int z) {
        super(name.getUnformattedText(), true, contents.length);
        this.chestData = chestData;
        for (int i = 0; i < contents.length && i < this.getSizeInventory(); i++) {
            if (contents[i] != null) this.setInventorySlotContents(i, contents[i]);
        }
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    public SpecialChestInventory(Object chestData, NBTTagCompound items, String name, int x, int y, int z) {
        super(name, true, 27);
        this.chestData = chestData;
        NBTTagList list = items.getTagList("Items", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound slot = list.getCompoundTagAt(i);
            byte idx = slot.getByte("Slot");
            if (idx >= 0 && idx < this.getSizeInventory()) {
                this.setInventorySlotContents(idx, ItemStack.loadItemStackFromNBT(slot));
            }
        }
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    public int getPosX() { return posX; }
    public int getPosY() { return posY; }
    public int getPosZ() { return posZ; }

    public void setTileRef(noobanidus.mods.lootr.block.tile.LootrChestTileEntity tile) {
        this.tileRef = tile;
    }

    @Override
    public void openInventory() {
        if (tileRef != null) tileRef.openInventory();
    }

    @Override
    public void closeInventory() {
        if (tileRef != null) tileRef.closeInventory();
    }

    public void setBlockPos(int x, int y, int z) {
        this.posX = x;
        this.posY = y;
        this.posZ = z;
    }

    @Nullable
    public TileEntityChest getTile(World world) {
        if (world == null || world.isRemote) return null;
        net.minecraft.tileentity.TileEntity te = world.getTileEntity(posX, posY, posZ);
        if (te instanceof TileEntityChest && te instanceof noobanidus.mods.lootr.api.tile.ILootTile) return (TileEntityChest) te;
        return null;
    }

    public NBTTagCompound writeItems() {
        NBTTagCompound result = new NBTTagCompound();
        NBTTagList list = new NBTTagList();
        for (int i = 0; i < this.getSizeInventory(); i++) {
            ItemStack stack = this.getStackInSlot(i);
            if (stack != null) {
                NBTTagCompound slot = new NBTTagCompound();
                slot.setByte("Slot", (byte) i);
                stack.writeToNBT(slot);
                list.appendTag(slot);
            }
        }
        result.setTag("Items", list);
        return result;
    }

    public String writeName() {
        return this.getInventoryName();
    }

    @Override
    public void markDirty() {
        if (chestData instanceof ChestData) ((ChestData) chestData).setDirty(true);
    }
}
