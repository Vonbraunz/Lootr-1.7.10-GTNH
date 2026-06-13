package noobanidus.mods.lootr;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import noobanidus.mods.lootr.command.LootrCommand;
import noobanidus.mods.lootr.init.ModBlocks;
import noobanidus.mods.lootr.setup.CommonSetup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Lootr.MODID, version = Tags.VERSION, name = "Lootr", acceptedMinecraftVersions = "[1.7.10]")
public class Lootr {
    public static final Logger LOG = LogManager.getLogger("Lootr");
    public static final String MODID = "lootr";

    @Mod.Instance("lootr")
    public static Lootr instance;

    @SidedProxy(clientSide = "noobanidus.mods.lootr.setup.ClientSetup", serverSide = "noobanidus.mods.lootr.setup.ServerSetup")
    public static CommonSetup proxy;

    public static CreativeTabs TAB = new CreativeTabs(MODID) {
        @Override
        public Item getTabIconItem() {
            return Item.getItemFromBlock(ModBlocks.CHEST);
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        event.registerServerCommand(new LootrCommand());
    }
}
