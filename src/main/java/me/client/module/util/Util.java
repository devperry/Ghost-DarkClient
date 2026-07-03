package me.client.module.util;
import me.client.Dark;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import net.minecraft.world.World;
import java.util.List;
import java.util.Random;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Mouse;
import net.minecraft.client.settings.KeyBinding;
import me.client.settings.Setting;
import me.client.module.Category;
import me.client.module.Module;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.*;
import net.minecraft.client.*;
import java.lang.reflect.*;
import net.minecraftforge.fml.common.gameevent.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraft.util.BlockPos; 
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.block.Block; 
import net.minecraft.util.MathHelper; 
import net.minecraft.block.material.Material; 
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
public class Util {
    private static final Random random = new Random();
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static KeyBinding keyBindAttack = mc.gameSettings.keyBindAttack;
    public static boolean nullCheck() {
        return mc != null
            && mc.thePlayer != null
            && mc.theWorld != null
            && mc.currentScreen == null
            && !mc.gameSettings.showDebugInfo
            && !Dark.instance.destructed
            && !mc.thePlayer.isRiding()
            && !mc.isGamePaused() 
            && mc.thePlayer.getHealth() > 0.0F; 
    }
    public static Entity getPointedEntityRayTrace(double reachDistance) {
        if (mc.getRenderViewEntity() == null) return null;
        Entity pointedEntity = null;
        Vec3 vec3 = mc.getRenderViewEntity().getPositionEyes(1.0F);
        Vec3 lookVec = mc.getRenderViewEntity().getLook(1.0F);
        Vec3 vec32 = vec3.addVector(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance);
        float f1 = 1.0F;
        List<Entity> list = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.getRenderViewEntity(), mc.getRenderViewEntity().getEntityBoundingBox().addCoord(lookVec.xCoord * reachDistance, lookVec.yCoord * reachDistance, lookVec.zCoord * reachDistance).expand(f1, f1, f1));
        double d2 = reachDistance;
        for (Entity entity : list) {
            if (entity.canBeCollidedWith()) {
                float collisionBorderSize = 0.13F;
                AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
                MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
                if (axisalignedbb.isVecInside(vec3)) {
                    if (0.0D < d2 || d2 == 0.0D) {
                        pointedEntity = entity;
                        d2 = 0.0D;
                    }
                } else if (movingobjectposition != null) {
                    double distance = vec3.distanceTo(movingobjectposition.hitVec);
                    if (distance < d2 || d2 == 0.0D) {
                        if (entity == mc.getRenderViewEntity().ridingEntity && !entity.canRiderInteract()) {
                            if (d2 == 0.0D) {
                                pointedEntity = entity;
                            }
                        } else {
                            pointedEntity = entity;
                            d2 = distance;
                        }
                    }
                }
            }
        }
        return pointedEntity;
    }
    public static double getRayTraceDistance(Entity target, double rayTraceReach) {
        if (mc.getRenderViewEntity() == null || target == null) return Double.MAX_VALUE;
        Vec3 vec3 = mc.getRenderViewEntity().getPositionEyes(1.0F);
        Vec3 lookVec = mc.getRenderViewEntity().getLook(1.0F);
        Vec3 vec32 = vec3.addVector(lookVec.xCoord * rayTraceReach, lookVec.yCoord * rayTraceReach, lookVec.zCoord * rayTraceReach);
        float collisionBorderSize = 0.13F;
        AxisAlignedBB axisalignedbb = target.getEntityBoundingBox().expand(collisionBorderSize, collisionBorderSize, collisionBorderSize);
        MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(vec3, vec32);
        if (movingobjectposition != null) {
            return vec3.distanceTo(movingobjectposition.hitVec);
        }
        return Double.MAX_VALUE;
    }
    public static void SmsToChat(String message) {
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(message));
        }
    }
    public static double getRandomMultiplier(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }
    public static void setTimerRate(final float tick, float sp) {
        try {
            final Field timerField = Minecraft.class.getDeclaredField("field_71428_T");
            final Field tickPSField = net.minecraft.util.Timer.class.getDeclaredField("field_74278_d");
            if (timerField != null) {
                timerField.setAccessible(true);
                final net.minecraft.util.Timer timer = (net.minecraft.util.Timer)timerField.get(mc);
                timerField.setAccessible(false);
                tickPSField.setAccessible(true);
                tickPSField.set(timer, 1.0f + ((float)sp - 1.0f));
                tickPSField.setAccessible(false);
            } else {
                System.out.println("timerfield is null");
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    public static void clickMouse() {
        try {
            Field counter;
            try {
                counter = Minecraft.class.getDeclaredField("leftClickCounter");
            } catch (NoSuchFieldException e) {
                counter = Minecraft.class.getDeclaredField("field_71467_ac"); 
            }
            counter.setAccessible(true);
            counter.setInt(mc, 0);
            Method clickMethod;
            try {
                clickMethod = Minecraft.class.getDeclaredMethod("clickMouse");
            } catch (NoSuchMethodException e) {
                clickMethod = Minecraft.class.getDeclaredMethod("func_147116_af");
            }
            clickMethod.setAccessible(true);
            clickMethod.invoke(mc);
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }
    public static void clickAttack(){
        int keyCode = keyBindAttack.getKeyCode();
        KeyBinding.setKeyBindState(keyCode, true);
        KeyBinding.onTick(keyCode);
        KeyBinding.setKeyBindState(keyCode, false);
    }
    /**
     * NUEVO MÉTODO: Comprueba si la entidad es un jugador y posee TNT en su primer o último slot de la Hotbar.
     * * @param entity Entidad a evaluar
     * @return true si tiene TNT en el slot 1 o 9 de la barra de acceso rápido, false de lo contrario.
     */
    public static boolean hasTntInHotbarEdges(Entity entity) {
        if (entity instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer) entity;
            ItemStack firstSlot = player.inventory.mainInventory[0];
            ItemStack lastSlot = player.inventory.mainInventory[8];
            boolean hasTntFirst = firstSlot != null 
                    && firstSlot.getItem() != null 
                    && Block.getBlockFromItem(firstSlot.getItem()) == Blocks.tnt;
            boolean hasTntLast = lastSlot != null 
                    && lastSlot.getItem() != null 
                    && Block.getBlockFromItem(lastSlot.getItem()) == Blocks.tnt;
            return hasTntFirst || hasTntLast;
        }
        return false;
    }
}
