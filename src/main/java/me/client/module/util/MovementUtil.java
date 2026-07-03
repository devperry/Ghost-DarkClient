package me.client.module.util;
import me.client.Dark;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import net.minecraft.world.World;
import java.util.List;
import java.util.Random;
import net.minecraft.util.ChatComponentText;
import me.client.Dark;
import org.lwjgl.input.Mouse;
import net.minecraft.client.settings.KeyBinding;
import me.client.settings.Setting;
import me.client.module.util.Util;
import me.client.module.Category;
import me.client.module.Module;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.*;
import net.minecraft.client.*;
import java.lang.reflect.*;
import net.minecraftforge.fml.common.gameevent.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraft.util.BlockPos; 
import net.minecraft.world.World; 
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.block.Block; 
import net.minecraft.util.MathHelper; 
import net.minecraft.block.material.Material; 
public class MovementUtil {
private static final Minecraft mc = Minecraft.getMinecraft();
public static boolean isRequiredSpeed(double speedMin, double speedMax) {
        double speed = Math.sqrt(mc.thePlayer.motionX * mc.thePlayer.motionX 
                               + mc.thePlayer.motionZ * mc.thePlayer.motionZ);
        double minSpeed = speedMin / 100D;
        double maxSpeed = speedMax / 100D;
        return speed >= minSpeed && speed <= maxSpeed;
    }
}