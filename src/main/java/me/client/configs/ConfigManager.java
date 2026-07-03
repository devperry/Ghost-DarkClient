package me.client.configs;
import me.client.Dark;
import me.client.module.Module;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
public class ConfigManager {
    private final File configDirectory;
    public ConfigManager() {
        configDirectory = new File(Minecraft.getMinecraft().mcDataDir, "DarkTeam/configs");
        if (!configDirectory.exists()) configDirectory.mkdirs();
    }
    public void saveConfig(String configName) {
        if (configName == null || configName.trim().isEmpty()) {
            sendMessage(EnumChatFormatting.RED + "Error: Debes proporcionar un nombre para la configuración.");
            return;
        }
        ArrayList<String> toSave = new ArrayList<>();
        for (Module mod : Dark.instance.moduleManager.modules) {
            toSave.add("MOD:" + mod.getName() + ":" + mod.isToggled() + ":" + mod.getKey() + ":" + mod.visible);
        }
        for (Setting set : Dark.instance.settingsManager.getSettings()) {                           
            if (set.isCheck()) toSave.add("SET:" + set.getName() + ":" + set.getParentMod().getName() + ":" + set.getValBoolean());
            else if (set.isCombo()) toSave.add("SET:" + set.getName() + ":" + set.getParentMod().getName() + ":" + set.getValString());
            else if (set.isSlider()) toSave.add("SET:" + set.getName() + ":" + set.getParentMod().getName() + ":" + set.getValDouble());                
            else if (set.isRangeSlider()) toSave.add("SET:" + set.getName() + ":" + set.getParentMod().getName() + ":" + set.getValDouble() + ":" + set.getValDouble2());              
        }
        try {
            File configFile = new File(configDirectory, configName + ".txt");
            PrintWriter pw = new PrintWriter(new FileWriter(configFile));
            for (String str : toSave) pw.println(str);
            pw.close();
            sendMessage(EnumChatFormatting.GREEN + "Configuración '" + configName + "' guardada.");
        } catch (IOException e) {
            e.printStackTrace();
            sendMessage(EnumChatFormatting.RED + "Error al guardar la configuración.");
        }
    }
    public void loadConfig(String configName) {
        if (configName == null || configName.trim().isEmpty()) {
            sendMessage(EnumChatFormatting.RED + "Error: Debes proporcionar un nombre.");
            return;
        }
        File configFile = new File(configDirectory, configName + ".txt");
        if (!configFile.exists() || !configFile.isFile()) {
            sendMessage(EnumChatFormatting.RED + "Error: La configuración '" + configName + "' no existe.");
            return;
        }
        Dark.instance.moduleManager.clearClones();
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line;
            while ((line = reader.readLine()) != null) lines.add(line);
        } catch (IOException e) {
            e.printStackTrace();
            sendMessage(EnumChatFormatting.RED + "Error al leer el archivo.");
            return;
        }
        for (String s : lines) {
            String[] args = s.split(":");
            if (s.toLowerCase().startsWith("mod:")) {
                String modName = args[1];
                Module m = Dark.instance.moduleManager.getModule(modName);
                if (m == null && modName.matches(".* \\(\\d+\\)")) {
                    String origName = modName.substring(0, modName.lastIndexOf(" ("));
                    Module origMod = Dark.instance.moduleManager.getModule(origName);
                    if (origMod != null) {
                        m = Dark.instance.moduleManager.createClone(origMod, modName);
                    }
                }
                if (m != null) {
                    if (args.length > 3) {
                        boolean shouldBeToggled = Boolean.parseBoolean(args[2]);
                        if (m.isToggled() != shouldBeToggled) m.toggle();
                        m.setKey(Integer.parseInt(args[3]));
                    }
                    if (args.length > 4) {
                        m.visible = Boolean.parseBoolean(args[4]);
                    }
                }
            } else if (s.toLowerCase().startsWith("set:")) {
                Module m = Dark.instance.moduleManager.getModule(args[2]);
                if (m != null) {
                    Setting set = Dark.instance.settingsManager.getSettingByName(m, args[1]);
                    if (set != null) {
                        if (set.isCheck()) set.setValBoolean(Boolean.parseBoolean(args[3]));
                        else if (set.isCombo()) set.setValString(args[3]);
                        else if (set.isSlider()) set.setValDouble(Double.parseDouble(args[3]));
                        else if (set.isRangeSlider()) {
                            set.setValDouble(Double.parseDouble(args[3]));
                            set.setValDouble2(Double.parseDouble(args[4]));
                        }
                    }
                }
            }
        }
        sendMessage(EnumChatFormatting.GREEN + "Configuración '" + configName + "' cargada.");
    }
    public void listConfigs() {
        File[] files = configDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".txt"));
        if (files == null || files.length == 0) {
            sendMessage(EnumChatFormatting.YELLOW + "No se encontraron configuraciones.");
            return;
        }
        String fileNames = Arrays.stream(files)
                                 .map(file -> file.getName().replace(".txt", ""))
                                 .collect(Collectors.joining(", "));
        sendMessage(EnumChatFormatting.AQUA + "Configuraciones disponibles: " + EnumChatFormatting.WHITE + fileNames);
    }
    private void sendMessage(String message) {
        Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(EnumChatFormatting.DARK_PURPLE + "[Dark] " + message));
    }
}