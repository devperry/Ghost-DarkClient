package me.client.module.combat;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import me.client.module.util.Util;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import io.netty.util.internal.ThreadLocalRandom;
import java.util.List;
import java.util.Random;
public class Wtap extends Module {
    private long lastActivationTime = 0;
    private long lastCooldownAct = 0;
    private boolean isWDeengaged = false;
    private boolean flag = false;
    private boolean wasAttackKeyDown = false;
    private static final Minecraft mc = Minecraft.getMinecraft();    
    private int ticksOnGround = 0;
    private int targetTicks = 0;
    private Setting durationMs;
    private Setting cooldownMs;
    private Setting delayMs;
    private Setting distanceRange;
    private Setting onlySelfHurt;
    private Setting onlyGround;
    private Setting groundTicks; 
public Wtap() {
    super("WTap", "Perfect W-Tap", Category.COMBAT);
    this.durationMs   = new Setting("Duration", this, 140, 150, 5, 200, true);
    Dark.instance.settingsManager.rSetting(durationMs);
    this.cooldownMs   = new Setting("Cooldown", this, 290, 300, 10, 400, true);
    Dark.instance.settingsManager.rSetting(cooldownMs);
    this.delayMs      = new Setting("Delay", this, 0, 10, 0, 100, true);
    Dark.instance.settingsManager.rSetting(delayMs);
    this.distanceRange = new Setting("Distance", this, 1.5, 3.5, 0, 6, false);
    Dark.instance.settingsManager.rSetting(distanceRange);
    this.onlySelfHurt = new Setting("Only On Self Hurt", this, false);
    Dark.instance.settingsManager.rSetting(onlySelfHurt);
    this.onlyGround   = new Setting("Ground Check", this, false);
    Dark.instance.settingsManager.rSetting(onlyGround);
    this.groundTicks  = new Setting("Ground Ticks", this, 1, 2, 0, 10, true);
    Dark.instance.settingsManager.rSetting(groundTicks);
}
    @SubscribeEvent
    public void tickEvent(TickEvent.ClientTickEvent event) {
        if (!Util.nullCheck() || event.phase != Phase.END) return;                   
        if (mc.thePlayer.onGround) {
            if (ticksOnGround == 0) {
                targetTicks = (int) Util.getRandomMultiplier(groundTicks.getValDouble(), groundTicks.getValDouble2());
            }
            ticksOnGround++;
        } else {
            ticksOnGround = 0; 
        }
        boolean isAttackKeyDown = mc.gameSettings.keyBindAttack.isKeyDown();
        if (isAttackKeyDown && !wasAttackKeyDown) {
            flag = true;
        }
        boolean isOnGround = mc.thePlayer.onGround;
        boolean isHurt = mc.thePlayer.hurtTime  == 10;
        wasAttackKeyDown = isAttackKeyDown;        
        boolean passesGroundCheck = !onlyGround.getValBoolean() || (isOnGround && ticksOnGround >= targetTicks);
        if ((flag && !onlySelfHurt.getValBoolean()) || (onlySelfHurt.getValBoolean() && isHurt)) {
            Entity target = Util.getPointedEntityRayTrace(6.0);
            if (((target != null && passesGroundCheck) || (onlySelfHurt.getValBoolean() && isHurt)) && mc.gameSettings.keyBindForward.isKeyDown()) {
                double distance = Util.getRayTraceDistance(target, 6.0);
                if (distance >= distanceRange.getValDouble() && distance <= distanceRange.getValDouble2()) {
                    double cooldown = Util.getRandomMultiplier(cooldownMs.getValDouble(), cooldownMs.getValDouble2());
                    double delay = Util.getRandomMultiplier(delayMs.getValDouble(), delayMs.getValDouble2());
                    if(System.currentTimeMillis() - lastCooldownAct >= cooldown + delay){
                        lastActivationTime = System.currentTimeMillis();
                        isWDeengaged = true;
                        lastCooldownAct = System.currentTimeMillis();                   
                    }                    
                }
            }
            flag = false;
        }
        if (isWDeengaged) {
            if (System.currentTimeMillis() - lastActivationTime >= (Util.getRandomMultiplier(durationMs.getValDouble(), durationMs.getValDouble2()) + Util.getRandomMultiplier(delayMs.getValDouble(), delayMs.getValDouble2()))) {
                isWDeengaged = false;
            } else {
                if (passesGroundCheck){
                    if(mc.thePlayer.isSprinting()){
                        KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                    }
                }
                return;
            }
        }
        if (!isWDeengaged) {
            boolean shouldBePressed = Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), shouldBePressed);            
        }
    }        
}
