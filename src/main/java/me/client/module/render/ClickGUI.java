package me.client.module.render;
import java.util.ArrayList;
import org.lwjgl.input.Keyboard;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.settings.Setting;
import net.minecraft.client.gui.Gui;
public class ClickGUI extends Module {
    private static Setting themeSetting;
    private static Setting prefixSetting;
    /** Devuelve la setting "Theme" (Purple/Lava/Toxic/Dark). Null si aun no se inicializo. */
    public static Setting getThemeSetting() {
        return themeSetting;
    }
    /** Devuelve la setting "Command Prefix" (. / @ / - / #). Null si aun no se inicializo. */
    public static Setting getCommandPrefixSetting() {
        return prefixSetting;
    }
    /** Acceso directo al campo estatico publico (compatibilidad). */
    public static Setting theme;
    /** Acceso directo al campo estatico publico (compatibilidad). */
    public static Setting commandPrefix;
    public ClickGUI() {
        super("ClickGUI", "Allows you to enable and disable modules", Category.RENDER);
        this.setKey(Keyboard.KEY_RSHIFT);
        ArrayList<String> themeOptions = new ArrayList<String>();
        themeOptions.add("Purple");
        themeOptions.add("Lava");
        themeOptions.add("Toxic");
        themeOptions.add("Dark");
        theme = new Setting("Theme", this, "Purple", themeOptions);
        Dark.instance.settingsManager.rSetting(theme);
        ArrayList<String> prefixOptions = new ArrayList<String>();
        prefixOptions.add(".");
        prefixOptions.add("@");
        prefixOptions.add("-");
        prefixOptions.add("#");
        commandPrefix = new Setting("Command Prefix", this, ".", prefixOptions);
        Dark.instance.settingsManager.rSetting(commandPrefix);
        themeSetting = theme;
        prefixSetting = commandPrefix;
    }
    @Override
    public void onEnable() {
        super.onEnable();
        mc.displayGuiScreen(Dark.instance.clickGui);
        this.setToggled(false);
    }
}