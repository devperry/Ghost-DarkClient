package me.client.module.player;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
public class AutoLeave extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    public AutoLeave() {
        super("AutoLeave", "Ejecuta el comando Leave al terminar una partida", Category.PLAYER);
    }
    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        if (!Util.nullCheck())
            return;
        if (!this.isToggled()) return;
        String message = event.message.getUnformattedText();
        if (message.contains("Ganadores") || message.contains("Ganador") || message.contains("HAS PERDIDO")  || message.contains("HAS GANADO") || message.contains("Jugar de nuevo")) {
            mc.thePlayer.sendChatMessage("/leave");
            return;
        }
    }
}
