package noobanidus.mods.lootr.block.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import noobanidus.mods.lootr.Lootr;
import noobanidus.mods.lootr.api.tile.ILootTile;
import noobanidus.mods.lootr.data.SpecialChestInventory;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@SuppressWarnings({"Duplicates", "ConstantConditions", "NullableProblems"})
public class LootrChestTileEntity extends TileEntityChest implements ILootTile {
    public Set<UUID> openers = new HashSet<>();
    private UUID tileId;
    private boolean opened;
    private int specialNumPlayersUsingChest;
    private int ticksSinceSync;

    public LootrChestTileEntity() {
        super();
    }

    @Override
    public UUID getTileId() {
        if (this.tileId == null) {
            this.tileId = UUID.randomUUID();
        }
        return this.tileId;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    @Override
    public void fillWithLoot(EntityPlayer player, IInventory inventory) {
        TileEntityChest vanillaChest = this;
        for (int i = 0; i < vanillaChest.getSizeInventory(); i++) {
            ItemStack stack = vanillaChest.getStackInSlot(i);
            if (stack != null) {
                inventory.setInventorySlotContents(i, stack.copy());
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("tileIdMost")) {
            this.tileId = new UUID(compound.getLong("tileIdMost"), compound.getLong("tileIdLeast"));
        }
        if (compound.hasKey("LootrOpeners")) {
            NBTTagList openers = compound.getTagList("LootrOpeners", Constants.NBT.TAG_COMPOUND);
            this.openers.clear();
            for (int i = 0; i < openers.tagCount(); i++) {
                NBTTagCompound tag = openers.getCompoundTagAt(i);
                this.openers.add(new UUID(tag.getLong("M"), tag.getLong("L")));
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        if (this.tileId != null) {
            compound.setLong("tileIdMost", this.tileId.getMostSignificantBits());
            compound.setLong("tileIdLeast", this.tileId.getLeastSignificantBits());
        }
        NBTTagList list = new NBTTagList();
        for (UUID opener : this.openers) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setLong("M", opener.getMostSignificantBits());
            tag.setLong("L", opener.getLeastSignificantBits());
            list.appendTag(tag);
        }
        compound.setTag("LootrOpeners", list);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();
        ++this.ticksSinceSync;
        this.specialNumPlayersUsingChest = calculatePlayersUsingSync(this.worldObj, this, this.ticksSinceSync, this.xCoord, this.yCoord, this.zCoord, this.specialNumPlayersUsingChest);

        this.prevLidAngle = this.lidAngle;
        if (this.specialNumPlayersUsingChest > 0 && this.lidAngle == 0.0F) {
            this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, "random.chestopen", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
        }

        if (this.specialNumPlayersUsingChest == 0 && this.lidAngle > 0.0F || this.specialNumPlayersUsingChest > 0 && this.lidAngle < 1.0F) {
            float f1 = this.lidAngle;
            if (this.specialNumPlayersUsingChest > 0) {
                this.lidAngle += 0.1F;
            } else {
                this.lidAngle -= 0.1F;
            }

            if (this.lidAngle > 1.0F) {
                this.lidAngle = 1.0F;
            }

            if (this.lidAngle < 0.5F && f1 >= 0.5F) {
                this.worldObj.playSoundEffect(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, "random.chestclosed", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
            }

            if (this.lidAngle < 0.0F) {
                this.lidAngle = 0.0F;
            }
        }
    }

    @Override
    public Set<UUID> getOpeners() {
        return openers;
    }

    @Override
    public void openInventory() {
        if (this.specialNumPlayersUsingChest < 0) {
            this.specialNumPlayersUsingChest = 0;
        }
        ++this.specialNumPlayersUsingChest;
        signalOpenCount();
    }

    @Override
    public void closeInventory() {
        --this.specialNumPlayersUsingChest;
        signalOpenCount();
    }

    public void onPlayerClose(EntityPlayer player) {
        openers.add(player.getUniqueID());
        this.markDirty();
        updatePacketViaState();
    }

    protected void signalOpenCount() {
        this.worldObj.addBlockEvent(this.xCoord, this.yCoord, this.zCoord, this.getBlockType(), 1, this.specialNumPlayersUsingChest);
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
    }

    @Override
    public boolean receiveClientEvent(int id, int type) {
        if (id == 1) {
            this.specialNumPlayersUsingChest = type;
            return true;
        }
        return super.receiveClientEvent(id, type);
    }

    @Override
    public void updatePacketViaState() {
        if (worldObj != null && !worldObj.isRemote) {
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }
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

    @Override
    public void checkForAdjacentChests() {
        this.adjacentChestChecked = true;
        this.adjacentChestXNeg = null;
        this.adjacentChestXPos = null;
        this.adjacentChestZNeg = null;
        this.adjacentChestZPos = null;
    }

    public static int getPlayersUsing(World world, int x, int y, int z) {
        TileEntityChest te = (TileEntityChest) world.getTileEntity(x, y, z);
        if (te instanceof LootrChestTileEntity) {
            return ((LootrChestTileEntity) te).specialNumPlayersUsingChest;
        }
        return 0;
    }

    public static int calculatePlayersUsingSync(World world, TileEntityChest tile, int ticksSinceSync, int x, int y, int z, int numPlayersUsing) {
        if (!world.isRemote && numPlayersUsing != 0 && (ticksSinceSync + x + y + z) % 200 == 0) {
            numPlayersUsing = calculatePlayersUsing(world, tile, x, y, z);
        }
        return MathHelper.clamp_int(numPlayersUsing, 0, 15);
    }

    public static int calculatePlayersUsing(World world, TileEntityChest tile, int x, int y, int z) {
        if (tile == null) return 0;
        int i = 0;

        for (EntityPlayer player : (java.util.List<EntityPlayer>) world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(x - 5.0, y - 5.0, z - 5.0, (x + 1) + 5.0, (y + 1) + 5.0, (z + 1) + 5.0))) {
            if (player.openContainer instanceof ContainerChest) {
                IInventory inv = ((ContainerChest) player.openContainer).getLowerChestInventory();
                if (inv == null) continue;
                if (inv == tile || (inv instanceof SpecialChestInventory && x == ((SpecialChestInventory) inv).getPosX() && y == ((SpecialChestInventory) inv).getPosY() && z == ((SpecialChestInventory) inv).getPosZ())) {
                    ++i;
                }
            }
        }
        return i;
    }
}
