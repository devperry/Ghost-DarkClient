package me.client.settings;
import java.util.ArrayList;
import me.client.module.Module;
public class SettingsManager {
	private ArrayList<Setting> settings;
	public SettingsManager(){
		this.settings = new ArrayList<Setting>();
	}
	public void rSetting(Setting in){
		this.settings.add(in);
	}
	public ArrayList<Setting> getSettings(){
		return this.settings;
	}
	public ArrayList<Setting> getSettingsByMod(Module mod){
		ArrayList<Setting> out = new ArrayList<Setting>();
		for(Setting s : getSettings()){
			if(s.getParentMod().equals(mod)){
				out.add(s);
			}
		}
		if(out.isEmpty()){
			return null;
		}
		return out;
	}
	public Setting getSettingByName(Module mod, String name){
		for(Setting set : getSettings()){
			if(set.getName().equalsIgnoreCase(name) && set.getParentMod() == mod){
				return set;
			}
		}
		System.err.println("[Dark] Warning: Setting NOT found: '" + name +"' in module " + (mod != null ? mod.getName() : "null") + "! Using fallback.");
		return new Setting(name, mod, false);
	}
	public Setting getGlobalSetting(String name) {
		for(Setting set : getSettings()){
			if(set.getName().equalsIgnoreCase(name) && set.getParentMod() == null){
				return set;
			}
		}
		return null;
	}
	public void cloneSettings(Module original, Module clone) {
		ArrayList<Setting> origSettings = getSettingsByMod(original);
		if(origSettings != null) {
			for(Setting origSet : origSettings) {
				Setting cloneSet = getSettingByName(clone, origSet.getName());
				if (cloneSet != null) {
					if (origSet.isCheck()) {
						cloneSet.setValBoolean(origSet.getValBoolean());
					} 
					else if (origSet.isCombo()) {
						cloneSet.setValString(origSet.getValString());
					} 
					else if (origSet.isSlider()) {
						cloneSet.setValDouble(origSet.getValDouble());
					} 
					else if (origSet.isRangeSlider()) {
						cloneSet.setValDouble(origSet.getValDouble());
						cloneSet.setValDouble2(origSet.getValDouble2());
					}
				}
			}
		}
	}
}
