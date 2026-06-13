package noobanidus.mods.lootr.entity;

import net.minecraft.block.BlockChest;
import net.minecraft.entity.item.EntityMinecartContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.FakePlayer;
import noobanidus.mods.lootr.api.entity.ILootCart;
import noobanidus.mods.lootr.config.ConfigManager;
import noobanidus.mods.lootr.networking.PacketHandler;
import noobanidus.mods.lootr.util.ChestUtil;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LootrChestMinecartEntity extends EntityMinecartContainer implements ILootCart {
    private final Set<UUID> openers = new HashSet<>();
    private boolean opened = false;

    public LootrChestMinecartEntity(World worldIn) {
        super(worldIn);
    }

    public LootrChestMinecartEntity(World worldIn, double x, double y, double z) {
        super(worldIn);
        setPosition(x, y, z);
    }

    @Override
    public Set<UUID> getOpeners() {
        return openers;
    }

    public void addOpener(EntityPlayer player) {
        openers.add(player.getUniqueID());
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened() {
        this.opened = true;
    }

    public void setClosed() {
        this.opened = false;
    }

    @Override
    public boolean isEntityInvulnerable() {
        // handled in interactFirst
        return false;
    }
    @Override
    public void killMinecart(DamageSource source) {
        this.setDead();
        if (this.worldObj.getGameRules().getGameRuleBooleanValue("doEntityDrops")) {
            this.entityDropItem(new ItemStack(net.minecraft.init.Items.minecart), 0);
            this.entityDropItem(new ItemStack(net.minecraft.init.Blocks.chest), 0);
        }
    }

    @Override
    public int getSizeInventory() {
        return 27;
    }

    @Override
    public int getMinecartType() {
        return 1; // chest
    }

    @Override
    public String getInventoryName() {
        return "Lootr Minecart";
    }

    @Override
    public BlockChest func_145817_o() {
        return (BlockChest) net.minecraft.init.Blocks.chest;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
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
    protected void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        if (compound.hasKey("LootrOpeners", Constants.NBT.TAG_LIST)) {
            NBTTagList openers = compound.getTagList("LootrOpeners", Constants.NBT.TAG_COMPOUND);
            this.openers.clear();
            for (int i = 0; i < openers.tagCount(); i++) {
                NBTTagCompound item = openers.getCompoundTagAt(i);
                this.openers.add(new UUID(item.getLong("M"), item.getLong("L")));
            }
        }
    }

    @Override
    public boolean interactFirst(EntityPlayer player) {
        if (player.isSneaking()) {
            ChestUtil.handleLootCartSneak(player.worldObj, this, player);
            return true;
        } else {
            ChestUtil.handleLootCart(player.worldObj, this, player);
            return true;
        }
    }

    public void addLoot(@Nullable EntityPlayer player, IInventory inventory) {
        // In 1.7.10, chest minecarts don't have loot tables - they get their items from world gen.
        // So we just copy the vanilla chest's contents.
        for (int i = 0; i < this.getSizeInventory() && i < inventory.getSizeInventory(); i++) {
            ItemStack stack = this.getStackInSlot(i);
            if (stack != null) {
                inventory.setInventorySlotContents(i, stack.copy());
            }
        }
    }

    @Override
    public void openInventory() {
        if (!this.worldObj.isRemote) {
            net.minecraft.network.play.server.S3FPacketCustomPayload pkt;
        }
    }

    @Override
    public void closeInventory() {
        // tracked via LootrChestTileEntity.onPlayerClose pattern
    }
}
