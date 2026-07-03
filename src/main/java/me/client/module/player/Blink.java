package me.client.module.player;
import me.client.Dark;
import me.client.events.PacketEvent;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import me.client.settings.Setting;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.network.Packet;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.login.client.C01PacketEncryptionResponse;
import net.minecraft.network.play.client.C00PacketKeepAlive;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C0FPacketConfirmTransaction;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.network.status.client.C00PacketServerQuery;
import net.minecraft.network.status.client.C01PacketPing;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import java.util.concurrent.ConcurrentLinkedQueue;
public class Blink extends Module {    
    private final ConcurrentLinkedQueue<Packet<?>> blinkedPackets = new ConcurrentLinkedQueue<>();
    private Vec3 pos;
    private long enableTime; 
    private Setting disableOnVelocity;
    private Setting disableOnAttack;
    private Setting disableTimeout; 
    public Blink() {
        super("Blink", "", Category.PLAYER);        
        disableOnVelocity = new Setting("Disable on velocity", this, true);
        disableOnAttack = new Setting("Disable on attack", this, true);
        disableTimeout = new Setting("Disable Timeout", this, 0.0, 0.0, 10.0, false);
        Dark.instance.settingsManager.rSetting(disableOnVelocity);
        Dark.instance.settingsManager.rSetting(disableOnAttack);
        Dark.instance.settingsManager.rSetting(disableTimeout); 
    }
    @Override
    public void onEnable() {
        super.onEnable(); 
        if (mc.thePlayer == null) {
            this.setToggled(false);
            return;
        }
        blinkedPackets.clear();
        pos = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        enableTime = System.currentTimeMillis(); 
    }
    @Override
    public void onDisable() {
        super.onDisable(); 
        if (mc.theWorld != null && mc.thePlayer != null) {
            synchronized (blinkedPackets) {
                for (Packet<?> packet : blinkedPackets) {            
                    mc.getNetHandler().getNetworkManager().sendPacket(packet);
                }
            }
        }
        blinkedPackets.clear();
        pos = null;
    }
    @SubscribeEvent
    public void onSendPacket(PacketEvent.Send event) {
        if (!Util.nullCheck()) {
            this.setToggled(false); 
            return;
        }
        Packet<?> packet = event.getPacket();
        if (packet instanceof C00Handshake || 
            packet instanceof C00PacketLoginStart || 
            packet instanceof C00PacketServerQuery || 
            packet instanceof C01PacketPing || 
            packet instanceof C01PacketEncryptionResponse || 
            packet instanceof C00PacketKeepAlive || 
            packet instanceof C0FPacketConfirmTransaction) {
            return;
        }
        if (disableOnAttack.getValBoolean() && packet instanceof C02PacketUseEntity) {
            C02PacketUseEntity attackPacket = (C02PacketUseEntity) packet;
            if (attackPacket.getAction() == C02PacketUseEntity.Action.ATTACK) {
                this.setToggled(false); 
                return; 
            }
        }
        blinkedPackets.add(packet);
        event.setCanceled(true);
    }
    @SubscribeEvent
    public void onReceivePacket(PacketEvent.Receive event) {
        if (!Util.nullCheck() || !this.isToggled()) return;
        Packet<?> packet = event.getPacket();
        if (disableOnVelocity.getValBoolean()) {
            if (packet instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity velocityPacket = (S12PacketEntityVelocity) packet;
                if (velocityPacket.getEntityID() == mc.thePlayer.getEntityId()) {
                    this.setToggled(false);
                }
            } 
            else if (packet instanceof S27PacketExplosion) {
                this.setToggled(false);
            }
        }
    }
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!Util.nullCheck() || pos == null) {
            return;
        }
        double timeoutSeconds = disableTimeout.getValDouble();
        if (timeoutSeconds > 0.0) {
            long timePassed = System.currentTimeMillis() - enableTime;
            if (timePassed >= (long) (timeoutSeconds * 1000)) {
                this.setToggled(false);
                return; 
            }
        }
        drawBox(pos);
    }
    private void drawBox(Vec3 pos) {
        GlStateManager.pushMatrix();
        double renderX = mc.getRenderManager().viewerPosX;
        double renderY = mc.getRenderManager().viewerPosY;
        double renderZ = mc.getRenderManager().viewerPosZ;
        double x = pos.xCoord - renderX;
        double y = pos.yCoord - renderY;
        double z = pos.zCoord - renderZ;
        AxisAlignedBB bbox = mc.thePlayer.getEntityBoundingBox();
        AxisAlignedBB axis = new AxisAlignedBB(
                bbox.minX - mc.thePlayer.posX + x, 
                bbox.minY - mc.thePlayer.posY + y, 
                bbox.minZ - mc.thePlayer.posZ + z, 
                bbox.maxX - mc.thePlayer.posX + x, 
                bbox.maxY - mc.thePlayer.posY + y, 
                bbox.maxZ - mc.thePlayer.posZ + z
        );
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glLineWidth(2.0F);
        float r = 0.0f;
        float g = 1.0f;
        float b = 0.0f;
        float a = 1.0f;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldRenderer = tessellator.getWorldRenderer();
        worldRenderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        worldRenderer.pos(axis.minX, axis.minY, axis.minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.maxX, axis.minY, axis.minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.maxX, axis.minY, axis.minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.maxX, axis.minY, axis.maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.maxX, axis.minY, axis.maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.minX, axis.minY, axis.maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.minX, axis.minY, axis.maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.minX, axis.minY, axis.minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.minX, axis.maxY, axis.minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.maxX, axis.maxY, axis.minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.maxX, axis.maxY, axis.minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.maxX, axis.maxY, axis.maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.maxX, axis.maxY, axis.maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.minX, axis.maxY, axis.maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.minX, axis.maxY, axis.maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.minX, axis.maxY, axis.minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.minX, axis.minY, axis.minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.minX, axis.maxY, axis.minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.maxX, axis.minY, axis.minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.maxX, axis.maxY, axis.minZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.maxX, axis.minY, axis.maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.maxX, axis.maxY, axis.maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.minX, axis.minY, axis.maxZ).color(r, g, b, a).endVertex();
        worldRenderer.pos(axis.minX, axis.maxY, axis.maxZ).color(r, g, b, a).endVertex();
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_BLEND);
        GlStateManager.popMatrix();
    }
}
