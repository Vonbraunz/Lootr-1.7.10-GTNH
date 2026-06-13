package noobanidus.mods.lootr.client.entity;

import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.util.ResourceLocation;
import noobanidus.mods.lootr.Lootr;
import org.lwjgl.opengl.GL11;

public class LootrMinecartRenderer extends RenderMinecart {
    private static final ResourceLocation TEXTURE = new ResourceLocation(Lootr.MODID, "textures/entity/lootr_minecart.png");

    public LootrMinecartRenderer() {
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityMinecart cart) {
        return TEXTURE;
    }
}
