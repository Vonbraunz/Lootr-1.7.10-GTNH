package noobanidus.mods.lootr;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.7.10")
public class LootrCore implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        // Mixins are loaded via the GTNH convention plugin which handles UniMixins.
        // The mixins JSON config files specify which mixins to load.
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
