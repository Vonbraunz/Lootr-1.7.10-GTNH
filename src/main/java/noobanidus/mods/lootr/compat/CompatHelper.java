package noobanidus.mods.lootr.compat;

import cpw.mods.fml.common.Loader;

public class CompatHelper {
    public static boolean isRecComplexLoaded() {
        return Loader.isModLoaded("reccomplex");
    }
}
