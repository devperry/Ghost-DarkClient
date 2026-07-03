package me.client.module.combat;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
public class STap extends Module {
    /*
    TODO: Epaleee Compadreee El Codigo Es Libre Pero Recuerda darme los creditos si vas a cualquier modulo B)
    */
    private long lastHitTime = 0;
    private long lastActivationTime = 0;
    private boolean isWDeactivated = false;
    private boolean isDelayOver = false;
    private boolean isSDesactivated = false;
    private boolean flag = false;
    private boolean wasAttackKeyDown = false;
    private int ticksOnGround = 0;
    private int targetTicks = 0;
    private static final Minecraft mc = Minecraft.getMinecraft();
    private Setting durationMs;
    private Setting cooldownMs;
    private Setting delayMs;
    private Setting distanceRange;
    private Setting onlyGround;
    private Setting groundTicks; 
    private final double rayTraceReach = 6.0;
    public STap() {
        super("STap", "Perfect S Tap", Category.COMBAT);
        this.durationMs   = new Setting("Duration", this, 140, 150, 5, 200, true);
        Dark.instance.settingsManager.rSetting(durationMs);
        this.cooldownMs   = new Setting("Cooldown", this, 290, 300, 10, 400, true);
        Dark.instance.settingsManager.rSetting(cooldownMs);
        this.delayMs      = new Setting("Delay", this, 0, 10, 0, 100, true);
        Dark.instance.settingsManager.rSetting(delayMs);
        this.distanceRange = new Setting("Distance", this, 1.5, 3.5, 0, 6, false);
        Dark.instance.settingsManager.rSetting(distanceRange);
        this.onlyGround   = new Setting("Ground Check", this, false);
        Dark.instance.settingsManager.rSetting(onlyGround);
        this.groundTicks  = new Setting("Ground Ticks", this, 1, 2, 0, 10, true);
        Dark.instance.settingsManager.rSetting(groundTicks);
    }
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!Util.nullCheck() || event.phase != Phase.END) return;
        if (mc.thePlayer.onGround) {
            if (ticksOnGround == 0) {
                targetTicks = (int) Util.getRandomMultiplier(groundTicks.getValDouble(), groundTicks.getValDouble2());
            }
            ticksOnGround++;
        } else {
            ticksOnGround = 0; 
        }
        boolean isSprinting = mc.thePlayer.isSprinting();
        boolean isAttackKeyDown = mc.gameSettings.keyBindAttack.isKeyDown();
        boolean isOnGround = mc.thePlayer.onGround;
        boolean OnlyGroundOpt = onlyGround.getValBoolean();
        boolean passesGroundCheck = !OnlyGroundOpt || (isOnGround && ticksOnGround >= targetTicks);
        if (isAttackKeyDown && !wasAttackKeyDown) {
            flag = true;
        }
        wasAttackKeyDown = isAttackKeyDown; 
        if (flag) {
            Entity target = Util.getPointedEntityRayTrace(rayTraceReach);
            if (isSprinting && mc.gameSettings.keyBindForward.isKeyDown() && !mc.gameSettings.keyBindBack.isKeyDown() && target != null && passesGroundCheck) {
                double distance = Util.getRayTraceDistance(target, 6.0);
                if (distance >= distanceRange.getValDouble() && distance <= distanceRange.getValDouble2()
                        && System.currentTimeMillis() - lastActivationTime >= Util.getRandomMultiplier(cooldownMs.getValDouble(), cooldownMs.getValDouble2())) {
                    lastHitTime = System.currentTimeMillis();
                    isDelayOver = false;
                }
            }
            flag = false;
        }
        if (!isDelayOver && System.currentTimeMillis() - lastHitTime >= Util.getRandomMultiplier(delayMs.getValDouble(), delayMs.getValDouble2())) {
            isWDeactivated = true;
            isSDesactivated = true;
            lastActivationTime = System.currentTimeMillis();
            isDelayOver = true;
        }
        if (isWDeactivated && System.currentTimeMillis() - lastActivationTime >= Util.getRandomMultiplier(durationMs.getValDouble(), durationMs.getValDouble2())) {
            isWDeactivated = false;
            isSDesactivated = false;
        }
        if (isWDeactivated && passesGroundCheck) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), true);
        } else {
            if (!(mc.currentScreen instanceof GuiScreen)) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()));
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode()));
            } else {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindBack.getKeyCode(), false);
            }
        }
    }
}
