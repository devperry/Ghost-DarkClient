package me.client.clickgui;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;
import static org.lwjgl.opengl.GL11.*;
public class RenderUtils {
    /**
     * Dibuja un rectángulo relleno con esquinas redondeadas.
     */
    public static void drawRoundedRect(float x, float y, float width, float height, float radius, int color) {
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >> 8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;
        float a = (float) (color >> 24 & 255) / 255.0F;
        if (radius > width / 2)  radius = width / 2;
        if (radius > height / 2) radius = height / 2;
        glPushAttrib(GL_ALL_ATTRIB_BITS);
        glPushMatrix();
        glEnable(GL_BLEND);
        glDisable(GL_TEXTURE_2D);
        glDisable(GL_CULL_FACE); 
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glColor4f(r, g, b, a);
        glRectf(x + radius, y, x + width - radius, y + height); 
        glRectf(x, y + radius, x + width, y + height - radius); 
        drawArc(x + radius, y + radius, radius, 180, 270);                      
        drawArc(x + width - radius, y + radius, radius, 270, 360);              
        drawArc(x + width - radius, y + height - radius, radius, 0, 90);      
        drawArc(x + radius, y + height - radius, radius, 90, 180);              
        glPopMatrix();
        glPopAttrib();
    }
    private static void drawArc(float x, float y, float radius, int startAngle, int endAngle) {
        final int segments = 90; 
        glBegin(GL_TRIANGLE_FAN);
        {
            glVertex2f(x, y);
            for (int i = startAngle; i <= endAngle; i += (360 / segments)) {
                double angle = Math.toRadians(i);
                glVertex2f((float) (x + radius * Math.cos(angle)), (float) (y + radius * Math.sin(angle)));
            }
        }
        glEnd();
    }
    /**
     * Novedad v3.0: Aplica un recorte (Scissor) usando coordenadas reales de pantalla.
     * Escala matemáticamente a la resolución nativa de Minecraft para no romper proporciones.
     */
    public static void prepareScissorBox(float x, float y, float x2, float y2) {
        ScaledResolution scale = new ScaledResolution(Minecraft.getMinecraft());
        int factor = scale.getScaleFactor();
        GL11.glScissor((int) (x * factor), (int) ((scale.getScaledHeight() - y2) * factor), (int) ((x2 - x) * factor), (int) ((y2 - y) * factor));
    }
}