package me.client.commands;
import me.client.Dark;
import me.client.module.render.ClickGUI;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
public class CommandManager {
    /**
     * Devuelve el prefijo actual. Usa el getter estatico del modulo ClickGUI
     * (no el campo directo) para ser robusto frente a problemas de
     * inicializacion.
     */
    public static String getCurrentPrefix() {
        try {
            Setting prefixSet = ClickGUI.getCommandPrefixSetting();
            if (prefixSet != null) {
                String s = prefixSet.getValString();
                if (s != null && !s.isEmpty()) return s;
            }
        } catch (Throwable ignored) {}
        return ".";
    }
    public void handleCommand(String rawMessage) {
        String prefix = getCurrentPrefix();
        if (!rawMessage.startsWith(prefix)) {
            return;
        }
        String message = rawMessage.substring(prefix.length());
        String[] args = message.split(" ");
        String commandName = args[0].toLowerCase();
        if (commandName.equals("config")) {
            if (args.length == 1) {
                sendUsage();
                return;
            }
            String subCommand = args[1].toLowerCase();
            switch (subCommand) {
                case "save":
                    if (args.length < 3) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.RED + "Uso: " + prefix + "config save <nombre>"));
                    } else {
                        Dark.instance.configManager.saveConfig(args[2]);
                    }
                    break;
                case "load":
                    if (args.length < 3) {
                        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                                EnumChatFormatting.RED + "Uso: " + prefix + "config load <nombre>"));
                    } else {
                        Dark.instance.configManager.loadConfig(args[2]);
                    }
                    break;
                case "list":
                    Dark.instance.configManager.listConfigs();
                    break;
                default:
                    sendUsage(prefix);
                    break;
            }
        }
    }
    private void sendUsage() {
        sendUsage(getCurrentPrefix());
    }
    private void sendUsage(String prefix) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(
                EnumChatFormatting.RED + "Invalid. Use: " + prefix + "config <save|load|list> [name]"));
    }
}