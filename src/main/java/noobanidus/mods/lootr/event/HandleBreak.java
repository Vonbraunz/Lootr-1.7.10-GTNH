package noobanidus.mods.lootr.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.world.BlockEvent;
import noobanidus.mods.lootr.config.ConfigManager;
import noobanidus.mods.lootr.init.ModBlocks;

public class HandleBreak {

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        EntityPlayer player = event.getPlayer();
        if (event.world.isRemote) return;

        if (ModBlocks.isLootContainer(event.block)) {
            if (player instanceof FakePlayer && ConfigManager.ENABLE_FAKE_PLAYER_BREAK) return;

            if (ConfigManager.DISABLE_BREAK) {
                if (player.capabilities.isCreativeMode) {
                    if (!player.isSneaking()) {
                        event.setCanceled(true);
                        ChatComponentTranslation msg = new ChatComponentTranslation("lootr.message.cannot_break_sneak");
                        msg.getChatStyle().setColor(EnumChatFormatting.AQUA);
                        ((ICommandSender) player).addChatMessage(msg);
                    }
                } else {
                    event.setCanceled(true);
                    ChatComponentTranslation msg = new ChatComponentTranslation("lootr.message.cannot_break");
                    msg.getChatStyle().setColor(EnumChatFormatting.AQUA);
                    ((ICommandSender) player).addChatMessage(msg);
                }
            } else {
                if (!player.isSneaking()) {
                    event.setCanceled(true);
                    ChatComponentTranslation msg1 = new ChatComponentTranslation("lootr.message.should_sneak");
                    msg1.getChatStyle().setColor(EnumChatFormatting.AQUA);
                    ((ICommandSender) player).addChatMessage(msg1);

                    ChatComponentTranslation sub = new ChatComponentTranslation("lootr.message.should_sneak3");
                    sub.getChatStyle().setColor(EnumChatFormatting.AQUA);
                    ChatComponentTranslation msg2 = new ChatComponentTranslation("lootr.message.should_sneak2", sub);
                    msg2.getChatStyle().setColor(EnumChatFormatting.AQUA);
                    ((ICommandSender) player).addChatMessage(msg2);
                }
            }
        }
    }
}
