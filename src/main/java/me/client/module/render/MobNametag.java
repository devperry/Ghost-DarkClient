package me.client.module.render;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11; 
public class MobNametag extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public MobNametag() {
        super("MobNametag", "Renderiza nametags personalizados sobre mobs con nombres personalizados.", Category.RENDER);
    }
    @SubscribeEvent
    public void onRenderLiving(RenderLivingEvent.Specials.Pre event) {
        if (!Util.nullCheck() || Dark.instance.destructed || mc.getRenderManager() == null || mc.fontRendererObj == null) {
            return;
        }
        if (event.entity instanceof EntityLivingBase && event.entity != mc.thePlayer && event.entity.deathTime == 0 && event.entity.hasCustomName()) {
            EntityLivingBase entity = (EntityLivingBase) event.entity; 
            String name = entity.getDisplayName().getFormattedText(); 
            event.setCanceled(true);
            RenderManager renderManager = mc.getRenderManager();
            FontRenderer fontRenderer = mc.fontRendererObj;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) event.x, (float) event.y + entity.height + 0.5F, (float) event.z);
            GlStateManager.rotate(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
            float scale = 0.02666667F;
            GlStateManager.scale(-scale, -scale, scale);
            if (entity.isSneaking()) {
                GlStateManager.translate(0.0F, 9.374999F, 0.0F);
            }
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.disableDepth();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            fontRenderer.drawStringWithShadow(name, -fontRenderer.getStringWidth(name) / 2, 0, 0xFFFFFFFF); 
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); 
            GlStateManager.popMatrix();
        }
    }
}