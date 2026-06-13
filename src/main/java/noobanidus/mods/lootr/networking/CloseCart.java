package noobanidus.mods.lootr.networking;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class CloseCart implements IMessage {
    public int entityId;

    public CloseCart() {
        this.entityId = -1;
    }

    public CloseCart(int entityId) {
        this.entityId = entityId;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityId = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.entityId);
    }
}
