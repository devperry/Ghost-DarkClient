package me.client.module.render;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import me.client.settings.Setting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
public class Effects extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public Effects() {
        super("Effects", "", Category.RENDER);
    }
    @SubscribeEvent
    public void onRender(TickEvent.RenderTickEvent event) {
        if (!Util.nullCheck() || event.phase != Phase.END) {
            return;
        }
        int hudColor = 0xFFFFFF; 
        try {
            int r = (int) Dark.instance.settingsManager.getSettingByName(Dark.instance.moduleManager.getModule("HUD"), "R").getValDouble();
            int g = (int) Dark.instance.settingsManager.getSettingByName(Dark.instance.moduleManager.getModule("HUD"), "G").getValDouble();
            int b = (int) Dark.instance.settingsManager.getSettingByName(Dark.instance.moduleManager.getModule("HUD"), "B").getValDouble();
            hudColor = (r << 16) | (g << 8) | b;
        } catch (NullPointerException e) {
        }
        ScaledResolution sr = new ScaledResolution(mc);
        int xPos = (int) (sr.getScaledWidth() * 0.02);
        int yPos = (int) (sr.getScaledHeight() * 0.9);
        int guiScale = mc.gameSettings.guiScale;
        if (guiScale == 0) {
            guiScale = 1;
        }
        yPos = yPos / guiScale;
        for (PotionEffect effect : mc.thePlayer.getActivePotionEffects()) {
            String effectName = effect.getEffectName();
            if (effectName.startsWith("potion.")) {
                effectName = effectName.substring(7);
            }
            int duration = effect.getDuration();
            String durationString = String.format("%d:%02d", duration / 1200, (duration / 20) % 60);
            mc.fontRendererObj.drawStringWithShadow(effectName, xPos, yPos, hudColor);
            mc.fontRendererObj.drawStringWithShadow(durationString, xPos, yPos + 10, hudColor);
            yPos -= (int) (sr.getScaledHeight() * 0.09);
        }
    }
}
