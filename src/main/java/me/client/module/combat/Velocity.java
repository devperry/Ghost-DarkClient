package me.client.module.combat;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import me.client.module.util.MovementUtil;
import me.client.module.util.utilities.TimerUtil; 
import me.client.settings.Setting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import java.util.Random;
public class Velocity extends Module {
    private static final Random random = new Random();
    private final TimerUtil timer = new TimerUtil();
    private int lastApplyTick = 0;
    private boolean isInCombat;
    private long lastAttackTime = 0L;
    private boolean pendingVelocityApply = false;
    private int scheduledApplyTick = 0;
    private Setting horizontalSet, verticalSet, distanceSet, cooldownSet, hurtTimeSet;
    private Setting reqSpeedSet, chanceSet, delayTicksSet, onlyGroundSet, onlyAirSet,fs;
    private Setting onlyForwardSet, onlySprintSet, onlyTargetSet, onlyCombatSet, onlyClickSet, debugSet;
    public Velocity() {
        super("Velocity", "Reduce el knockback limpiamente", Category.COMBAT);
        Dark.instance.settingsManager.rSetting(fs = new Setting("SpeedBoost", this, 0.05, 0.2, 0.0, 0.5, false));
        Dark.instance.settingsManager.rSetting(horizontalSet = new Setting("Horizontal", this, 30.0, 100.0, -200.0, 100.0, false));
        Dark.instance.settingsManager.rSetting(verticalSet = new Setting("Vertical", this, 95.0, 100.0, -200.0, 100.0, false));
        Dark.instance.settingsManager.rSetting(distanceSet = new Setting("Distance", this, 0.0, 1.5, 0.0, 6.0, false));
        Dark.instance.settingsManager.rSetting(cooldownSet = new Setting("Cooldown", this, 0.5, 0.5, 0.0, 3.0, false));
        Dark.instance.settingsManager.rSetting(hurtTimeSet = new Setting("HurtTime", this, 1.0, 10.0, 1.0, 10.0, true));
        Dark.instance.settingsManager.rSetting(reqSpeedSet = new Setting("Required Speed", this, 0.0, 10.0, 0.0, 30.0, false));
        Dark.instance.settingsManager.rSetting(chanceSet = new Setting("Chance", this, 0.7, 0.1, 1.0, false));
        Dark.instance.settingsManager.rSetting(delayTicksSet = new Setting("DelayTicks", this, 0, 0, 10, true));
        Dark.instance.settingsManager.rSetting(onlyGroundSet = new Setting("OnlyGround", this, false));
        Dark.instance.settingsManager.rSetting(onlyAirSet = new Setting("OnlyAir", this, true));
        Dark.instance.settingsManager.rSetting(onlyForwardSet = new Setting("OnlyForward", this, true));
        Dark.instance.settingsManager.rSetting(onlySprintSet = new Setting("OnlySprint", this, false));
        Dark.instance.settingsManager.rSetting(onlyTargetSet = new Setting("OnlyTarget", this, false));
        Dark.instance.settingsManager.rSetting(onlyCombatSet = new Setting("OnlyCombat", this, false));      
        Dark.instance.settingsManager.rSetting(onlyClickSet = new Setting("OnlyClick", this, false));
        Dark.instance.settingsManager.rSetting(debugSet = new Setting("Debug", this, true));
    }
    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent e) {
        if (!Util.nullCheck() || e.phase != Phase.START) return;
        if (System.currentTimeMillis() - lastAttackTime > 100) {
            isInCombat = false;
        }
        if (onlyCombatSet.getValBoolean() && !isInCombat) return;
        double chance = chanceSet.getValDouble();
        if (random.nextDouble() > chance) return;
        if (!MovementUtil.isRequiredSpeed(reqSpeedSet.getValDouble(), reqSpeedSet.getValDouble2())) return;
        double minDistance = distanceSet.getValDouble(); 
        double maxDistance = distanceSet.getValDouble2(); 
        Entity targetEntity = getClosestEntityInRange(minDistance, maxDistance);
        if (targetEntity == null) return;
        if (onlyClickSet.getValBoolean() && !Mouse.isButtonDown(0)) return;
        if (onlyTargetSet.getValBoolean() && mc.objectMouseOver.entityHit == null) return;
        boolean onGround = mc.thePlayer.onGround;
        if (onGround && onlyAirSet.getValBoolean()) return;
        if (!onGround && onlyGroundSet.getValBoolean()) return;
        if (onlySprintSet.getValBoolean() && !mc.thePlayer.isSprinting()) return;
        if (onlyForwardSet.getValBoolean() && mc.thePlayer.moveForward == 0) return;
        int tickSet = (int) delayTicksSet.getValDouble();
        boolean executeApply = false;
        if (tickSet > 0) {
            if (pendingVelocityApply && mc.thePlayer.ticksExisted >= scheduledApplyTick) {
                executeApply = true;
                pendingVelocityApply = false; 
            }
        } else {
            int minHurtTime = (int) hurtTimeSet.getValDouble();
            int maxHurtTime = (int) hurtTimeSet.getValDouble2();
            if (mc.thePlayer.hurtTime >= minHurtTime && mc.thePlayer.hurtTime <= maxHurtTime) {
                executeApply = true;
            }
        }
        if (!executeApply) return;
        if (!timer.hasCooldownExpired()) return;
        double minHorizontal = horizontalSet.getValDouble() / 100D;
        double maxHorizontal = horizontalSet.getValDouble2() / 100D;        
        double minVertical = verticalSet.getValDouble() / 100D;
        double maxVertical = verticalSet.getValDouble2() / 100D;
        double h = Util.getRandomMultiplier(minHorizontal, maxHorizontal);
        double v = Util.getRandomMultiplier(minVertical, maxVertical);
        double cw = Util.getRandomMultiplier(cooldownSet.getValDouble() * 1000D, cooldownSet.getValDouble2() * 1000D);
        mc.thePlayer.motionX *= h;
        mc.thePlayer.motionY *= v;
        mc.thePlayer.motionZ *= h;
        timer.setDelay((long) cw);
        timer.reset();
        lastApplyTick = mc.thePlayer.ticksExisted;
        //if (mc.thePlayer.motionY > 0) {
            double yaw = Math.toRadians(mc.thePlayer.rotationYaw);
            double sp = Util.getRandomMultiplier(fs.getValDouble(),fs.getValDouble2());
            mc.thePlayer.motionX -= Math.sin(yaw) * sp;
            mc.thePlayer.motionZ += Math.cos(yaw) * sp;     
            Util.SmsToChat("Motion applied: " + sp);
        //}
        if (debugSet.getValBoolean()) {
            if(this.isClone){
            Util.SmsToChat(this.getName()+" ! H: " + h + " V: " + v + " | HurtTime: " + mc.thePlayer.hurtTime + " | Delay Ticks: " + tickSet);
            }else {
            Util.SmsToChat("Velocity! H: " + h + " V: " + v + " | HurtTime: " + mc.thePlayer.hurtTime + " | Delay Ticks: " + tickSet);
            }
        }
    }
    private Entity getClosestEntityInRange(double minDistance, double maxDistance) {
        Entity closestEntity = null;
        double currentClosestDistance = Double.MAX_VALUE;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityLivingBase) || entity == mc.thePlayer || entity.isDead) continue;
            double distanceToEntity = mc.thePlayer.getDistanceToEntity(entity);
            if (distanceToEntity < currentClosestDistance) {
                currentClosestDistance = distanceToEntity;
                closestEntity = entity;
            }
        }
        if (closestEntity != null && currentClosestDistance >= minDistance && currentClosestDistance <= maxDistance) {
            return closestEntity;
        }
        return null;
    }
    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!Util.nullCheck() || event.target == null) return;
        isInCombat = true;
        lastAttackTime = System.currentTimeMillis();
        int tickSet = (int) delayTicksSet.getValDouble();
        if (tickSet > 0) {
            pendingVelocityApply = true;
            scheduledApplyTick = mc.thePlayer.ticksExisted + tickSet;
        }
    }
}