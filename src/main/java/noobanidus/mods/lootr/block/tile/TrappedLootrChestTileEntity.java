package noobanidus.mods.lootr.block.tile;

public class TrappedLootrChestTileEntity extends LootrChestTileEntity {
    public TrappedLootrChestTileEntity() {
        super();
    }

    @Override
    protected void signalOpenCount() {
        super.signalOpenCount();
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord - 1, this.zCoord, this.getBlockType());
    }
}
