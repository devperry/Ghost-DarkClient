package me.client.module.render;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import me.client.module.util.Util;
import java.awt.Color;
import java.util.LinkedList;
import java.util.Queue;
public class CpsDisplay extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private final Queue<Long> clicks = new LinkedList<>();
    private boolean wasClicking = false;
    public CpsDisplay() {
        super("CpsDisplay", "Muestra tus Clics Por Segundo", Category.RENDER);
    }
    @SubscribeEvent
    public void onClientTick(TickEvent.RenderTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!Util.nullCheck()) return;
        boolean isClicking = mc.gameSettings.keyBindAttack.isKeyDown();
        if (wasClicking && !isClicking) {
            registerClick();
        }
        wasClicking = isClicking;
    }
    private void registerClick() {
        clicks.add(System.currentTimeMillis());
        cleanOldClicks();
    }
    private void cleanOldClicks() {
        long currentTime = System.currentTimeMillis();
        while (!clicks.isEmpty() && currentTime - clicks.peek() > 1000L) {
            clicks.poll();
        }
    }
    private int getCPS() {
        cleanOldClicks();
        return clicks.size();
    }
    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Post event) {
        if (event.type != RenderGameOverlayEvent.ElementType.TEXT) return;
        if (!Util.nullCheck()) return;
        int hudColor = 0xFFFFFF; 
        try {
            int r = (int) Dark.instance.settingsManager.getSettingByName(Dark.instance.moduleManager.getModule("HUD"), "R").getValDouble();
            int g = (int) Dark.instance.settingsManager.getSettingByName(Dark.instance.moduleManager.getModule("HUD"), "G").getValDouble();
            int b = (int) Dark.instance.settingsManager.getSettingByName(Dark.instance.moduleManager.getModule("HUD"), "B").getValDouble();
            hudColor = (r << 16) | (g << 8) | b;
        } catch (NullPointerException e) {
        }
        ScaledResolution res = new ScaledResolution(mc);
        String text = getCPS() + " CPS";
        int x = res.getScaledWidth() / 2 - mc.fontRendererObj.getStringWidth(text) / 2;
        int y = 5;
        mc.fontRendererObj.drawString(text, x, y, hudColor, true);
    }
}