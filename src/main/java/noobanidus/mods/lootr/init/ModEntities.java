package noobanidus.mods.lootr.init;

import cpw.mods.fml.common.registry.EntityRegistry;
import noobanidus.mods.lootr.Lootr;
import noobanidus.mods.lootr.entity.LootrChestMinecartEntity;

public class ModEntities {
    public static void init() {
        EntityRegistry.registerModEntity(LootrChestMinecartEntity.class, "lootr_minecart", 1, Lootr.instance, 64, 1, true);
    }
}
