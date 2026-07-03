package me.client.module.combat;
import me.client.Dark;
import me.client.settings.Setting;
import me.client.module.Category;
import me.client.module.util.Util;
import me.client.module.Module;
import org.lwjgl.input.Keyboard;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.client.settings.KeyBinding;
import java.util.Random;
public class MidHitSelect extends Module {
    private Random random = new Random();
    private final Minecraft mc = Minecraft.getMinecraft();
    private long lastAttackTime = 0;
    public boolean currentShouldAttack = false;
    private long delay;
    private double chance;
    private long configStartTime = 0;
    private boolean configDelayExpired = false;
    private boolean atrun;
    public boolean run;
    public MidHitSelect() {
        super("MidHitSelect", "", Category.COMBAT);
        Dark.instance.settingsManager.rSetting(new Setting("Chance", this, 0.8, 0.1, 1.0, false));
        Dark.instance.settingsManager.rSetting(new Setting("Delay", this, 420, 50, 500, true));
    }
    @Override
    public void onEnable() {
        super.onEnable();
        resetConfigDelay();
    }
    @Override
    public void onDisable() {
        super.onDisable();
        configDelayExpired = true;
    }
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (!Util.nullCheck()) return;
        chance = Dark.instance.settingsManager.getSettingByName(this, "Chance").getValDouble();
        delay = (long) Dark.instance.settingsManager.getSettingByName(this, "Delay").getValDouble();
        updateAttackConditions();
        if(random.nextDouble() >= chance){
        currentShouldAttack = true;        
        }
        else{
        if(!currentShouldAttack){
            currentShouldAttack = System.currentTimeMillis() - configStartTime >= delay;
        }
        if(currentShouldAttack){        
          resetConfigDelay();                    
        }
        }
    }
    private void updateAttackConditions() {
        currentShouldAttack = mc.thePlayer.movementInput.moveForward < 0.0F || (mc.thePlayer.hurtTime > 0 && !mc.thePlayer.onGround && mc.thePlayer.moveForward != 0);
    }
    private void resetConfigDelay() {
        configStartTime = System.currentTimeMillis();
        configDelayExpired = false;
    }
}