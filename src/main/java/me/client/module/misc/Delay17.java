package me.client.module.misc;
import me.client.Dark;
import me.client.settings.Setting;
import java.lang.reflect.Field;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import me.client.module.util.utilities.StringRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
public class Delay17 extends Module {
    private Field leftClickCounter;
    private Minecraft mc;
    public Delay17() {
        super(StringRegistry.register(new String(new char[] {'D', 'e', 'l', 'a', 'y', ' ', 'F', 'i', 'x'})), "quita el delay de la 1.8", Category.MISC);
        this.mc = Minecraft.getMinecraft();
        Dark.instance.settingsManager.rSetting(new Setting("Activation Distance", this, 3.4, 1.0, 6.0, false));
        Dark.instance.settingsManager.rSetting(new Setting("Distance Based", this, false));
        try {
            this.leftClickCounter = Minecraft.class.getDeclaredField("field_71429_W");
        } catch (Exception var4) {
            try {
                this.leftClickCounter = Minecraft.class.getDeclaredField("leftClickCounter");
            } catch (Exception ex) {
                ex.printStackTrace(); 
            }
        }
        if (this.leftClickCounter != null) {
            this.leftClickCounter.setAccessible(true);
        }
    }
    @SubscribeEvent
public void onPlayerTick(final TickEvent.PlayerTickEvent e) {
    if (e.phase != TickEvent.Phase.START) return; 
    if (Util.nullCheck()) {
        boolean isDistEnabled = Dark.instance.settingsManager.getSettingByName(this, "Distance Based").getValBoolean();
        boolean shouldReset = true;
        if (isDistEnabled) {
            float dist = (float) Dark.instance.settingsManager.getSettingByName(this, "Activation Distance").getValDouble();
            Entity target = Util.getPointedEntityRayTrace(dist);
            shouldReset = (target != null);
        }
        if (shouldReset) {
            try {
                this.leftClickCounter.set(this.mc, 0);
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
            }
        }
    }
}
}
