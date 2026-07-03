package me.client.module.player;
import me.client.module.Category;
import me.client.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import me.client.module.util.Util;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
public class AutoTool extends Module {
    private Block previousBlock;
    private boolean isWaiting;
    public static int previousSlot;
    public static boolean justFinishedMining, mining;
    public static List<Class<?>> pickaxe = Arrays.asList(ItemBlock.class, BlockIce.class);
    public AutoTool() {
        super("Auto Tool", "Intercambia Automaticamente Las Herramientas", Category.PLAYER);
    }
    @SubscribeEvent
    public void onRenderTick(TickEvent.RenderTickEvent e) {
        if (!Util.nullCheck())
            return;
        if(!Mouse.isButtonDown(0)){
            if(mining)
                finishMining();
            if(isWaiting)
                isWaiting = false;
            return;
        }
        BlockPos lookingAtBlock = mc.objectMouseOver.getBlockPos();
        if (lookingAtBlock != null) {
            Block stateBlock = mc.theWorld.getBlockState(lookingAtBlock).getBlock();
            if (stateBlock != Blocks.air && !(stateBlock instanceof BlockLiquid) && stateBlock instanceof Block) {
                    if(previousBlock != null){
                        if(previousBlock!=stateBlock){
                            previousBlock = stateBlock;
                            isWaiting = true;
                        } else {
                            if(isWaiting) {
                                isWaiting = false;
                                previousSlot = getCurrentPlayerSlot();
                                mining = true;
                                hotkeyToFastest();
                            }
                        }
                    } else {
                        previousBlock = stateBlock;
                        isWaiting = false;
                    }
                    return;
                }
                if(!mining) {
                    previousSlot = getCurrentPlayerSlot();
                    mining = true;
                }
                hotkeyToFastest();
            }
        }
      private static int getCurrentPlayerSlot() {
         return mc.thePlayer.inventory.currentItem;
      }
    private static void hotkeyToSlot(int slot) {
         mc.thePlayer.inventory.currentItem = slot;
      }
    private void finishMining(){
            hotkeyToSlot(previousSlot);
            justFinishedMining = false;
        mining = false;
    }
    private void hotkeyToFastest(){
        int index = -1;
        double speed = 1;
        for (int slot = 0; slot <= 8; slot++) {
            ItemStack itemInSlot = mc.thePlayer.inventory.getStackInSlot(slot);
            if(itemInSlot != null) {
                if( itemInSlot.getItem() instanceof ItemTool || itemInSlot.getItem() instanceof ItemShears){
                    BlockPos p = mc.objectMouseOver.getBlockPos();
                    Block bl = mc.theWorld.getBlockState(p).getBlock();
                    if(itemInSlot.getItem().getDigSpeed(itemInSlot, bl.getDefaultState()) > speed) {
                        speed = itemInSlot.getItem().getDigSpeed(itemInSlot, bl.getDefaultState());
                        index = slot;
                    }
                }
            }
        }
        if(index == -1 || speed <= 1.1 || speed == 0) {
        } else {
            hotkeyToSlot(index);
        }
    }
}
