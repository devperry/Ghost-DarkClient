package me.client.module.combat;
import me.client.module.Category;
import me.client.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import me.client.module.util.Util;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScorePlayerTeam;
public class TimmingHit extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private int countdown = 0; 
    private boolean isCountingDown = false; 
    private long lastTickTime = 0; 
    private boolean attackScheduled = false; 
    private long attackTime = 0; 
    public TimmingHit() {
        super("TimmingHit", "Espera al ultimo segundo para dar con la tnt", Category.COMBAT);
    }
    private long lastDebugPrint = 0;
@SubscribeEvent
public void onTick(TickEvent.ClientTickEvent event) {
    if (!Util.nullCheck()) return;
    long now = System.currentTimeMillis();
    if (now - lastDebugPrint < 2000) return;
    lastDebugPrint = now;
    Scoreboard sb = mc.theWorld.getScoreboard();
    ScoreObjective obj = sb.getObjectiveInDisplaySlot(1); 
    if (obj == null) {
        Util.SmsToChat("§c[Debug] No hay objective en el sidebar.");
        return;
    }
    Util.SmsToChat("§a[Debug] Titulo: " + obj.getDisplayName());
    for (Score score : sb.getSortedScores(obj)) {
        if (score.getPlayerName().startsWith("#")) continue; 
        ScorePlayerTeam team = sb.getPlayersTeam(score.getPlayerName());
        String line = ScorePlayerTeam.formatPlayerName(team, score.getPlayerName());
        String clean = line.replaceAll("§.", "");
        Util.SmsToChat("§e[Debug] '" + clean + "' (score=" + score.getScorePoints() + ")");
    }
}
}
