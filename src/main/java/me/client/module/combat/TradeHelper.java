package me.client.module.combat;
import me.client.Dark;
import me.client.settings.Setting;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import me.client.module.util.utilities.TimerUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;
public class TradeHelper extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private Setting distance;
    private Setting ms;
    private final TimerUtil attackTimer = new TimerUtil();
    private long nextDelay = 0;
    public TradeHelper() {
        super("TradeHelper", "Util Para Tnt Tag", Category.COMBAT);
        this.distance = new Setting("Distance", this, 2.96, 5, 0, 5, false);
        this.ms = new Setting("Ms", this, 0, 200, 0, 500, true);
        Dark.instance.settingsManager.rSetting(distance);
        Dark.instance.settingsManager.rSetting(ms);
    }
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!Util.nullCheck()) return;
        if (Mouse.isButtonDown(0)) return;
        String displayName = mc.thePlayer.getDisplayName().getUnformattedText().toLowerCase();
        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player != mc.thePlayer) {
                boolean hasTnt = Util.hasTntInHotbarEdges(mc.thePlayer) && !Util.hasTntInHotbarEdges(player);
                if (hasTnt &&
                    isWithinDistance(mc.thePlayer, player, distance.getValDouble(), distance.getValDouble2())) {
                    if (attackTimer.hasCooldownExpired()) {
                        Util.clickAttack();
                        attackTimer.reset();
                        attackTimer.setDelay(nextDelay);
                        nextDelay = (long) Util.getRandomMultiplier(ms.getValDouble(),ms.getValDouble2());
                    }
                }
            }
        }
    }
    private boolean isWithinDistance(EntityPlayer player1, EntityPlayer player2, double minDistance, double maxDistance) {
        double distance = player1.getDistanceToEntity(player2);
        return distance >= minDistance && distance <= maxDistance;
    }
}