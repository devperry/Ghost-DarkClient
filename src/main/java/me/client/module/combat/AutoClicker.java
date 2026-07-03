package me.client.module.combat;
import me.client.Dark;
import me.client.module.misc.ReceiveHits;
import net.minecraft.item.ItemSword;
import net.minecraft.util.MovingObjectPosition;
import org.lwjgl.input.Mouse;
import net.minecraft.client.Minecraft;
import me.client.module.util.Util;
import me.client.module.Category;
import me.client.module.Module;
import me.client.settings.Setting;
import me.client.module.combat.MidHitSelect;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import java.util.Random;
import java.lang.reflect.Field;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import java.nio.ByteBuffer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
public class AutoClicker extends Module {
    public static Minecraft mc = Minecraft.getMinecraft();
    private long nextClickTime;
    private long nextReleaseTime;
    private boolean mouseHeldDown = false;
    private final Random random = new Random();
    private long lastAttackTime = 0L;
    private boolean isInCombat = false;
    private static Field fff;
    private static Field bst;
    private static Field bff;
    private ReceiveHits receiveHits;
    private MidHitSelect midHitSelectInstance;
    private Setting avarageCps,varianceCps,spikeChance,spikeBoost,dropChance,multiplier,multiplierOnAttack,onlySword, breakBlocks;
    public AutoClicker() {
        super("Autoclicker", ".", Category.COMBAT);
        Dark.instance.settingsManager.rSetting(avarageCps = new Setting("Average CPS", this, 7.0, 12.0, 1.0, 30.0, false));
        Dark.instance.settingsManager.rSetting(varianceCps = new Setting("CPS Variance", this, 2.0, 0.0, 10.0, false));
        Dark.instance.settingsManager.rSetting(spikeChance = new Setting("Spike Chance", this, 20.0, 0.0, 100.0, true));
        Dark.instance.settingsManager.rSetting(spikeBoost = new Setting("Spike CPS Boost", this, 3.0, 0.0, 10.0, false));
        Dark.instance.settingsManager.rSetting(dropChance = new Setting("Drop Chance", this, 15.0, 0.0, 100.0, true));
        Dark.instance.settingsManager.rSetting(multiplier = new Setting("Multiplier", this, 1.3, 1.0, 3.0, false));
        Dark.instance.settingsManager.rSetting(multiplierOnAttack= new Setting("Trade Check", this, true));
        Dark.instance.settingsManager.rSetting(onlySword = new Setting("Only Sword", this, false));
        Dark.instance.settingsManager.rSetting(breakBlocks = new Setting("Break Blocks", this, false));
    }
    @Override
    public void onEnable() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    @Override
    public void onDisable() {
        MinecraftForge.EVENT_BUS.unregister(this);
        if (mouseHeldDown) {
            releaseMouse();
        }
    }
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (!Util.nullCheck() || event.phase != Phase.START) {
            return;
        }
        if (receiveHits == null) receiveHits = (ReceiveHits) Dark.instance.moduleManager.getModule("ReceiveHits");
        if (midHitSelectInstance == null) midHitSelectInstance = (MidHitSelect) Dark.instance.moduleManager.getModule("MidHitSelect");
        if (receiveHits != null && Dark.instance.moduleManager.getModule("ReceiveHits").isToggled() && !receiveHits.isHab()) return;
        if (midHitSelectInstance != null && Dark.instance.moduleManager.getModule("MidHitSelect").isToggled() && !midHitSelectInstance.currentShouldAttack) return;
        if (onlySword.getValBoolean() && (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemSword))) {
            return;
        }
        if (breakBlocks.getValBoolean() && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            return; 
        }
        if (mc.thePlayer.isUsingItem()) {
    if (mouseHeldDown) {
        releaseMouse();
    }
    return;
}
        boolean leftClick = Mouse.isButtonDown(0);
    boolean aimingAtEntity = mc.objectMouseOver != null &&
                             mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY;
    if (!leftClick || !aimingAtEntity || mc.thePlayer.hurtTime <= 0) {
        isInCombat = false;
    } else {
        isInCombat = true;
    }
        if (Mouse.isButtonDown(0)) {
            long currentTime = System.currentTimeMillis();
            if (mouseHeldDown && currentTime >= nextReleaseTime) {
                releaseMouse();
            } else if (!mouseHeldDown && currentTime >= nextClickTime) {
                pressMouse();
                calculateNextClickTimings();
            }
        } else {
            if (mouseHeldDown) {
                releaseMouse();
            }
            nextClickTime = System.currentTimeMillis();
        }
    }
    private void calculateNextClickTimings() {
        double averageCPS = Util.getRandomMultiplier(avarageCps.getValDouble(),avarageCps.getValDouble2());        
        boolean attackMultiplier = this.multiplierOnAttack.getValBoolean();
        double currentMultiplier = this.multiplier.getValDouble();
        double cps = averageCPS + (random.nextGaussian() * varianceCps.getValDouble());
        if (random.nextDouble() * 100.0 < spikeChance.getValDouble()) {
            cps += spikeBoost.getValDouble() + (random.nextDouble() * varianceCps.getValDouble());
        } else if ((random.nextDouble() * 100.0 < dropChance.getValDouble()) && !isInCombat) {
            cps -= (varianceCps.getValDouble() * 1.5) + (random.nextDouble() * 2.0);
        }
        if (attackMultiplier && isInCombat || !attackMultiplier) {
            cps *= currentMultiplier;
        }
        cps = Math.max(1.5, Math.min(60.0, cps));
        long delay = (long) (1000.0 / cps);
        long downTime = (long) (delay * (0.35 + (random.nextGaussian() * 0.1)));
        downTime = Math.max(25, downTime);
        downTime = Math.min(delay - 25, downTime);
        this.nextReleaseTime = System.currentTimeMillis() + downTime;
        this.nextClickTime = System.currentTimeMillis() + delay;
    }
    private void pressMouse() {
        int attackKeyCode = mc.gameSettings.keyBindAttack.getKeyCode();
        KeyBinding.setKeyBindState(attackKeyCode, true);
        KeyBinding.onTick(attackKeyCode);
        setMouseButtonState(0, true);
        mouseHeldDown = true;
    }
    private void releaseMouse() {
        int attackKeyCode = mc.gameSettings.keyBindAttack.getKeyCode();
        KeyBinding.setKeyBindState(attackKeyCode, false);
        setMouseButtonState(0, false);
        mouseHeldDown = false;
    }
    private void setMouseButtonState(int button, boolean state) {
        MouseEvent mouseEvent = new MouseEvent();
        try {
            fff.setAccessible(true);
            fff.set(mouseEvent, button);
            fff.setAccessible(false);
            bst.setAccessible(true);
            bst.set(mouseEvent, state);
            bst.setAccessible(false);
            MinecraftForge.EVENT_BUS.post(mouseEvent);
            bff.setAccessible(true);
            ByteBuffer buffer = (ByteBuffer) bff.get(null);
            bff.setAccessible(false);
            buffer.put(button, (byte) (state ? 1 : 0));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    static {
        try {
            fff = MouseEvent.class.getDeclaredField("button");
            bst = MouseEvent.class.getDeclaredField("buttonstate");
            bff = Mouse.class.getDeclaredField("buttons");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Error crítico: Fallo al inicializar los campos de reflexión para el AutoClicker.", e);
        }
    }
        @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!Util.nullCheck() || event.target == null) {
            isInCombat = false;
            return;
        }
            if (System.currentTimeMillis() - lastAttackTime <= 100 || mc.thePlayer.hurtTime > 0) {
                isInCombat = true;
            }else{
            isInCombat = false;
            }
        lastAttackTime = System.currentTimeMillis();
    }
}