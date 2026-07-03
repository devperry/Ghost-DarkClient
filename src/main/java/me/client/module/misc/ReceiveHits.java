package me.client.module.misc;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
public class ReceiveHits extends Module {
    private final Minecraft mc = Minecraft.getMinecraft();
    private Setting hitsSetting;
    private Setting distanceSetting;
    private Setting delaySetting;
    private Setting distanceBasedSetting; 
    private int hitCounter;
    private boolean Hab;
    private boolean justHit = false;
    private boolean delayActive = false;  
    private int delayTicksRemaining = 0;  
    public ReceiveHits() {  
        super("ReceiveHits", "Se desactiva despues de recibir una cantidad de golpes", Category.MISC);  
        this.hitsSetting = new Setting("Hits", this, 1, 1, 4, true);  
        this.distanceSetting = new Setting("Distance Reset", this, 3.9, 1, 6, false);  
        this.delaySetting = new Setting("Delay ticks", this, 0, 0, 5, true);  
        this.distanceBasedSetting = new Setting("Distance Based", this, false);
        Dark.instance.settingsManager.rSetting(this.distanceBasedSetting);  
        Dark.instance.settingsManager.rSetting(this.delaySetting);  
        Dark.instance.settingsManager.rSetting(this.distanceSetting);  
        Dark.instance.settingsManager.rSetting(this.hitsSetting);  
        this.hitCounter = 0;  
    }  
    @Override  
    public void onEnable() {  
        super.onEnable();  
        this.resetState();
    }  
    @Override  
    public void onDisable() {  
        super.onDisable();  
        this.resetState();
    }
    private void resetState() {
        this.Hab = false;  
        this.hitCounter = 0;  
        this.justHit = false;  
        this.delayActive = false;  
        this.delayTicksRemaining = 0;
    }
    @SubscribeEvent  
    public void onClientTick(TickEvent.ClientTickEvent event) {  
        if (event.phase != TickEvent.Phase.END) return;  
        if (!Util.nullCheck()) return;
        if (!mc.thePlayer.isEntityAlive()) {
            resetState();
            return;
        }
        boolean distB = this.distanceBasedSetting.getValBoolean();  
        if (delayActive) {  
            delayTicksRemaining--;  
            if (delayTicksRemaining <= 0) {  
                delayActive = false;  
                if (!distB) {  
                    this.setToggled(false);  
                    return;  
                } else {  
                    Hab = true;  
                    hitCounter = 0;  
                    justHit = false;  
                    return; 
                }  
            } else {  
                return; 
            }  
        }  
        boolean isHurt = mc.thePlayer.hurtTime > 0 && mc.thePlayer.hurtTime >= mc.thePlayer.maxHurtTime - 1;
        if (!Hab && isHurt && !justHit && !delayActive) {  
            hitCounter++;  
            justHit = true;  
            int hitsLimit = (int) hitsSetting.getValDouble();  
            if (hitCounter >= hitsLimit) {  
                int delay = (int) delaySetting.getValDouble();
                if (delay <= 0) {
                    if (!distB) {
                        this.setToggled(false);
                        return;
                    } else {
                        Hab = true;
                        hitCounter = 0;
                        justHit = true; 
                    }
                } else {
                    delayActive = true;  
                    delayTicksRemaining = delay;  
                    hitCounter = 0; 
                }
            }  
        } else if (!isHurt) {  
            justHit = false;  
        }  
        if (distB && Hab) {  
            EntityPlayer closestPlayer = findClosestPlayer();  
            float distance = (float) distanceSetting.getValDouble();  
            if (closestPlayer == null || mc.thePlayer.getDistanceToEntity(closestPlayer) > distance) {  
                Hab = false;  
                justHit = false;  
                hitCounter = 0; 
            }  
        }  
    }  
    private EntityPlayer findClosestPlayer() {  
        EntityPlayer closest = null;  
        double minDistanceSq = Double.MAX_VALUE;  
        for (EntityPlayer player : mc.theWorld.playerEntities) {  
            if (player == mc.thePlayer || player.isDead || !player.isEntityAlive()) {  
                continue;  
            }  
            double distanceSq = mc.thePlayer.getDistanceSqToEntity(player);  
            if (distanceSq < minDistanceSq) {  
                minDistanceSq = distanceSq;  
                closest = player;  
            }  
        }  
        return closest;  
    }  
    public boolean isHab() {  
        return Hab;  
    }
}
