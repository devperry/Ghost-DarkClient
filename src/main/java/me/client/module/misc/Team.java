package me.client.module.misc;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.util.*;
import me.client.settings.Setting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
public class Team extends Module {
    private Setting checkArmor;
    private Setting checkColorName;
    private Setting checkTnt;
    public Team() {
        super("Team", "Don't Target Team", Category.MISC);
        this.checkArmor    = new Setting("Armor",      this, false);
        this.checkColorName = new Setting("Color Name", this, true);
        this.checkTnt      = new Setting("Tnt",        this, false);
        Dark.instance.settingsManager.rSetting(checkArmor);
        Dark.instance.settingsManager.rSetting(checkColorName);
        Dark.instance.settingsManager.rSetting(checkTnt);
    }
    public boolean isTeam(EntityPlayer player) {
        if (checkTnt.getValBoolean() && isTeamTNT(player)) return true;
        if (checkColorName.getValBoolean()) {
            String targetName = player.getDisplayName().getFormattedText();
            String localName  = mc.thePlayer.getDisplayName().getFormattedText();
            if (targetName.length() >= 2 && localName.length() >= 2
                    && targetName.charAt(0) == '\u00a7'
                    && localName.charAt(0)  == '\u00a7'
                    && targetName.charAt(1) == localName.charAt(1)) {
                return true;
            }
        }
        if (checkArmor.getValBoolean()) {
            ItemStack targetHelmet = player.getEquipmentInSlot(4);       
            ItemStack localHelmet  = mc.thePlayer.getEquipmentInSlot(4);
            if (targetHelmet != null && localHelmet != null
                    && targetHelmet.getItem() instanceof ItemArmor
                    && localHelmet.getItem()  instanceof ItemArmor) {
                int targetColor = ((ItemArmor) targetHelmet.getItem()).getColor(targetHelmet);
                int localColor  = ((ItemArmor) localHelmet.getItem()).getColor(localHelmet);
                if (targetColor != -1 && targetColor == localColor) {
                    return true;
                }
            }
        }
        if (player.getTeam() != null
                && mc.thePlayer.getTeam() != null
                && player.getTeam().isSameTeam(mc.thePlayer.getTeam())) {
            return true;
        }
        return false;
    }
public boolean isTeamTNT(EntityPlayer player) {
    return Util.hasTntInHotbarEdges(mc.thePlayer) && Util.hasTntInHotbarEdges(player);
}
}