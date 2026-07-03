package me.client.autosave;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import me.client.Dark;
import me.client.module.Module;
import me.client.settings.Setting;
import net.minecraft.client.Minecraft;
public class SaveLoad {
    private File dir;
    private File dataFile;
    public SaveLoad() {
        dir = new File(Minecraft.getMinecraft().mcDataDir, "DarkTeam");
        if (!dir.exists()) dir.mkdir();
        dataFile = new File(dir, "data.txt");
        if (!dataFile.exists()) {
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        this.load();
    }
    public void save() {
        if (Dark.instance.destructed) return;
        ArrayList<String> toSave = new ArrayList<String>();
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
            PrintWriter pw = new PrintWriter(this.dataFile);
            for (String str : toSave) pw.println(str);
            pw.close();
        } catch (FileNotFoundException e) { e.printStackTrace(); }
    }
    public void load() {
        if (Dark.instance.destructed) return;
        Dark.instance.moduleManager.clearClones();
        ArrayList<String> lines = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(this.dataFile));
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
            reader.close();
        } catch (Exception e) { e.printStackTrace(); }
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
                        boolean shouldToggle = Boolean.parseBoolean(args[2]);
                        if (m.isToggled() != shouldToggle) m.toggle();
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
    }
}