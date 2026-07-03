package me.client.module.combat;

import me.client.Dark;
import me.client.module.util.Util;
import me.client.module.Category;
import me.client.module.Module;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import me.client.module.misc.Team;
import org.lwjgl.input.Mouse;
import me.client.module.util.utilities.TimerUtil;
import java.util.Random;

public class AimAssist extends Module {
    private final TimerUtil Cw = new TimerUtil();
    private Random ra;
    private Minecraft mc;
    private Setting speedSetting;
    private Setting pitchSpeedSetting;
    private Setting pitchSetting;
    private Setting fovSetting;
    private Setting distanceSetting;
    private Setting jitterSetting;
    private Setting hitboxToleranceSetting;
    private Setting onlyVisibleSetting;
    private Setting onlySwordSetting;
    private Setting onlyClickSetting;
    private Team team;
    private double randomXOffset = 0.0;
    private double randomYOffset = 0.0;
    private double randomZOffset = 0.0;
    private long lastTargetChange = 0;
    private float lastMouseDeltaX = 0f;
    private float lastMouseDeltaY = 0f;

    public AimAssist() {
        super("AimAssist", "Nice Aim", Category.COMBAT);
        this.mc = Minecraft.getMinecraft();
        this.ra = new Random();
        this.speedSetting = new Setting("Speed", this, 10.0, 1.0, 25.0, true);
        this.pitchSetting = new Setting("Pitch", this, true);
        this.pitchSpeedSetting = new Setting("PitchSpeed", this, 10.0, 1.0, 25.0, true);
        this.fovSetting = new Setting("FOV", this, 90, 10.0, 360.0, true);
        this.distanceSetting = new Setting("Distance", this, 0.3, 4.3, 0.0, 6.0, false);
        this.jitterSetting = new Setting("Jitter", this, true);
        this.hitboxToleranceSetting = new Setting("HitboxTolerance", this, 5.0, 1.0, 10.0, true);
        this.onlyVisibleSetting = new Setting("OnlyVisible", this, true);
        this.onlySwordSetting = new Setting("OnlySworld", this, false);
        this.onlyClickSetting = new Setting("OnlyClick", this, false);
        Dark.instance.settingsManager.rSetting(speedSetting);
        Dark.instance.settingsManager.rSetting(pitchSetting);
        Dark.instance.settingsManager.rSetting(pitchSpeedSetting);
        Dark.instance.settingsManager.rSetting(fovSetting);
        Dark.instance.settingsManager.rSetting(distanceSetting);
        Dark.instance.settingsManager.rSetting(jitterSetting);
        Dark.instance.settingsManager.rSetting(hitboxToleranceSetting);
        Dark.instance.settingsManager.rSetting(onlyVisibleSetting);
        Dark.instance.settingsManager.rSetting(onlySwordSetting);
        Dark.instance.settingsManager.rSetting(onlyClickSetting);
        Cw.setDelay(200);
    }

    private void applyMouseMovement(float deltaYaw, float deltaPitch, float speedParamYaw, float speedParamPitch, boolean applyJitter) {
        float f = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
        float gcd = f * f * f * 8.0F * 0.15F;
        float speedYaw = speedParamYaw / 100.0f;
        float speedPitch = speedParamPitch / 100.0f;
        float randomizer = 0.8f + (ra.nextFloat() * 0.4f);
        float intendedDx = (deltaYaw * speedYaw * randomizer) / gcd;
        float intendedDy = (deltaPitch * speedPitch * randomizer) / gcd;
        if (applyJitter) {
            if (Cw.hasCooldownExpired()) {
                intendedDx += (ra.nextFloat() - 0.5f) * 3.0f;
                intendedDy += (ra.nextFloat() - 0.5f) * 1.5f;
                Cw.reset();
            }
        }
        intendedDx = (lastMouseDeltaX * 0.45f) + (intendedDx * 0.55f);
        intendedDy = (lastMouseDeltaY * 0.45f) + (intendedDy * 0.55f);
        int mouseDx = Math.round(intendedDx);
        int mouseDy = Math.round(intendedDy);
        lastMouseDeltaX = mouseDx;
        lastMouseDeltaY = mouseDy;
        float actualYawDelta = (float) mouseDx * gcd;
        float actualPitchDelta = (float) mouseDy * gcd;
        if (actualYawDelta == 0.0f && actualPitchDelta == 0.0f) return;
        mc.thePlayer.rotationYaw += actualYawDelta;
        mc.thePlayer.rotationPitch = MathHelper.clamp_float(mc.thePlayer.rotationPitch + actualPitchDelta, -90.0F, 90.0F);
    }

    private float[] getRotationsToWanderingPoint(Entity ent) {
        if (System.currentTimeMillis() - lastTargetChange > 150 + ra.nextInt(200)) {
            randomXOffset = (ra.nextFloat() - 0.5) * 0.4;
            randomZOffset = (ra.nextFloat() - 0.5) * 0.4;
            randomYOffset = (ent.getEyeHeight() * 0.7) + (ra.nextFloat() * 0.2);
            lastTargetChange = System.currentTimeMillis();
        }
        double diffX = (ent.posX + randomXOffset) - mc.thePlayer.posX;
        double diffZ = (ent.posZ + randomZOffset) - mc.thePlayer.posZ;
        double diffY = (ent.posY + randomYOffset) - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double dist = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);
        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, dist));
        return new float[]{yaw, pitch};
    }

    @SubscribeEvent
    public void tickEvent(final TickEvent.RenderTickEvent event) {
        if (event.phase != Phase.END || !Util.nullCheck() || mc.currentScreen != null) {
            return;
        }
        if (team == null) team = (Team) Dark.instance.moduleManager.getModule("Team");
        if (onlyClickSetting.getValBoolean() && !Mouse.isButtonDown(0)) {
            lastMouseDeltaX = 0f;
            lastMouseDeltaY = 0f;
            return;
        }
        if (onlySwordSetting.getValBoolean() && !isSwordInHand()) {
            lastMouseDeltaX = 0f;
            lastMouseDeltaY = 0f;
            return;
        }
        final Entity target = this.ent();
        if (target != null) {
            float[] rotations = getRotationsToWanderingPoint(target);
            float targetYaw = rotations[0];
            float targetPitch = rotations[1];
            float deltaYaw = MathHelper.wrapAngleTo180_float(targetYaw - mc.thePlayer.rotationYaw);
            float deltaPitch = targetPitch - mc.thePlayer.rotationPitch;
            float tolerance = (float) hitboxToleranceSetting.getValDouble();
            if (Math.abs(deltaYaw) < tolerance && Math.abs(deltaPitch) < tolerance) {
                lastMouseDeltaX *= 0.5f;
                lastMouseDeltaY *= 0.5f;
                return;
            }
            float speedYaw = (float) speedSetting.getValDouble();
            float speedPitch = (float) pitchSpeedSetting.getValDouble();
            boolean doPitch = pitchSetting.getValBoolean();
            boolean doJitter = jitterSetting.getValBoolean();
            applyMouseMovement(deltaYaw, doPitch ? deltaPitch : 0f, speedYaw, speedPitch, doJitter);
        } else {
            lastMouseDeltaX *= 0.7f;
            lastMouseDeltaY *= 0.7f;
        }
    }

    public double entityPosCompare(final Entity ent) {
        return ((this.mc.thePlayer.rotationYaw - rotationUntilTargetOriginal(ent)) % 360.0 + 540.0) % 360.0 - 180.0;
    }

    private float rotationUntilTargetOriginal(Entity ent) {
        double diffX = ent.posX - this.mc.thePlayer.posX;
        double diffZ = ent.posZ - this.mc.thePlayer.posZ;
        return (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
    }

    public boolean isFovLargeEnough(final Entity en, float a) {
        final double v = entityPosCompare(en);
        return (v > 0.0 && v < a) || (-a < v && v < 0.0);
    }

    private Entity ent() {
        Entity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;
        float fov = (float) fovSetting.getValDouble();
        float distance = (float) distanceSetting.getValDouble2();
        float mindistance = (float) distanceSetting.getValDouble();
        for (Entity entity : this.mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityPlayer) || entity.equals(mc.thePlayer)) {
                continue;
            }
            if (mc.getNetHandler() != null && mc.getNetHandler().getPlayerInfo(entity.getUniqueID()) == null) {
                continue;
            }
            double distanceToEntity = mc.thePlayer.getDistanceToEntity(entity);
            if (distanceToEntity <= distance && isFovLargeEnough(entity, fov)) {
                if (team != null && Dark.instance.moduleManager.getModule("Team").isToggled() && team.isTeam((EntityPlayer) entity))
                    continue;
                if (onlyVisibleSetting.getValBoolean() && !isEntityVisible(entity)) {
                    continue;
                }
                if (distanceToEntity < closestDistance) {
                    closestEntity = entity;
                    closestDistance = distanceToEntity;
                }
            }
        }
        if (closestEntity != null) {
            if (mc.thePlayer.getDistanceToEntity(closestEntity) < mindistance) {
                return null;
            }
        }
        return closestEntity;
    }

    private boolean isSwordInHand() {
        return mc.thePlayer.getHeldItem() != null && mc.thePlayer.getHeldItem().getItem()
                        instanceof ItemSword;
    }

    private boolean isEntityVisible(Entity entity) {
        if (entity == null) return false;
        Vec3 playerEyes = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 entityPos = entity.getPositionVector().addVector(0, entity.getEyeHeight(), 0);
        MovingObjectPosition result = mc.theWorld.rayTraceBlocks(playerEyes, entityPos, false, false, true);
        return result == null || result.typeOfHit == MovingObjectPosition.MovingObjectType.MISS;
    }
}