package me.client.module.render;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.gui.Gui;
public class HUD extends Module {
        private Setting r;
        private Setting g;
        private Setting b;
        private Setting hideClones;
    public HUD() {
        super("HUD", "", Category.RENDER);
        this.r = new Setting("R", this, 255, 0, 255, true);
        this.g = new Setting("G", this, 255, 0, 255, true);
        this.b = new Setting("B", this, 255, 0, 255, true);
        this.hideClones = new Setting("Hide Clones", this, true);
        Dark.instance.settingsManager.rSetting(r);
        Dark.instance.settingsManager.rSetting(g);
        Dark.instance.settingsManager.rSetting(b);
        Dark.instance.settingsManager.rSetting(hideClones);
        this.mc = Minecraft.getMinecraft();
    }
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase != Phase.END) return;
        if (mc.thePlayer == null || mc.theWorld == null || Dark.instance.destructed ||
            mc.gameSettings.showDebugInfo || mc.currentScreen != null) return;
        if (!this.isToggled()) return;
        int r = (int) this.r.getValDouble();
        int g = (int) this.g.getValDouble();
        int b = (int) this.b.getValDouble();
        int hudColor = 0xFF000000 | (r << 16) | (g << 8) | b;
        ScaledResolution sr = new ScaledResolution(mc);
        FontRenderer fr = mc.fontRendererObj;
        int y = 2;
        for (Module mod : Dark.instance.moduleManager.getModuleList()) {
            if (mod.isClone && hideClones.getValBoolean()) continue;
            if (!mod.getName().equalsIgnoreCase("HUD") && mod.isToggled() && mod.visible) {
                String moduleName = mod.getName();
                int textWidth = fr.getStringWidth(moduleName);
                int x = sr.getScaledWidth() - textWidth - 2;
                Gui.drawRect(x - 2, y - 1, x + textWidth + 2, y + fr.FONT_HEIGHT, 0x55000000);
                Gui.drawRect(x + textWidth + 2, y - 1, x + textWidth + 3, y + fr.FONT_HEIGHT, 0xAA7C3AED);
                fr.drawStringWithShadow(moduleName, x, y, hudColor);
                y += fr.FONT_HEIGHT + 2;
            }
        }
    }
}
