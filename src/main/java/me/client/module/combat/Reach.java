package me.client.module.combat;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import me.client.module.util.utilities.TimerUtil;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.List;
import java.util.Random;
public class Reach extends Module {
    private static MovingObjectPosition moving;
    private static float theReach = 3.0F;
    private static final Random random = new Random();
    private final TimerUtil cooldownReach = new TimerUtil();
    private int tickCounter;
    private Setting reach;
    private Setting chanse;
    private Setting delayTicks;
    private Setting tradeMode;
    private Setting comboMode;
    private Setting ghostTapMode;
    private Setting onlySprint;
    private Setting cooldown;
    public Reach() {
        super("Reach", "The Best Reach of all Time", Category.COMBAT);
        reach = new Setting("Reach", this, 3.0, 3.4, 3.0, 6.0, false);
        chanse = new Setting("Chanse", this, 0.2, 0.1, 1.0, false);
        cooldown = new Setting("Cooldown", this, 0.0, 0.4, 0.0, 5.0, false);
        delayTicks = new Setting("DelayTicks", this, 0, 0, 10, true);
        tradeMode = new Setting("Trade Mode", this, false);
        comboMode = new Setting("Combo Mode", this, false);
        ghostTapMode = new Setting("GhostTap Mode", this, false);
        onlySprint = new Setting("Only Sprint", this, false);
        Dark.instance.settingsManager.rSetting(reach);
        Dark.instance.settingsManager.rSetting(chanse);
        Dark.instance.settingsManager.rSetting(cooldown);
        Dark.instance.settingsManager.rSetting(delayTicks);
        Dark.instance.settingsManager.rSetting(tradeMode);
        Dark.instance.settingsManager.rSetting(comboMode);
        Dark.instance.settingsManager.rSetting(ghostTapMode);
        Dark.instance.settingsManager.rSetting(onlySprint);
    }
    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        try {
            if (moving != null && e.button == 0 && e.buttonstate) {
                Minecraft.getMinecraft().objectMouseOver = moving;
            }
        } catch (Exception er) {
            er.printStackTrace();
        }
    }
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END || !Util.nullCheck()) return;
        if (!cooldownReach.hasCooldownExpired()) {
            theReach = 3.0F;
            getMouseOver(1.0F);
            return;
        }
        float rMax = (float) reach.getValDouble2();
        float rMin = (float) reach.getValDouble();
        double rChanse = chanse.getValDouble();
        int tDelay = (int) delayTicks.getValDouble();
        if (tickCounter < tDelay) {
            tickCounter++;
            return;
        } else {
            tickCounter = 0;
        }
        boolean isMoving = Minecraft.getMinecraft().thePlayer.moveForward != 0 || Minecraft.getMinecraft().thePlayer.moveStrafing != 0;
        boolean isSprinting = Minecraft.getMinecraft().thePlayer.isSprinting();
        boolean foward = Minecraft.getMinecraft().thePlayer.moveForward != 0;
        boolean isHurt = Minecraft.getMinecraft().thePlayer.hurtResistantTime > 0;
        boolean onGround = mc.thePlayer.onGround;
        if (tradeMode.getValBoolean()) {
            theReach = isHurt || !onGround ? (random.nextFloat() < rChanse ? rMax : rMin) : rMin;
        }
        else if (comboMode.getValBoolean()) {
            theReach = !isHurt && isMoving ? (random.nextFloat() < rChanse ? rMax : rMin) : rMin;
        }
        else if (ghostTapMode.getValBoolean()) {
            theReach = !foward ? (random.nextFloat() < rChanse ? rMax : rMin) : rMin;
        }
        else if (onlySprint.getValBoolean()) {
            theReach = isSprinting ? (random.nextFloat() < rChanse ? rMax : rMin) : rMin;
        } 
        else {
            theReach = (random.nextFloat() < rChanse ? rMax : rMin);
        }
        if (theReach > 3.0F) {
            double cooldownR = Util.getRandomMultiplier(cooldown.getValDouble(), cooldown.getValDouble2());
            cooldownReach.setDelay((long) (cooldownR * 1000));
            cooldownReach.reset();
        }
        getMouseOver(1.0F);
    }
    public void getMouseOver(float partialTicks) {
        if (Minecraft.getMinecraft().getRenderViewEntity() != null && Minecraft.getMinecraft().theWorld != null) {
            Minecraft.getMinecraft().pointedEntity = null;
            double d0 = (double) theReach;
            moving = Minecraft.getMinecraft().getRenderViewEntity().rayTrace(d0, partialTicks);
            double d1 = d0;
            Vec3 vec3 = Minecraft.getMinecraft().getRenderViewEntity().getPositionEyes(partialTicks);
            if (moving != null) {
                d1 = moving.hitVec.distanceTo(vec3);
            }
            Vec3 vec31 = Minecraft.getMinecraft().getRenderViewEntity().getLook(partialTicks);
            Vec3 vec32 = vec3.addVector(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0);
            Entity pointedEntity = null;
            Vec3 vec33 = null;
            float f1 = 1.0F;
            List<Entity> list = Minecraft.getMinecraft().theWorld.getEntitiesWithinAABBExcludingEntity(Minecraft.getMinecraft().getRenderViewEntity(), Minecraft.getMinecraft().getRenderViewEntity().getEntityBoundingBox().addCoord(vec31.xCoord * d0, vec31.yCoord * d0, vec31.zCoord * d0).expand((double) f1, (double) f1, (double) f1));
            double d2 = d1;
            for (int i = 0; i < list.size(); ++i) {
                Entity entity = list.get(i);
                if (entity.canBeCollidedWith()) {
                    float f2 = 0.13F;
                    AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expand((double) f2, (double) f2, (double) f2);
                    MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                    if (axisalignedbb.isVecInside(vec3)) {
                        if (0.0D < d2 || d2 == 0.0D) {
                            pointedEntity = entity;
                            vec33 = movingobjectposition == null ? vec3 : movingobjectposition.hitVec;
                            d2 = 0.0D;
                        }
                    } else if (movingobjectposition != null) {
                        double d3 = vec3.distanceTo(movingobjectposition.hitVec);
                        if (d3 < d2 || d2 == 0.0D) {
                            if (entity == Minecraft.getMinecraft().getRenderViewEntity().ridingEntity && !entity.canRiderInteract()) {
                                if (d2 == 0.0D) {
                                    pointedEntity = entity;
                                    vec33 = movingobjectposition.hitVec;
                                }
                            } else {
                                pointedEntity = entity;
                                vec33 = movingobjectposition.hitVec;
                                d2 = d3;
                            }
                        }
                    }
                }
            }
            if (pointedEntity != null && (d2 < d1 || moving == null)) {
                moving = new MovingObjectPosition(pointedEntity, vec33);
                if (pointedEntity instanceof EntityLivingBase || pointedEntity instanceof EntityItemFrame) {
                    Minecraft.getMinecraft().pointedEntity = pointedEntity;
                }
            }
        }
    }
}
