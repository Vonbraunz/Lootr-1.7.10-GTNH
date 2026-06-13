package noobanidus.mods.lootr.client.block;

import net.minecraft.client.model.ModelChest;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import noobanidus.mods.lootr.Lootr;
import noobanidus.mods.lootr.block.tile.LootrChestTileEntity;
import noobanidus.mods.lootr.config.ConfigManager;
import org.lwjgl.opengl.GL11;

import java.util.UUID;

public class SpecialLootChestTileRenderer extends TileEntitySpecialRenderer {
    private static final ResourceLocation TEXTURE_NORMAL = new ResourceLocation("textures/entity/chest/normal.png");
    public static final ResourceLocation MATERIAL_NOT_OPENED = new ResourceLocation(Lootr.MODID, "textures/entity/chest/lootr_chest.png");
    public static final ResourceLocation MATERIAL_OPENED = new ResourceLocation(Lootr.MODID, "textures/entity/chest/lootr_chest_opened.png");
    private final ModelChest simpleChest = new ModelChest();
    private UUID playerId = null;

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks) {
        if (!(te instanceof LootrChestTileEntity)) return;
        LootrChestTileEntity tile = (LootrChestTileEntity) te;
        TileEntityChest chest = (TileEntityChest) te;
        int meta = te.hasWorldObj() ? te.getBlockMetadata() : 2;

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslatef((float) x, (float) y + 1.0F, (float) z + 1.0F);
        GL11.glScalef(1.0F, -1.0F, -1.0F);
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);

        int rotation = 0;
        switch (meta & 3) {
            case 2: rotation = 180; break;
            case 3: rotation = 0; break;
            case 4: rotation = 90; break;
            case 5: rotation = -90; break;
        }
        GL11.glRotatef(rotation, 0.0F, 1.0F, 0.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

        float lidAngle = chest.prevLidAngle + (chest.lidAngle - chest.prevLidAngle) * partialTicks;
        lidAngle = 1.0F - lidAngle;
        lidAngle = 1.0F - lidAngle * lidAngle * lidAngle;
        simpleChest.chestLid.rotateAngleX = -(lidAngle * (float) Math.PI / 2.0F);

        bindTexture(getChestTexture(tile));
        simpleChest.renderAll();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glPopMatrix();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private ResourceLocation getChestTexture(LootrChestTileEntity tile) {
        if (ConfigManager.isVanillaTextures()) return TEXTURE_NORMAL;
        if (playerId == null) {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
            if (mc.thePlayer == null) return MATERIAL_NOT_OPENED;
            playerId = mc.thePlayer.getUniqueID();
        }
        if (tile.getOpeners().contains(playerId)) return MATERIAL_OPENED;
        return MATERIAL_NOT_OPENED;
    }
}
