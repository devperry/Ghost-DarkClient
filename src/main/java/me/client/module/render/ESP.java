package me.client.module.render;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
import me.client.module.util.Util;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.entity.monster.EntityMob;
import org.lwjgl.opengl.GL11;
import java.util.ArrayList;
import java.util.List;
public class ESP extends Module {
    private Minecraft mc = Minecraft.getMinecraft();
    private static final float RED = 1.0f;
    private static final float GREEN = 1.0f;
    private static final float BLUE = 1.0f;
    private static final float ALPHA = 0.6f;
    private Frustum frustum = new Frustum();
    private List<Entity> cachedEntities = new ArrayList<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_UPDATE_DELAY = 100; 
    private double maxRenderDistance = 200.0; 
    private double maxRenderDistanceSq; 
    public ESP() {
        super("ESP", "Classic 3D box Esp", Category.RENDER);
        Dark.instance.settingsManager.rSetting(new Setting("MaxDistance", this, 100, 10, 200, true));
        Dark.instance.settingsManager.rSetting(new Setting("ShowPlayers", this, true));
        Dark.instance.settingsManager.rSetting(new Setting("ShowMobs", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("LineWidth", this, 1.0, 0.5, 3.0, false));
        updateSettings();
    }
    private void updateSettings() {
        maxRenderDistance = Dark.instance.settingsManager.getSettingByName(this, "MaxDistance").getValDouble();
        maxRenderDistanceSq = maxRenderDistance * maxRenderDistance;
    }
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!Util.nullCheck()) return;
        updateSettings();
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheUpdate > CACHE_UPDATE_DELAY) {
            updateEntityCache();
            lastCacheUpdate = currentTime;
        }
        if (cachedEntities.isEmpty()) return;
        double renderPosX = mc.getRenderManager().viewerPosX;
        double renderPosY = mc.getRenderManager().viewerPosY;
        double renderPosZ = mc.getRenderManager().viewerPosZ;
        frustum.setPosition(renderPosX, renderPosY, renderPosZ);
        setupGL();
        float lineWidth = (float) Dark.instance.settingsManager.getSettingByName(this, "LineWidth").getValDouble();
        GL11.glLineWidth(lineWidth);
        GlStateManager.color(RED, GREEN, BLUE, ALPHA);
        for (Entity entity : cachedEntities) {
            if (!frustum.isBoundingBoxInFrustum(entity.getEntityBoundingBox())) continue;
            drawESPOptimized(entity, event.partialTicks, renderPosX, renderPosY, renderPosZ);
        }
        restoreGL();
    }
    private void updateEntityCache() {
        cachedEntities.clear();
        boolean showPlayers = Dark.instance.settingsManager.getSettingByName(this, "ShowPlayers").getValBoolean();
        boolean showMobs = Dark.instance.settingsManager.getSettingByName(this, "ShowMobs").getValBoolean();
        double playerX = mc.thePlayer.posX;
        double playerY = mc.thePlayer.posY;
        double playerZ = mc.thePlayer.posZ;
        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (entity == mc.thePlayer) continue;
            if (entity instanceof EntityArmorStand) continue;
            if (!entity.isEntityAlive()) continue;
            boolean shouldRender = false;
            if (showPlayers && entity instanceof EntityPlayer) {
                shouldRender = true;
            } else if (showMobs && entity instanceof EntityMob) {
                shouldRender = true;
            }
            if (!shouldRender) continue;
            double dx = entity.posX - playerX;
            double dy = entity.posY - playerY;
            double dz = entity.posZ - playerZ;
            double distanceSq = dx * dx + dy * dy + dz * dz;
            if (distanceSq > maxRenderDistanceSq) continue;
            cachedEntities.add(entity);
        }
    }
    private void setupGL() {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }
    private void restoreGL() {
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
    private void drawESPOptimized(Entity entity, float partialTicks, double renderPosX, double renderPosY, double renderPosZ) {
        AxisAlignedBB entityBox = entity.getEntityBoundingBox();
        if (entityBox == null) return;
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - renderPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - renderPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - renderPosZ;
        double minX = entityBox.minX - entity.posX + x;
        double minY = entityBox.minY - entity.posY + y;
        double minZ = entityBox.minZ - entity.posZ + z;
        double maxX = entityBox.maxX - entity.posX + x;
        double maxY = entityBox.maxY - entity.posY + y;
        double maxZ = entityBox.maxZ - entity.posZ + z;
        drawBox(minX, minY, minZ, maxX, maxY, maxZ);
    }
    private void drawBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(maxX, minY, minZ);
        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINE_STRIP);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex3d(minX, minY, minZ);
        GL11.glVertex3d(minX, maxY, minZ);
        GL11.glVertex3d(maxX, minY, minZ);
        GL11.glVertex3d(maxX, maxY, minZ);
        GL11.glVertex3d(maxX, minY, maxZ);
        GL11.glVertex3d(maxX, maxY, maxZ);
        GL11.glVertex3d(minX, minY, maxZ);
        GL11.glVertex3d(minX, maxY, maxZ);
        GL11.glEnd();
    }
}