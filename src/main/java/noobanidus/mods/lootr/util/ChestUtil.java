package noobanidus.mods.lootr.util;

import net.minecraft.block.Block;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import noobanidus.mods.lootr.api.tile.ILootTile;
import noobanidus.mods.lootr.block.tile.LootrChestTileEntity;
import noobanidus.mods.lootr.block.tile.LootrInventoryTileEntity;
import noobanidus.mods.lootr.config.ConfigManager;
import noobanidus.mods.lootr.data.DataStorage;
import noobanidus.mods.lootr.data.SpecialChestInventory;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;
import noobanidus.mods.lootr.networking.CloseCart;
import noobanidus.mods.lootr.networking.PacketHandler;

import java.util.Set;
import java.util.UUID;

public class ChestUtil {

    private static void sendMessage(EntityPlayer player, String key, EnumChatFormatting color) {
        ChatComponentTranslation msg = new ChatComponentTranslation(key);
        msg.getChatStyle().setColor(color);
        ((ICommandSender) player).addChatMessage(msg);
    }

    private static void sendMessage(EntityPlayer player, String key, EnumChatFormatting color, Object... args) {
        ChatComponentTranslation msg = new ChatComponentTranslation(key, args);
        msg.getChatStyle().setColor(color);
        ((ICommandSender) player).addChatMessage(msg);
    }

    private static void handleDecayTimer(EntityPlayer player, UUID tileId, boolean isDecaying) {
        int decayValue = DataStorage.getDecayValue(tileId);
        if (decayValue > 0 && ConfigManager.shouldNotify(decayValue)) {
            sendMessage(player, "lootr.message.decay_in", EnumChatFormatting.RED, decayValue / 20);
        } else if (decayValue == -1 && isDecaying) {
            DataStorage.setDecaying(tileId, ConfigManager.getDecayValue());
            sendMessage(player, "lootr.message.decay_start", EnumChatFormatting.RED, ConfigManager.getDecayValue() / 20);
        }
    }

    private static void handleRefreshTimer(EntityPlayer player, UUID tileId, boolean isRefreshing) {
        int refreshValue = DataStorage.getRefreshValue(tileId);
        if (refreshValue > 0 && ConfigManager.shouldNotify(refreshValue)) {
            sendMessage(player, "lootr.message.refresh_in", EnumChatFormatting.BLUE, refreshValue / 20);
        } else if (refreshValue == -1 && isRefreshing) {
            DataStorage.setRefreshing(tileId, ConfigManager.getRefreshValue());
            sendMessage(player, "lootr.message.refresh_start", EnumChatFormatting.BLUE, ConfigManager.getRefreshValue() / 20);
        }
    }

    public static boolean handleLootSneak(Block block, World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote) return false;
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof ILootTile) {
            Set<UUID> openers = ((ILootTile) te).getOpeners();
            openers.remove(player.getUniqueID());
            ((ILootTile) te).updatePacketViaState();
            return true;
        }
        return false;
    }

    public static void handleLootCartSneak(World world, LootrChestMinecartEntity cart, EntityPlayer player) {
        if (world.isRemote) return;
        cart.getOpeners().remove(player.getUniqueID());
        PacketHandler.sendToAll(cart, new CloseCart(cart.getEntityId()));
    }

    public static boolean handleLootChest(Block block, World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote) return false;
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof ILootTile) || !(te instanceof TileEntityChest)) return false;

        UUID tileId = ((ILootTile) te).getTileId();

        if (DataStorage.isDecayed(tileId)) {
            world.func_147480_a(x, y, z, true);
            DataStorage.removeDecayed(tileId);
            sendMessage(player, "lootr.message.decayed", EnumChatFormatting.RED);
            return false;
        }

        handleDecayTimer(player, tileId, ConfigManager.isDecaying(world, (ILootTile) te));

        if (DataStorage.isRefreshed(tileId)) {
            DataStorage.refreshInventory(world, tileId, (EntityPlayerMP) player, x, y, z);
            DataStorage.removeRefreshed(tileId);
            sendMessage(player, "lootr.message.refreshed", EnumChatFormatting.BLUE);
        }
        handleRefreshTimer(player, tileId, ConfigManager.isRefreshing(world, (ILootTile) te));

        SpecialChestInventory provider = DataStorage.getInventory(world, tileId, x, y, z, (EntityPlayerMP) player, (TileEntityChest) te, ((ILootTile) te)::fillWithLoot);
        if (provider != null) {
            ((EntityPlayerMP) player).displayGUIChest(provider);
            if (te instanceof LootrChestTileEntity) {
                ((LootrChestTileEntity) te).onPlayerClose(player);
            }
        }
        return true;
    }

    public static boolean handleLootInventory(Block block, World world, int x, int y, int z, EntityPlayer player) {
        if (world.isRemote) return false;
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof LootrInventoryTileEntity)) return false;

        UUID tileId = ((ILootTile) te).getTileId();
        LootrInventoryTileEntity tile = (LootrInventoryTileEntity) te;
        ItemStack[] stacks = tile.getCustomInventory();

        if (DataStorage.isDecayed(tileId)) {
            world.func_147480_a(x, y, z, true);
            DataStorage.removeDecayed(tileId);
            sendMessage(player, "lootr.message.decayed", EnumChatFormatting.RED);
            return false;
        }

        handleDecayTimer(player, tileId, ConfigManager.isDecaying(world, (ILootTile) te));

        if (DataStorage.isRefreshed(tileId)) {
            DataStorage.refreshInventory(world, tileId, (EntityPlayerMP) player, x, y, z);
            DataStorage.removeRefreshed(tileId);
            sendMessage(player, "lootr.message.refreshed", EnumChatFormatting.BLUE);
        }
        handleRefreshTimer(player, tileId, ConfigManager.isRefreshing(world, (ILootTile) te));

        SpecialChestInventory provider = DataStorage.getInventory(world, tileId, stacks != null ? stacks.clone() : null, (EntityPlayerMP) player, x, y, z, tile);
        if (provider != null) {
            ((EntityPlayerMP) player).displayGUIChest(provider);
            tile.onPlayerClose(player);
        }
        return true;
    }

    public static void handleLootCart(World world, LootrChestMinecartEntity cart, EntityPlayer player) {
        if (world.isRemote) return;

        UUID tileId = cart.getUniqueID();

        if (DataStorage.isDecayed(tileId)) {
            cart.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
            DataStorage.removeDecayed(tileId);
            sendMessage(player, "lootr.message.decayed", EnumChatFormatting.RED);
            return;
        }

        handleDecayTimer(player, tileId, ConfigManager.isDecaying(world, cart));

        if (!cart.getOpeners().contains(player.getUniqueID())) {
            cart.addOpener(player);
        }

        if (DataStorage.isRefreshed(tileId)) {
            DataStorage.refreshInventory(world, cart, (EntityPlayerMP) player, (int) cart.posX, (int) cart.posY, (int) cart.posZ);
            DataStorage.removeRefreshed(tileId);
            sendMessage(player, "lootr.message.refreshed", EnumChatFormatting.BLUE);
        }
        handleRefreshTimer(player, tileId, ConfigManager.isRefreshing(world, cart));

        SpecialChestInventory provider = DataStorage.getInventory(world, cart, (EntityPlayerMP) player, cart::addLoot, (int) cart.posX, (int) cart.posY, (int) cart.posZ);
        if (provider != null) {
            ((EntityPlayerMP) player).displayGUIChest(provider);
        }
    }

    public static ItemStack[] copyItemList(ItemStack[] reference) {
        ItemStack[] contents = new ItemStack[reference.length];
        for (int i = 0; i < reference.length; i++) {
            if (reference[i] != null) contents[i] = reference[i].copy();
        }
        return contents;
    }
}
