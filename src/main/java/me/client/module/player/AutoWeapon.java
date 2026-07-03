package me.client.module.player;
import com.google.common.collect.Multimap; 
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.Util;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes; 
import net.minecraft.entity.ai.attributes.AttributeModifier; 
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword; 
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Mouse; 
import java.util.Map; 
public class AutoWeapon extends Module {
    private boolean swap;
    private boolean requireClick;
    private int previousSlot = -1;
    private int ticksHovered;
    private Entity currentEntity;
    public AutoWeapon() {
        super("AutoWeapon", "Automatically switches to the best weapon when hovering over an entity.", Category.PLAYER);
        Dark.instance.settingsManager.rSetting(new Setting("Swap to previous slot", this, false));
        Dark.instance.settingsManager.rSetting(new Setting("Requires click", this, false));        
    }
    @Override
    public void onEnable() {
        super.onEnable();
        resetVariables();
        previousSlot = -1; 
    }
    @Override
    public void onDisable() {
        super.onDisable();
        resetSlot();
        resetVariables();
    }
    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) { 
            if (!this.isToggled() || !Util.nullCheck()) {
                resetSlot();
                resetVariables();
                return;
            }
            requireClick = Dark.instance.settingsManager.getSettingByName(this, "Requires click").getValBoolean();
            if(requireClick && !Mouse.isButtonDown(0)){ 
            return;
            }
            swap = Dark.instance.settingsManager.getSettingByName(this, "Swap to previous slot").getValBoolean();
            Entity hoveredEntity = mc.objectMouseOver != null ? mc.objectMouseOver.entityHit : null;
            if (!(hoveredEntity instanceof EntityLivingBase) || hoveredEntity == mc.thePlayer) {
                resetSlot();
                resetVariables();
                return;
            }
            ticksHovered = hoveredEntity.equals(currentEntity) ? ticksHovered + 1 : 0;
            currentEntity = hoveredEntity;
            if (ticksHovered > 0) { 
                int bestWeaponSlot = getWeapon(); 
                if (bestWeaponSlot != -1 && mc.thePlayer.inventory.currentItem != bestWeaponSlot) {
                    if (previousSlot == -1) {
                        previousSlot = getCurrentSlot();
                    }
                    setSlot(bestWeaponSlot);
                } else if (bestWeaponSlot == -1) {
                     resetSlot();
                     resetVariables(); 
                }
            } else {
                 resetSlot();
            }
        }
    }
    /**
     * Cambia el slot de la hotbar del jugador.
     * @param slot El índice del slot (0-8).
     */
    private void setSlot(int slot) {
        if (slot >= 0 && slot < 9) {
             mc.thePlayer.inventory.currentItem = slot;
        }
    }
    /**
     * Vuelve al slot guardado si existe uno.
     */
    private void resetSlot() {
        if (previousSlot == -1 || !swap) {
          return;
        }
            setSlot(previousSlot);
            previousSlot = -1; 
    }
    /**
     * Resetea las variables de seguimiento de la entidad.
     */
    private void resetVariables() {
        ticksHovered = 0;
        currentEntity = null;
    }
    /**
     * Obtiene el slot actual del jugador.
     * @return El índice del slot actual (0-8).
     */
    private int getCurrentSlot() {
        return mc.thePlayer.inventory.currentItem;
    }
    /**
     * Encuentra el slot con la mejor espada en la hotbar.
     * @return El índice del slot (0-8) con la mejor espada, o -1 si no hay espadas.
     */
    private int getWeapon() {
        int bestWeaponSlot = -1;
        double maxDamage = 0.0;
        for (int i = 0; i < InventoryPlayer.getHotbarSize(); ++i) {
            ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(i);
            if (itemStack != null && itemStack.getItem() instanceof ItemSword) {
                double currentDamage = getDamage(itemStack); 
                if (currentDamage > maxDamage) {
                    maxDamage = currentDamage;
                    bestWeaponSlot = i;
                }
            }
        }
        return bestWeaponSlot;
    }
    /**
     * Calcula el daño base de un ItemStack (principalmente para espadas).
     * @param stack El ItemStack del arma.
     * @return El daño base del arma.
     */
    private double getDamage(ItemStack stack) {
         if (stack == null) {
             return 0.0;
         }
        Multimap<String, AttributeModifier> attributeModifiers = stack.getAttributeModifiers();
        if (attributeModifiers != null && !attributeModifiers.isEmpty()) {
             for (Map.Entry<String, AttributeModifier> entry : attributeModifiers.entries()) {
                 if (entry.getKey().equals(SharedMonsterAttributes.attackDamage.getAttributeUnlocalizedName())) {
                    AttributeModifier modifier = entry.getValue();
                    if (modifier.getOperation() == 0) { 
                         return modifier.getAmount() + 1.0; 
                    }
                 }
             }
        }
        if (stack.getItem() instanceof ItemSword) {
             return ((ItemSword) stack.getItem()).getDamageVsEntity() + 1.0f; 
        }
        return 1.0; 
    }
}
