package me.client.module.combat;
import me.client.Dark;
import me.client.events.PacketEvent;
import me.client.module.Category;
import me.client.module.Module;
import me.client.settings.Setting;
import me.client.module.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import me.client.module.util.utilities.TimerUtil;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import me.client.module.misc.ReceiveHits;
import org.lwjgl.input.Mouse;
public class HitSelect extends Module {  
    private Setting ht;
    private Setting ms;
    private Setting pr;
    private final TimerUtil clickTimer = new TimerUtil();
    private double nextDelay = 0;
    private ReceiveHits receiveHits;
    public HitSelect() {
        super("HitSelect", "Realiza un burst de hits al recibir daño.", Category.COMBAT);
        this.ht = new Setting("HurtTime", this, 0 , 10, 0, 10, true);
        this.ms = new Setting("Ms", this, 0 , 100, 0, 500, true); 
        this.pr = new Setting("OnPacket", this,true);
        Dark.instance.settingsManager.rSetting(ht);
        Dark.instance.settingsManager.rSetting(ms);
        Dark.instance.settingsManager.rSetting(pr);
    }
        @SubscribeEvent
    public void onReceive(PacketEvent.Receive e) {
    if(!pr.getValBoolean()) return;
        Packet packet = e.getPacket();
        if (packet instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity wrapper = (S12PacketEntityVelocity) packet;
            if (wrapper.getEntityID() == mc.thePlayer.getEntityId()) {
                Util.clickAttack();
            }
        }
    }
    @SubscribeEvent
    public void onClientTick(TickEvent.RenderTickEvent event) {
        if (event.phase != Phase.END) return;    
        if (!Util.nullCheck()) {
            return;
        }
        if (receiveHits == null) receiveHits = (ReceiveHits) Dark.instance.moduleManager.getModule("ReceiveHits");
        if (receiveHits != null && !(Dark.instance.moduleManager.getModule("ReceiveHits").isToggled()) && (Mouse.isButtonDown(0))) return;           
        if ((mc.thePlayer.hurtTime >= ht.getValDouble()) && (mc.thePlayer.hurtTime <= ht.getValDouble2())) {
            if (clickTimer.hasCooldownExpired()) { 
                long minDelay = (long) ms.getValDouble();
                long maxDelay = (long) ms.getValDouble2();
                nextDelay = Util.getRandomMultiplier(minDelay, maxDelay);
                Util.clickAttack();
                clickTimer.setDelay((long)nextDelay);
                clickTimer.reset();
            }
        }
    }
}