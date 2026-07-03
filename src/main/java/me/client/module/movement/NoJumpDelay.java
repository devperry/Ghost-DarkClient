package me.client.module.movement;
import java.lang.reflect.Field;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import me.client.module.util.utilities.StringRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase; 
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
public class NoJumpDelay extends Module
{
    private Field jumpTicks;
    private Minecraft mc;
    public NoJumpDelay() {
        super(StringRegistry.register(new String(new char[] {'N', 'o', 'J', 'u', 'm', 'p', 'D', 'e', 'l', 'a', 'y'})),"", Category.MOVEMENT);
        this.mc = Minecraft.getMinecraft();
        try {      
            this.jumpTicks = EntityLivingBase.class.getDeclaredField("field_70773_bE"); 
        }
        catch (Exception var4) {
            try {          
                this.jumpTicks = EntityLivingBase.class.getDeclaredField("jumpTicks"); 
            }
            catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (this.jumpTicks != null) {          
            this.jumpTicks.setAccessible(true);
        }
    }
    @SubscribeEvent
    public void onPlayerTick(final TickEvent.PlayerTickEvent e) {
        if(!Util.nullCheck())return;
            try {               
                this.jumpTicks.set(this.mc.thePlayer, 0); 
            }
            catch (IllegalAccessException ex) {
                ex.printStackTrace(); 
            }
    }
}