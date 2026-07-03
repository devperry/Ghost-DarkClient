package me.client.module.misc;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
public class Debug extends Module {
    public Debug() {
        super("Debug", "Muestra información de depuración del jugador", Category.MISC);
        Dark.instance.settingsManager.rSetting(new Setting("HurtTime", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("HurtResistantTime", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("Position", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("Motion", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("OnGround", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("FallDistance", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("Rotation", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("Sprinting", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("Sneaking", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("TicksExisted", this, false));
    }
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
       if (!Util.nullCheck()) {
            return;
        }
        boolean hurtTimeDebug = Dark.instance.settingsManager.getSettingByName(this, "HurtTime").getValBoolean();
        boolean hurtResistantTimeDebug = Dark.instance.settingsManager.getSettingByName(this, "HurtResistantTime").getValBoolean();
        boolean positionDebug = Dark.instance.settingsManager.getSettingByName(this, "Position").getValBoolean();
        boolean motionDebug = Dark.instance.settingsManager.getSettingByName(this, "Motion").getValBoolean();
        boolean onGroundDebug = Dark.instance.settingsManager.getSettingByName(this, "OnGround").getValBoolean();
        boolean fallDistanceDebug = Dark.instance.settingsManager.getSettingByName(this, "FallDistance").getValBoolean();
        boolean rotationDebug = Dark.instance.settingsManager.getSettingByName(this, "Rotation").getValBoolean();
        boolean sprintingDebug = Dark.instance.settingsManager.getSettingByName(this, "Sprinting").getValBoolean();
        boolean sneakingDebug = Dark.instance.settingsManager.getSettingByName(this, "Sneaking").getValBoolean();
        boolean ticksExistedDebug = Dark.instance.settingsManager.getSettingByName(this, "TicksExisted").getValBoolean();
        if (hurtTimeDebug && mc.thePlayer.hurtTime > 0) {
            Util.SmsToChat("HurtTime: " + mc.thePlayer.hurtTime);
        }
        if (hurtResistantTimeDebug && mc.thePlayer.hurtResistantTime > 0) {
            Util.SmsToChat("HurtResistantTime: " + mc.thePlayer.hurtResistantTime);
        }
        if (positionDebug) {
            String pos = String.format("Position -> X: %.2f, Y: %.2f, Z: %.2f", mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            Util.SmsToChat(pos);
        }
        if (motionDebug) {
            String motion = String.format("Motion -> X: %.4f, Y: %.4f, Z: %.4f", mc.thePlayer.motionX, mc.thePlayer.motionY, mc.thePlayer.motionZ);
            Util.SmsToChat(motion);
        }
        if (onGroundDebug) {
            Util.SmsToChat("OnGround: " + mc.thePlayer.onGround);
        }
        if (fallDistanceDebug && mc.thePlayer.fallDistance > 0) {
            Util.SmsToChat("FallDistance: " + mc.thePlayer.fallDistance);
        }
        if (rotationDebug) {
            String rotation = String.format("Rotation -> Yaw: %.2f, Pitch: %.2f", mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch);
            Util.SmsToChat(rotation);
        }
        if (sprintingDebug) {
            Util.SmsToChat("IsSprinting: " + mc.thePlayer.isSprinting());
        }
        if (sneakingDebug) {
            Util.SmsToChat("IsSneaking: " + mc.thePlayer.isSneaking());
        }
        if (ticksExistedDebug) {
            Util.SmsToChat("TicksExisted: " + mc.thePlayer.ticksExisted);
        }
    }
}