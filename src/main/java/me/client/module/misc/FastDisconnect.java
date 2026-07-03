package me.client.module.misc;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.util.Util;
import me.client.module.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
public class FastDisconnect extends Module {
    private boolean originalConnectionState = true;
    public FastDisconnect() {
        super("FastDisconnect", "Se Desconecta Muy *Op* para trollear", Category.MISC);
    }
    @Override
    public void onEnable() {
        if (!Util.nullCheck())
            return;
        originalConnectionState = mc.isSingleplayer() || mc.getIntegratedServer() != null;
        if (!originalConnectionState && mc.thePlayer != null && mc.getNetHandler() != null) {
            mc.getNetHandler().getNetworkManager().closeChannel(null);
        }
    }
    @Override
    public void onDisable() {
        if (!Util.nullCheck())
            return;
        if (!originalConnectionState && mc.getNetHandler() != null) {
            mc.getNetHandler().getNetworkManager().checkDisconnected();
        }
    }
    @SubscribeEvent
    public void onPacket(TickEvent.PlayerTickEvent event) {
        if (Util.nullCheck() && event.phase == TickEvent.Phase.START && !originalConnectionState) {
            event.setCanceled(true);
        }
    }
}
