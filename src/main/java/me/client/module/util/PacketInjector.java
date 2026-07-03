package me.client.module.util;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import me.client.events.PacketEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
public class PacketInjector {
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (Minecraft.getMinecraft().getNetHandler() != null) {
            NetworkManager nm = Minecraft.getMinecraft().getNetHandler().getNetworkManager();
            if (nm != null && nm.channel() != null && nm.channel().isOpen()) {
                if (nm.channel().pipeline().get("GhostDarkPacketFilter") == null) {
                    inject(nm);
                }
            }
        }
    }
    private void inject(NetworkManager networkManager) {
        Channel channel = networkManager.channel();
        if (channel == null) return;
        try {
            channel.pipeline().addBefore("packet_handler", "GhostDarkPacketFilter", new ChannelDuplexHandler() {
                @Override
                public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                    if (msg instanceof Packet) {
                        PacketEvent.Receive event = new PacketEvent.Receive((Packet<?>) msg);
                        MinecraftForge.EVENT_BUS.post(event);
                        if (event.isCanceled()) {
                            return; 
                        }
                        msg = event.getPacket(); 
                    }
                    super.channelRead(ctx, msg);
                }
                @Override
                public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                    if (msg instanceof Packet) {
                        PacketEvent.Send event = new PacketEvent.Send((Packet<?>) msg);
                        MinecraftForge.EVENT_BUS.post(event);
                        if (event.isCanceled()) {
                            return; 
                        }
                        msg = event.getPacket(); 
                    }
                    super.write(ctx, msg, promise);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}