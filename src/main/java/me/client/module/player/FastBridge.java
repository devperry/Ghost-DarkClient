package me.client.module.player;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.ReflectionUtil;
import me.client.module.util.Util;
import me.client.settings.Setting; 
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
public class FastBridge extends Module {
    private Minecraft mc = Minecraft.getMinecraft();
    private boolean isSneaking = false;
    private long lastSneakTime = 0;
    public Setting pitchMin = new Setting("Min Pitch", this, 70.0, 0.0, 90.0, false);
    public Setting checkVoid = new Setting("Only Void", this, true);
    public Setting sneakDelayMs = new Setting("Sneak Delay (Ms)", this, 50.0, 0.0, 200.0, true);
    public FastBridge() {
        super("FastBridge", "Scaffold legit", Category.PLAYER);
        Dark.instance.settingsManager.rSetting(pitchMin);
        Dark.instance.settingsManager.rSetting(checkVoid);
        Dark.instance.settingsManager.rSetting(sneakDelayMs);
    }
    @Override
    public void onDisable() {
        super.onDisable();
        setSneak(false);
    }
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!this.isToggled() || !Util.nullCheck()) return;
        ItemStack heldItem = mc.thePlayer.getCurrentEquippedItem();
        if (heldItem == null || !(heldItem.getItem() instanceof ItemBlock)) {
            setSneak(false);
            return;
        }
        if (!mc.thePlayer.onGround || Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            setSneak(false);
            return;
        }
        if (mc.thePlayer.rotationPitch < pitchMin.getValDouble()) {
            setSneak(false);
            return;
        }
        BlockPos blockBelow = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 1.0D, mc.thePlayer.posZ);
        boolean isAirBelow = mc.theWorld.getBlockState(blockBelow).getBlock() == Blocks.air;
        if (isAirBelow) {
            if (checkVoid.getValBoolean() && !isOverVoid()) {
                setSneak(false);
                return;
            }
            if (System.currentTimeMillis() - lastSneakTime >= sneakDelayMs.getValDouble()) {
                setSneak(true);
                lastSneakTime = System.currentTimeMillis();
            }
        } else {
            setSneak(false);
            lastSneakTime = System.currentTimeMillis(); 
        }
    }
    /**
     * Método auxiliar para modificar la tecla de agacharse y no abusar de Reflection por cada tick.
     */
    private void setSneak(boolean state) {
        if (this.isSneaking == state) return; 
        this.isSneaking = state;
        try {
            ReflectionUtil.pressed.set(mc.gameSettings.keyBindSneak, state);
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }
    /**
     * Verifica si debajo del jugador hay puro aire hasta la coordenada Y = 0 (el vacío).
     */
    private boolean isOverVoid() {
        for (int y = (int) (mc.thePlayer.posY - 1); y >= 0; y--) {
            BlockPos pos = new BlockPos(mc.thePlayer.posX, y, mc.thePlayer.posZ);
            if (mc.theWorld.getBlockState(pos).getBlock() != Blocks.air) {
                return false; 
            }
        }
        return true; 
    }
}
