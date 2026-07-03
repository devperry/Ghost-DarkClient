package me.client.module.movement;
import me.client.Dark;
import me.client.settings.Setting;
import me.client.module.util.Util;
import me.client.module.Category;
import me.client.module.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.Random;
public class FastFall extends Module {
    private static final Random random = new Random();
    private Setting timerMode;    
    private Setting timerSpeed;
    private Setting speedFall;    
    private Setting speedBoost;   
    private Setting cooldownMs;   
    private Setting chance;       
    private Setting motionYRange; 
    private Setting onlySprint;   
    private Setting onlyAir;      
    private Setting minFallDist;  
    private Setting maxMotionY;   
    private long lastTime = 0;
    public FastFall() {
        super("FastFall", "Acelera tu caida", Category.MOVEMENT);
        Dark.instance.settingsManager.rSetting(
            timerMode = new Setting("Timer Mode", this, true)
        );
        Dark.instance.settingsManager.rSetting(
            timerSpeed = new Setting("Timer Speed", this, 1.15, 1.0, 2.0, false)
        );
        Dark.instance.settingsManager.rSetting(
            speedFall = new Setting("Speed", this, 0.15, 0.00, 2.00, false)
        );
        Dark.instance.settingsManager.rSetting(
            speedBoost = new Setting("Speed Boost", this, 0.05, 0.0, 1.0, false)
        );
        Dark.instance.settingsManager.rSetting(
            cooldownMs = new Setting("Cooldown (Ms)", this, 200.0, 0.0, 2000.0, true)
        );
        Dark.instance.settingsManager.rSetting(
            chance = new Setting("Chance", this, 1.0, 0.1, 1.0, false)
        );
        Dark.instance.settingsManager.rSetting(
            motionYRange = new Setting("MotionY Range", this, -0.5, 0.0, -10.0, 0.0, false)
        );
        Dark.instance.settingsManager.rSetting(
            minFallDist = new Setting("FallDist", this, 0.0, 5.0, 0.0, 5.0, false)
        );
        Dark.instance.settingsManager.rSetting(
            maxMotionY = new Setting("Max FallSpeed", this, -5.0, -20.0, -1.0, false)
        );
        Dark.instance.settingsManager.rSetting(
            onlySprint = new Setting("OnlySprint", this, false)
        );
        Dark.instance.settingsManager.rSetting(
            onlyAir = new Setting("OnlyAir", this, true)
        );
    }
    @Override
    public void onEnable() {
        this.lastTime = 0; 
        super.onEnable();
    }
    @Override
    public void onDisable() {
        Util.setTimerRate(1, 1.0f);
        super.onDisable();
    }
    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent e) { 
        if (!Util.nullCheck() || e.phase != TickEvent.Phase.END) return;
        if (mc.thePlayer.onGround || (onlyAir.getValBoolean() && (mc.thePlayer.isOnLadder() || mc.thePlayer.isInWater()))) {
            if (timerMode.getValBoolean()) {
                Util.setTimerRate(1, 1.0f); 
            }
            return; 
        }
        if (mc.thePlayer.fallDistance < minFallDist.getValDouble() || mc.thePlayer.fallDistance > minFallDist.getValDouble2()) return;
        double currentMotionY = mc.thePlayer.motionY;
        double motionMin = motionYRange.getValDouble();  
        double motionMax = motionYRange.getValDouble2(); 
        if (currentMotionY < motionMin || currentMotionY > motionMax) return;
        if (onlySprint.getValBoolean() && !mc.thePlayer.isSprinting()) return;
        if (timerMode.getValBoolean()) {
            Util.setTimerRate(1, (float) timerSpeed.getValDouble());
        } else {
            if (random.nextDouble() > chance.getValDouble()) return;
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastTime < cooldownMs.getValDouble()) return;
            double newMotionY = currentMotionY - speedFall.getValDouble();
            double cap = maxMotionY.getValDouble();
            if (newMotionY < cap) newMotionY = cap;
            mc.thePlayer.motionY = newMotionY;
            double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
            mc.thePlayer.motionX -= Math.sin(yaw) * speedBoost.getValDouble();
            mc.thePlayer.motionZ += Math.cos(yaw) * speedBoost.getValDouble();
            this.lastTime = currentTime;
            Util.SmsToChat("Applied motion speed");
        }
    }
}
