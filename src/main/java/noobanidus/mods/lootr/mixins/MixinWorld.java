package noobanidus.mods.lootr.mixins;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.world.chunk.Chunk;
import noobanidus.mods.lootr.api.tile.ILootTile;
import noobanidus.mods.lootr.block.tile.TileTicker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public class MixinWorld {
    @Inject(method = "addTileEntity", at = @At("RETURN"))
    protected void lootrAddBlockEntity(TileEntity tile, CallbackInfo ci) {
        if (!(tile instanceof TileEntityChest) || tile instanceof ILootTile) return;
        TileTicker.addEntry(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
    }
}
