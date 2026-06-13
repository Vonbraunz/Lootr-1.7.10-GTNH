package noobanidus.mods.lootr.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import noobanidus.mods.lootr.Lootr;
import noobanidus.mods.lootr.block.tile.LootrChestTileEntity;
import noobanidus.mods.lootr.block.tile.LootrInventoryTileEntity;
import noobanidus.mods.lootr.block.tile.TrappedLootrChestTileEntity;
import noobanidus.mods.lootr.config.ConfigManager;
import noobanidus.mods.lootr.init.ModBlocks;
import noobanidus.mods.lootr.util.ChestUtil;

import java.util.Random;

public class LootrChestBlock extends BlockChest {
    private final boolean isInventory;
    private final boolean isTrapped;

    public LootrChestBlock(int type, boolean isInventory) {
        super(type);
        this.isInventory = isInventory;
        this.isTrapped = type == 1;
        this.setHardness(2.5F);
        this.setStepSound(Block.soundTypeWood);

        if (isInventory) {
            setBlockName("lootr_inventory");
        } else if (isTrapped) {
            setBlockName("lootr_trapped_chest");
        } else {
            setBlockName("lootr_chest");
        }
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            ChestUtil.handleLootSneak(this, world, x, y, z, player);
        } else if (!world.getBlock(x, y + 1, z).isOpaqueCube()) {
            if (isInventory) {
                ChestUtil.handleLootInventory(this, world, x, y, z, player);
            } else {
                ChestUtil.handleLootChest(this, world, x, y, z, player);
            }
        }
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if (isInventory) return new LootrInventoryTileEntity();
        if (isTrapped) return new TrappedLootrChestTileEntity();
        return new LootrChestTileEntity();
    }

    @Override
    public CreativeTabs getCreativeTabToDisplayOn() {
        return Lootr.TAB;
    }

    @Override
    public Item getItemDropped(int meta, Random rand, int fortune) {
        return Item.getItemFromBlock(isTrapped ? Blocks.trapped_chest : Blocks.chest);
    }

    @Override
    public int isProvidingWeakPower(net.minecraft.world.IBlockAccess world, int x, int y, int z, int side) {
        if (!isTrapped) return 0;
        return LootrChestTileEntity.getPlayersUsing((World)world, x, y, z);
    }

    @Override
    public int isProvidingStrongPower(net.minecraft.world.IBlockAccess world, int x, int y, int z, int side) {
        return side == 1 ? isProvidingWeakPower(world, x, y, z, side) : 0;
    }

    @Override
    public boolean canProvidePower() {
        return isTrapped;
    }

    @Override
    public float getExplosionResistance(Entity exploder, World world, int x, int y, int z, double explosionX, double explosionY, double explosionZ) {
        if (ConfigManager.BLAST_IMMUNE) return Float.MAX_VALUE;
        if (ConfigManager.BLAST_RESISTANT) return 16.0f;
        return Blocks.chest.getExplosionResistance(exploder);
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        if (ConfigManager.ZERO_COMPARATOR) return 0;
        return 1;
    }
}
