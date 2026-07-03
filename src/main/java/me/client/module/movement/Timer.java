package me.client.module.movement;
import me.client.Dark;
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
public class Timer extends Module {
        float Speed;
        boolean Voidcheck;
	public Timer() {
		super("Timer", "", Category.MOVEMENT);
        Dark.instance.settingsManager.rSetting(new Setting("Speed", this, 0.5, 0.01, 2.0, false));
        Dark.instance.settingsManager.rSetting(new Setting("CheckVoid", this, true));
	}
    private boolean isOnVoidCheckBelow() {
        EntityPlayerSP player = mc.thePlayer;
        if (player.onGround) return false;
        World world = player.worldObj;
        int playerX = MathHelper.floor_double(player.posX);
        int playerY = MathHelper.floor_double(player.posY); 
        int playerZ = MathHelper.floor_double(player.posZ);
        for (int yOffset = 1; yOffset <= 125; yOffset++) {
            int checkY = playerY - yOffset;
            if (checkY < 0) {
                continue; 
            }
            BlockPos checkPos = new BlockPos(playerX, checkY, playerZ);
            Block block = world.getBlockState(checkPos).getBlock();
            if (block.getMaterial().blocksMovement()) {
                return false;
            }
        }
        return true;
    }
	@SubscribeEvent
	public void RenderTick(final TickEvent.RenderTickEvent e) {
		if (!Util.nullCheck() || !this.isToggled())
			return;
        if (Timer.mc == null) {          
            return;
        }
        Speed = (float) Dark.instance.settingsManager.getSettingByName(this, "Speed").getValDouble();
        Voidcheck = Dark.instance.settingsManager.getSettingByName(this, "CheckVoid").getValBoolean();
        if(Voidcheck){
        if(isOnVoidCheckBelow()){
        Util.setTimerRate(1,Speed);	
        }else{
        Util.setTimerRate(1,1);
        }
        }else{
        Util.setTimerRate(1,Speed);	
        }
	}
    @Override
	public void onDisable() {
		super.onDisable();
        Util.setTimerRate(1,1);
		Speed = 1;
	}
}
