package me.client.module.combat;
import me.client.Dark;
import me.client.events.PacketEvent;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import me.client.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.*;
import net.minecraft.network.status.server.*;
import net.minecraft.network.handshake.client.*;
import net.minecraft.network.status.client.*;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
public class BackTrack extends Module {
    private final ConcurrentLinkedQueue<DelayedPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private final Random random = new Random();
    private volatile EntityPlayer target = null;
    private volatile long lastAttackTime = 0L;
    private long currentDelay   = 0L;
    private long cooldownEndTime = 0L;
    private Setting delaySetting;
    private Setting rangeSetting;
    private Setting releaseOnHitSetting;
    private Setting stopOnVelocitySetting;
    private Setting cooldownSetting;
    public BackTrack() {
        super("BackTrack", "", Category.COMBAT);
        delaySetting          = new Setting("Delay (ms)",          this, 200, 400, 0,    1000, true);
        rangeSetting          = new Setting("Target Range",         this, 3.0, 6.0, 0.0, 10.0, false);
        releaseOnHitSetting   = new Setting("Release On Hit",       this, true);
        stopOnVelocitySetting = new Setting("Stop On Get Velocity", this, true);
        cooldownSetting       = new Setting("Cooldown (ms)",        this, 0,   1000, 0,  5000, true);
        Dark.instance.settingsManager.rSetting(delaySetting);
        Dark.instance.settingsManager.rSetting(rangeSetting);
        Dark.instance.settingsManager.rSetting(releaseOnHitSetting);
        Dark.instance.settingsManager.rSetting(stopOnVelocitySetting);
        Dark.instance.settingsManager.rSetting(cooldownSetting);
    }
    @Override
    public void onEnable() {
        super.onEnable();
        flushPackets();
        target          = null;
        currentDelay    = getRandomDelay();
        cooldownEndTime = 0L;
    }
    @Override
    public void onDisable() {
        super.onDisable();
        flushPackets();
        target = null;
    }
    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        if (!Util.nullCheck() || !this.isToggled()) return;
        if (!(event.target instanceof EntityPlayer)) return;
        EntityPlayer victim = (EntityPlayer) event.target;
        double dist = mc.thePlayer.getDistanceToEntity(victim);
        double min  = rangeSetting.getValDouble();
        double max  = rangeSetting.getValDouble2();
        if (dist < min || dist > max) return;
        target         = victim;
        lastAttackTime = System.currentTimeMillis();
        if (releaseOnHitSetting.getValBoolean()) {
            flushPackets(); 
        }
    }
    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (!Util.nullCheck() || !this.isToggled()) return;
        Packet<?> packet = event.getPacket();
        if (packet instanceof S00PacketKeepAlive       
         || packet instanceof S08PacketPlayerPosLook   
         || packet instanceof S40PacketDisconnect      
         || packet instanceof S01PacketJoinGame        
         || packet instanceof S07PacketRespawn         
         || packet instanceof S2BPacketChangeGameState 
         || packet instanceof S19PacketEntityStatus    
         || packet instanceof S3FPacketCustomPayload   
         || packet instanceof S45PacketTitle || packet instanceof S01PacketPong ) {        
            return;
        }
        if (packet instanceof S12PacketEntityVelocity) {
            S12PacketEntityVelocity vel = (S12PacketEntityVelocity) packet;
            if (mc.thePlayer != null && vel.getEntityID() == mc.thePlayer.getEntityId()) {
                if (stopOnVelocitySetting.getValBoolean()) {
                    mc.addScheduledTask(() -> {
                        flushPackets();
                        target = null;
                    });
                    return; 
                }
            }
        }
        if (!shouldBacktrack()) return;
        event.setCanceled(true);
        packetQueue.add(new DelayedPacket(packet, System.currentTimeMillis()));
    }
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !Util.nullCheck()) return;
        if (!shouldBacktrack()) {
            if (!packetQueue.isEmpty()) {
                cooldownEndTime = System.currentTimeMillis()
                        + (long) cooldownSetting.getValDouble();
            }
            flushPackets();
            target = null;
            return;
        }
        while (!packetQueue.isEmpty()) {
            DelayedPacket dp = packetQueue.peek();
            if (System.currentTimeMillis() - dp.time >= currentDelay) {
                packetQueue.poll();
                processPacket(dp.packet);
            } else {
                break;
            }
        }
        if (packetQueue.isEmpty()) {
            currentDelay = getRandomDelay();
        }
    }
    private boolean shouldBacktrack() {
        if (!Util.nullCheck()) return false;
        if (System.currentTimeMillis() < cooldownEndTime) return false;
        EntityPlayer t = target;
        if (t == null || System.currentTimeMillis() - lastAttackTime >= 2000) return false;
        if (t.isDead || t.getHealth() <= 0) return false;
        double dist = mc.thePlayer.getDistanceToEntity(t);
        double min  = rangeSetting.getValDouble();
        double max  = rangeSetting.getValDouble2();
        return dist >= min && dist <= max;
    }
    private long getRandomDelay() {
        double min = delaySetting.getValDouble();
        double max = delaySetting.getValDouble2();
        if (max <= min) return (long) min;
        return (long) (min + random.nextDouble() * (max - min));
    }
    @SuppressWarnings("unchecked")
    private void processPacket(Packet<?> packet) {
        try {
            if (mc.getNetHandler() != null) {
                ((Packet<INetHandlerPlayClient>) packet).processPacket(mc.getNetHandler());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void flushPackets() {
        while (!packetQueue.isEmpty()) {
            DelayedPacket dp = packetQueue.poll();
            processPacket(dp.packet);
        }
    }
    private static class DelayedPacket {
        public final Packet<?> packet;
        public final long      time;
        public DelayedPacket(Packet<?> packet, long time) {
            this.packet = packet;
            this.time   = time;
        }
    }
}
