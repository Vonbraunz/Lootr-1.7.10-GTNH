package noobanidus.mods.lootr.command;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.*;
import net.minecraft.world.World;
import noobanidus.mods.lootr.api.tile.ILootTile;
import noobanidus.mods.lootr.block.tile.LootrInventoryTileEntity;
import noobanidus.mods.lootr.data.DataStorage;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;
import noobanidus.mods.lootr.init.ModBlocks;

import javax.annotation.Nullable;
import java.util.*;

public class LootrCommand implements net.minecraft.command.ICommand {

    @Override
    public String getCommandName() {
        return "lootr";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "lootr.commands.usage";
    }

    @Override
    public List<String> getCommandAliases() {
        return ImmutableList.of();
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) throw new WrongUsageException("lootr.commands.usage");
        String command = args[0];

        if (command.equals("clear") && args.length >= 2) {
            clearPlayerProfile(sender, args[1]);
        } else if (command.equals("chest") || command.equals("cart")) {
            createBlock(sender, command.equals("chest") ? ModBlocks.CHEST : null);
        } else if (command.equals("openers") && args.length >= 4) {
            listOpeners(sender, args);
        } else if (command.equals("custom")) {
            convertToCustom(sender);
        } else {
            throw new WrongUsageException("lootr.commands.usage");
        }
    }

    private void clearPlayerProfile(ICommandSender sender, String profileName) {
        GameProfile profile = cpw.mods.fml.common.FMLCommonHandler.instance().getMinecraftServerInstance().func_152358_ax().func_152655_a(profileName);
        if (profile == null) {
            sender.addChatMessage(new ChatComponentText("Invalid player name: " + profileName));
            return;
        }
        boolean cleared = DataStorage.clearInventories(profile.getId());
        sender.addChatMessage(new ChatComponentText(cleared
                ? "Cleared stored inventories for " + profileName
                : "No stored inventories for " + profileName + " to clear"));
    }

    private void listOpeners(ICommandSender sender, String[] args) {
        int x = parseIntBounded(sender, args[1], -30000000, 30000000);
        int y = parseIntBounded(sender, args[2], 0, 256);
        int z = parseIntBounded(sender, args[3], -30000000, 30000000);
        TileEntity tile = sender.getEntityWorld().getTileEntity(x, y, z);
        if (tile instanceof ILootTile) {
            Set<UUID> openers = ((ILootTile) tile).getOpeners();
            sender.addChatMessage(new ChatComponentText("Tile at " + x + "," + y + "," + z + " has " + openers.size() + " openers:"));
            for (UUID uuid : openers) {
                sender.addChatMessage(new ChatComponentText("UUID: " + uuid));
            }
        } else {
            sender.addChatMessage(new ChatComponentText("No Lootr tile at " + x + "," + y + "," + z));
        }
    }

    private void convertToCustom(ICommandSender sender) {
        EntityPlayer player = (EntityPlayer) sender;
        int x = MathHelper.floor_double(player.posX);
        int y = MathHelper.floor_double(player.posY);
        int z = MathHelper.floor_double(player.posZ);
        World world = player.worldObj;
        Block block = world.getBlock(x, y, z);
        if (block != Blocks.chest) {
            y--; block = world.getBlock(x, y, z);
        }
        if (block != Blocks.chest) {
            sender.addChatMessage(new ChatComponentText("Please stand on the chest you wish to convert."));
            return;
        }
        TileEntityChest te = (TileEntityChest) world.getTileEntity(x, y, z);
        ItemStack[] reference = new ItemStack[te.getSizeInventory()];
        for (int i = 0; i < te.getSizeInventory(); i++) {
            ItemStack s = te.getStackInSlot(i);
            if (s != null) reference[i] = s.copy();
        }
        int meta = world.getBlockMetadata(x, y, z);
        world.removeTileEntity(x, y, z);
        world.setBlock(x, y, z, ModBlocks.INVENTORY, meta, 2);
        TileEntity newTe = world.getTileEntity(x, y, z);
        if (!(newTe instanceof LootrInventoryTileEntity)) {
            sender.addChatMessage(new ChatComponentText("Unable to convert chest."));
        } else {
            LootrInventoryTileEntity inv = (LootrInventoryTileEntity) newTe;
            inv.setCustomInventory(reference);
            inv.markDirty();
            sender.addChatMessage(new ChatComponentText("Chest converted to Lootr inventory."));
        }
    }

    private void createBlock(ICommandSender sender, @Nullable Block block) {
        World world = sender.getEntityWorld();
        int x = MathHelper.floor_double(((Entity) sender).posX);
        int y = MathHelper.floor_double(((Entity) sender).posY);
        int z = MathHelper.floor_double(((Entity) sender).posZ);
        if (block == null) {
            LootrChestMinecartEntity cart = new LootrChestMinecartEntity(world, x + 0.5, y + 0.5, z + 0.5);
            world.spawnEntityInWorld(cart);
            sender.addChatMessage(new ChatComponentText("Lootr minecart spawned at " + x + "," + y + "," + z));
        } else {
            EntityPlayer player = (EntityPlayer) sender;
            int facing = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
            world.setBlock(x, y, z, block, facing, 2);
            sender.addChatMessage(new ChatComponentText("Lootr block placed at " + x + "," + y + "," + z));
        }
    }

    private static int parseIntBounded(ICommandSender sender, String arg, int min, int max) {
        int i = parseInt(sender, arg);
        return MathHelper.clamp_int(i, min, max);
    }

    private static int parseInt(ICommandSender sender, String arg) {
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new WrongUsageException("lootr.commands.usage");
        }
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return sender.canCommandSenderUseCommand(2, getCommandName());
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("chest", "cart", "clear", "openers", "custom");
        }
        return ImmutableList.of();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int i) {
        return args.length >= 1 && args[0].equals("clear") && i == 1;
    }

    @Override
    public int compareTo(Object obj) {
        return 0;
    }
}
