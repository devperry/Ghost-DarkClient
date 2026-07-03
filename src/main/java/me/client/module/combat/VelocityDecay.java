package me.client.module.combat;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.*;
import me.client.module.util.utilities.TimerUtil;
import me.client.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
public class VelocityDecay extends Module {
    private int velocityTick = 0;
    private int decayStep = 0;
    private boolean isVelocityActive = false;
    private Setting reqSpeedSet;
    private long lastDecayTime = 0L;
    private long currentStepDelay = 0L;
    private final TimerUtil globalCooldown = new TimerUtil();
    private boolean onGlobalCooldown = false;
    private boolean isInCombat = false;
    private long lastAttackTime = 0L;
    public VelocityDecay() {
        super("VelocityDecay", "Simula friccion extrema", Category.COMBAT);
        Dark.instance.settingsManager.rSetting(new Setting("DecayRate", this, 0.6, 0.1, 1.0, false));
        Dark.instance.settingsManager.rSetting(new Setting("DecaySteps", this, 3, 1, 10, true));
        Dark.instance.settingsManager.rSetting(new Setting("HurtTime", this, 9, 1, 10, true));
        Dark.instance.settingsManager.rSetting(reqSpeedSet = new Setting("Required Speed", this, 0.0, 10.0, 0.0, 30.0, false));
        Dark.instance.settingsManager.rSetting(new Setting("OnlyGround", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("OnlyAir", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("Chance", this, 1.0, 0.1, 1.0, false));
        Dark.instance.settingsManager.rSetting(new Setting("OnlyCombat", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("CombatTimeout", this, 1.0, 0.08, 5.0, false));
        Dark.instance.settingsManager.rSetting(new Setting("minStepDelay", this, 50.0, 0.0, 500.0, false));   
        Dark.instance.settingsManager.rSetting(new Setting("maxStepDelay", this, 150.0, 0.0, 500.0, false));  
        Dark.instance.settingsManager.rSetting(new Setting("minCooldown", this, 0.5, 0.0, 5.0, false));  
        Dark.instance.settingsManager.rSetting(new Setting("maxCooldown", this, 1.5, 0.0, 5.0, false));  
        Dark.instance.settingsManager.rSetting(new Setting("Debug", this, false)); 
    }
    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!Util.nullCheck() || event.target == null) {
            isInCombat = false;
            return;
        }
        double timeout = Dark.instance.settingsManager.getSettingByName(this, "CombatTimeout").getValDouble() * 1000D;
        if (System.currentTimeMillis() - lastAttackTime <= (long) timeout) {
            isInCombat = true;
        } else {
            isInCombat = false;
        }
        lastAttackTime = System.currentTimeMillis();
    }
    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent e) {
        if (e.phase != Phase.START) return;
        if (!Util.nullCheck()) return;
        boolean onlyCombat = Dark.instance.settingsManager.getSettingByName(this, "OnlyCombat").getValBoolean();
        double timeout = Dark.instance.settingsManager.getSettingByName(this, "CombatTimeout").getValDouble() * 1000D;
        if (lastAttackTime != 0 && System.currentTimeMillis() - lastAttackTime > (long) timeout) {
            isInCombat = false;
        }
        if (onlyCombat && !isInCombat) return;
        if (onGlobalCooldown) {
            if (globalCooldown.hasCooldownExpired()) {
                onGlobalCooldown = false;
            } else {
                return; 
            }
        }
        if (!MovementUtil.isRequiredSpeed(reqSpeedSet.getValDouble(), reqSpeedSet.getValDouble2())) return;
        int hurtTimeSetting = (int) Dark.instance.settingsManager.getSettingByName(this, "HurtTime").getValDouble();
        if (mc.thePlayer.hurtTime == hurtTimeSetting && !isVelocityActive) {
            double chance = Dark.instance.settingsManager.getSettingByName(this, "Chance").getValDouble();
            if (Math.random() <= chance) {
                isVelocityActive = true;
                velocityTick = 0;
                decayStep = 0;
                lastDecayTime = System.currentTimeMillis();
                generateNextStepDelay();
            }
        }
        if (mc.thePlayer.hurtTime == 0 && isVelocityActive) {
            finishCycle();
        }
        if (isVelocityActive) {
            velocityTick++;
            if (velocityTick == 1) {
                return;
            }
            applyFrictionCurve();
        }
    }
    private void applyFrictionCurve() {
        double baseDecay = Dark.instance.settingsManager.getSettingByName(this, "DecayRate").getValDouble();
        boolean onlyGround = Dark.instance.settingsManager.getSettingByName(this, "OnlyGround").getValBoolean();
        boolean onlyAir = Dark.instance.settingsManager.getSettingByName(this, "OnlyAir").getValBoolean();
        int maxSteps = (int) Dark.instance.settingsManager.getSettingByName(this, "DecaySteps").getValDouble();
        if (onlyGround && !mc.thePlayer.onGround) return;
        if(onlyAir && mc.thePlayer.onGround) return;
        if (decayStep >= maxSteps) {
            finishCycle();
            return;
        }
        long now = System.currentTimeMillis();
        if (now - lastDecayTime < currentStepDelay) {
            return; 
        }
        decayStep++;
        double progress = (double) decayStep / maxSteps;
        double curve = progress * progress;
        double currentDecay = 1.0 - (1.0 - baseDecay) * curve;
        mc.thePlayer.motionX *= currentDecay;
        mc.thePlayer.motionZ *= currentDecay;
        boolean db = Dark.instance.settingsManager.getSettingByName(this, "Debug").getValBoolean();
        if(db){
        Util.SmsToChat("Aplicated");
        }
        lastDecayTime = now;
        generateNextStepDelay();
        if (decayStep >= maxSteps) {
            finishCycle();
        }
    }
    private void finishCycle() {
        isVelocityActive = false;
        velocityTick = 0;
        decayStep = 0;
        startGlobalCooldown();
    }
    private void startGlobalCooldown() {
        double minCd = Dark.instance.settingsManager.getSettingByName(this, "minCooldown").getValDouble() * 1000D;
        double maxCd = Dark.instance.settingsManager.getSettingByName(this, "maxCooldown").getValDouble() * 1000D;
        if (minCd > maxCd) {
            minCd = maxCd;
        }
        long cooldownMs = (long) Util.getRandomMultiplier(minCd, maxCd);
        globalCooldown.setDelay(cooldownMs);
        globalCooldown.reset();
        onGlobalCooldown = true;
    }
    private void generateNextStepDelay() {
        double minDelay = Dark.instance.settingsManager.getSettingByName(this, "minStepDelay").getValDouble();
        double maxDelay = Dark.instance.settingsManager.getSettingByName(this, "maxStepDelay").getValDouble();
        if (minDelay > maxDelay) {
            currentStepDelay = (long) minDelay;
            return;
        }
        currentStepDelay = (long) Util.getRandomMultiplier(minDelay, maxDelay);
    }
}