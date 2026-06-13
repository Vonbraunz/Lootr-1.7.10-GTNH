package noobanidus.mods.lootr.block;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import noobanidus.mods.lootr.Lootr;

public class TrophyBlock extends Block {
    public TrophyBlock() {
        super(Material.rock);
        setBlockName("lootr_trophy");
        setHardness(15f);
    }

    @Override
    public CreativeTabs getCreativeTabToDisplayOn() {
        return Lootr.TAB;
    }
}
