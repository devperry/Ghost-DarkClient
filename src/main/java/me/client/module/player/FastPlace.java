package me.client.module.player; 
import me.client.Dark;
import net.minecraft.item.ItemSword; 
import org.lwjgl.input.Mouse;
import net.minecraft.client.Minecraft;
import io.netty.util.internal.ThreadLocalRandom; 
import me.client.module.util.Util;
import me.client.module.Category;
import me.client.module.Module;
import me.client.settings.Setting;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.item.ItemBlock;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraft.client.*; 
import java.util.*; 
import java.lang.reflect.*; 
import net.minecraft.client.gui.*; 
import net.minecraftforge.fml.common.gameevent.*; 
import net.minecraft.client.settings.*; 
import net.minecraft.client.gui.inventory.*; 
import org.lwjgl.input.*; 
import net.minecraft.util.*; 
import net.minecraft.entity.*; 
import net.minecraft.world.*; 
import net.minecraft.block.material.*; 
import net.minecraftforge.client.event.*; 
import net.minecraftforge.common.*; 
import net.minecraftforge.fml.common.eventhandler.*; 
import java.nio.*; 
public class FastPlace extends Module
{
    public static boolean isRunning;
    public static Minecraft mc;
    private long nlu; 
    private long nld; 
    private long nd; 
    private long ne; 
    private double drr; 
    private int Max;
    private int Min;
    private boolean dr; 
    public Random r;
    private static Field bst; 
    private static Field fff; 
    private static Field bff; 
    public FastPlace() {
        super("FastPlace", "", Category.PLAYER);
        this.r = new Random();
        Dark.instance.settingsManager.rSetting(new Setting("Min", this, 8.0, 0.0, 20.0, true)); 
        Dark.instance.settingsManager.rSetting(new Setting("Max", this, 12.0, 0.0, 40.0, true)); 
        Dark.instance.settingsManager.rSetting(new Setting("OnlyBlocks", this, true)); 
        Dark.instance.settingsManager.rSetting(new Setting("CPS Boost Chance", this, 30.0, 0.0, 100.0, true)); 
        Dark.instance.settingsManager.rSetting(new Setting("CPS Boost Amount", this, 1.0, 0.0, 3.0, true)); 
        Dark.instance.settingsManager.rSetting(new Setting("Click Delay Variance", this, 4.0, 0.0, 15.0, true)); 
        Dark.instance.settingsManager.rSetting(new Setting("Release Delay Variance", this, 6.0, 0.0, 15.0, true)); 
    }
    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent e) {
        if (!Util.nullCheck()) return;
        if(mc.thePlayer.getCurrentEquippedItem() == null) return;
        boolean OnlyBlocks = Dark.instance.settingsManager.getSettingByName(this, "OnlyBlocks").getValBoolean();
        Min = (int) Dark.instance.settingsManager.getSettingByName(this, "Min").getValDouble();
        Max = (int) Dark.instance.settingsManager.getSettingByName(this, "Max").getValDouble();
        if(OnlyBlocks && !(mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBlock)) return;
        Mouse.poll(); 
        if (Mouse.isButtonDown(1)) {
            if (this.nld > 0L && this.nlu > 0L) {
                if (System.currentTimeMillis() > this.nld) {
                    final int useKeyBind = FastPlace.mc.gameSettings.keyBindUseItem.getKeyCode();
                    KeyBinding.setKeyBindState(useKeyBind, true);
                    KeyBinding.onTick(useKeyBind); 
                    s(1, true);
                    this.vcx(); 
                    FastPlace.isRunning = true;
                }
                else if (System.currentTimeMillis() > this.nlu) {
                    KeyBinding.setKeyBindState(FastPlace.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                    s(1, false);
                    FastPlace.isRunning = false;
                }
            }
            else {
                this.vcx();
            }
        }
        else {
            this.nlu = 0L;
            this.nld = 0L;
        }
    }
    private void vcx() {
        final double mcc = Min;
        final double mca = Max + 1; 
        if (mcc > mca) { 
            return;
        }
        double CPS = mcc + this.r.nextDouble() * (mca - mcc);
        double boostChance = Dark.instance.settingsManager.getSettingByName(this, "CPS Boost Chance").getValDouble();
        double boostAmount = Dark.instance.settingsManager.getSettingByName(this, "CPS Boost Amount").getValDouble();
        if(this.r.nextInt(100) <= boostChance){
            CPS += this.r.nextDouble() * boostAmount;
        }
        long delay = (long)Math.round(1000.0 / CPS);
        if (System.currentTimeMillis() > this.nd) {
            if (!this.dr && this.r.nextInt(100) >= 55) {
                this.dr = true;
                this.drr = 1.1 + this.r.nextDouble() * 0.15;
            }
            else {
                this.dr = false;
            }
            this.nd = System.currentTimeMillis() + 400L + this.r.nextInt(1500);
        }
        if (this.dr) {
            delay *= (long)this.drr;
        }
        if (System.currentTimeMillis() > this.ne) {
            if (this.r.nextInt(100) >= 50) {
                delay += 20L + this.r.nextInt(135);
            }
            this.ne = System.currentTimeMillis() + 400L + this.r.nextInt(1500);
        }
        double clickVariance = Dark.instance.settingsManager.getSettingByName(this, "Click Delay Variance").getValDouble();
        double releaseVariance = Dark.instance.settingsManager.getSettingByName(this, "Release Delay Variance").getValDouble();
        this.nld = System.currentTimeMillis() + delay + (long)(this.r.nextDouble() * clickVariance);
        this.nlu = System.currentTimeMillis() + delay / 2L - (long)(this.r.nextDouble() * releaseVariance);
        if (nlu < System.currentTimeMillis() + 10) { 
             nlu = System.currentTimeMillis() + 10;
        }
        if (nlu >= nld) {
             nlu = nld - 1; 
             if (nlu < System.currentTimeMillis() + 1) nlu = System.currentTimeMillis() + 1; 
        }
    }
    public static void s(final int button, final boolean state) {
        final MouseEvent m = new MouseEvent();
        FastPlace.fff.setAccessible(true);
        try {
            FastPlace.fff.set(m, button);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        FastPlace.fff.setAccessible(false);
        FastPlace.bst.setAccessible(true);
        try {
            FastPlace.bst.set(m, state);
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        FastPlace.bst.setAccessible(false);
        MinecraftForge.EVENT_BUS.post((Event)m);
        try {
            FastPlace.bff.setAccessible(true);
            final ByteBuffer buffer = (ByteBuffer)FastPlace.bff.get(null); 
            FastPlace.bff.setAccessible(false);
            buffer.put(button, (byte)(state ? 1 : 0)); 
        }
        catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
    static {
        FastPlace.isRunning = false;
        FastPlace.mc = Minecraft.getMinecraft();
        try {
            FastPlace.fff = MouseEvent.class.getDeclaredField("button");
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            FastPlace.bst = MouseEvent.class.getDeclaredField("buttonstate");
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        try {
            FastPlace.bff = Mouse.class.getDeclaredField("buttons");
        }
        catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }
}