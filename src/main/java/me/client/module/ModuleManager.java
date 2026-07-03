package me.client.module;
import java.util.ArrayList;
import java.util.Iterator;
import me.client.Dark;
import me.client.module.combat.*;
import me.client.module.player.*;
import me.client.module.misc.*;
import me.client.module.movement.*;
import me.client.module.render.*;
public class ModuleManager {
    public ArrayList<Module> modules;
    public ModuleManager() {
        (modules = new ArrayList<Module>()).clear();
        this.modules.add(new Blink());
        this.modules.add(new BackTrack());
        this.modules.add(new ClickGUI());
        this.modules.add(new HUD());
        this.modules.add(new SelfDestruct());    
        this.modules.add(new Velocity());
        this.modules.add(new FastFall());
        this.modules.add(new VelocityDecay());
        this.modules.add(new AimAssist());
        this.modules.add(new AutoClicker());
        this.modules.add(new Wtap());
        this.modules.add(new Team());
        this.modules.add(new STap());
        this.modules.add(new TradeHelper());
        this.modules.add(new HitSelect());      
        this.modules.add(new MidHitSelect());      
        this.modules.add(new Reach());
        this.modules.add(new MobNametag());
        this.modules.add(new NameTags());
        this.modules.add(new CpsDisplay());
        this.modules.add(new ESP());
        this.modules.add(new Fullbright());
        //this.modules.add(new AutoLeave());
        this.modules.add(new Sprint());
        this.modules.add(new Timer());
        this.modules.add(new Effects());              
        this.modules.add(new Parkour());
        this.modules.add(new AutoWeapon());
        this.modules.add(new AutoTool());
        this.modules.add(new FastBridge());
        this.modules.add(new FastPlace());
        this.modules.add(new Debug());
        this.modules.add(new FastDisconnect());
        this.modules.add(new AntiAfk());
        this.modules.add(new ReceiveHits());
        this.modules.add(new Delay17());        
       
    }
    public Module getModule(String name) {
        for (Module m : this.modules) {
            if (m.getName().equalsIgnoreCase(name)) {
                return m;
            }
        }
        return null;
    }
    public ArrayList<Module> getModuleList() {
        return this.modules;
    }
    public ArrayList<Module> getModulesInCategory(Category c) {
        ArrayList<Module> mods = new ArrayList<Module>();
        for (Module m : this.modules) {
            if (m.getCategory() == c) {
                mods.add(m);
            }
        }
        return mods;
    }
    public Module createClone(Module original, String forcedName) {
        try {
            Module clone = original.getClass().newInstance();
            clone.isClone = true;
            clone.setKey(0); 
            if (forcedName != null) {
                clone.setName(forcedName);
            } else {
                int cloneCount = 1;
                for (Module m : this.modules) {
                    if (m.isClone && m.getName().startsWith(original.getName())) cloneCount++;
                }
                clone.setName(original.getName() + " (" + (cloneCount + 1) + ")");
            }
            int index = this.modules.indexOf(original);
            if(index != -1) {
                this.modules.add(index + 1, clone);
            } else {
                this.modules.add(clone);
            }
            Dark.instance.settingsManager.cloneSettings(original, clone);
            return clone;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public void createClone(Module original) {
        createClone(original, null);
    }
    public void removeClone(Module clone) {
        if (clone.isToggled()) clone.toggle(); 
        this.modules.remove(clone);
        Dark.instance.settingsManager.getSettings().removeIf(s -> s.getParentMod() == clone);
    }
    public void clearClones() {
        Iterator<Module> iterator = this.modules.iterator();
        while (iterator.hasNext()) {
            Module m = iterator.next();
            if (m.isClone) {
                if (m.isToggled()) m.toggle();
                Dark.instance.settingsManager.getSettings().removeIf(s -> s.getParentMod() == m);
                iterator.remove();
            }
        }
    }
}
