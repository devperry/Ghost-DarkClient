package me.client;
import org.lwjgl.input.Keyboard;
import me.client.autosave.SaveLoad;
import me.client.clickgui.ClickGui;
import me.client.commands.CommandManager;
import me.client.configs.ConfigManager;
import me.client.module.Module;
import me.client.module.ModuleManager;
import me.client.settings.SettingsManager;
import me.client.module.util.PacketInjector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import java.lang.reflect.Field;
public class Dark
{
    private static final Minecraft mc = Minecraft.getMinecraft();
    public static Dark instance;
    public ModuleManager moduleManager;
    public SettingsManager settingsManager;
    public ClickGui clickGui;
    public SaveLoad saveLoad;
    public ConfigManager configManager;
    public CommandManager commandManager;
    public boolean destructed = false;
    private Field chatInputField = null;
    public void init() {
        instance = this;
    	MinecraftForge.EVENT_BUS.register(this);
    	MinecraftForge.EVENT_BUS.register(new PacketInjector());
    	settingsManager = new SettingsManager();
    	moduleManager = new ModuleManager();
    	clickGui = new ClickGui();
    	saveLoad = new SaveLoad();
        configManager = new ConfigManager();
        commandManager = new CommandManager();
    }
    @SubscribeEvent
    public void onGuiKeyPress(GuiScreenEvent.KeyboardInputEvent.Pre event) {
        if (!(event.gui instanceof GuiChat) || Keyboard.getEventKey() != Keyboard.KEY_RETURN) {
            return;
        }
        try {
            if (chatInputField == null) {
                try {
                    chatInputField = GuiChat.class.getDeclaredField("inputField");
                } catch (NoSuchFieldException e) {
                    chatInputField = GuiChat.class.getDeclaredField("field_146415_a");
                }
                chatInputField.setAccessible(true);
            }
            GuiTextField textField = (GuiTextField) chatInputField.get(event.gui);
            String message = textField.getText();
            String prefix = CommandManager.getCurrentPrefix();
            if (message.startsWith(prefix)) {
                mc.ingameGUI.getChatGUI().addToSentMessages(message);
                textField.setText("");
                commandManager.handleCommand(message);
                mc.thePlayer.closeScreen();
                event.setCanceled(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @SubscribeEvent
    public void key(KeyInputEvent e) {
    	if (Minecraft.getMinecraft().theWorld == null || Minecraft.getMinecraft().thePlayer == null)
    		return;
    	try {
             if (Keyboard.isCreated()) {
                 if (Keyboard.getEventKeyState()) {
                     int keyCode = Keyboard.getEventKey();
                     if (keyCode <= 0)
                    	 return;
                     for (Module m : moduleManager.modules) {
                    	 if (m.getKey() == keyCode && keyCode > 0) {
                    		 m.toggle();
                    	 }
                     }
                 }
             }
         } catch (Exception q) { q.printStackTrace(); }
    }
    public void onDestruct() {
    	if (Minecraft.getMinecraft().currentScreen != null && Minecraft.getMinecraft().thePlayer != null) {
    		Minecraft.getMinecraft().thePlayer.closeScreen();
    	}
    	destructed = true;
    	MinecraftForge.EVENT_BUS.unregister(this);
    	for (int k = 0; k < this.moduleManager.modules.size(); k++) {
    		Module m = this.moduleManager.modules.get(k);
    		MinecraftForge.EVENT_BUS.unregister(m);
    		this.moduleManager.getModuleList().remove(m);
    	}
    	this.moduleManager = null;
    	this.clickGui = null;
    }
}