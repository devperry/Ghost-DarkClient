package me.client.clickgui;
import me.client.Dark;
import me.client.module.Category;
import me.client.module.Module;
import me.client.module.render.ClickGUI;
import me.client.settings.Setting;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
public class ClickGui extends GuiScreen {
    public float windowX = -1;
    public float windowY = -1;
    public float guiWidth = 0;
    public float guiHeight = 0;
    public float catWidth, modWidth, setWidth;
    private Category selectedCategory = Category.COMBAT;
    private Module selectedModule = null;
    private float modScrollTarget = 0, modScrollAnim = 0;
    private float setScrollTarget = 0, setScrollAnim = 0;
    private boolean draggingWindow = false;
    private float dragX = 0, dragY = 0;
    private boolean draggingModScroll = false;
    private boolean draggingSetScroll = false;
    private int lastMouseY = 0;
    private Setting activeSlider = null;
    private boolean activeSliderIsMin = false;
    private boolean isBinding = false;
    public static class Palette {
        public int accent;
        public int selectedBg;
        public int variantBg;
        public int darkerBg;
        public int textBright;
        public int textMuted;
        public int textDim;
        public int mainBg;
        public int cloneBg;
        public int cloneTextBright;
        public int cloneTextDim;
        public int createClone;
        public int deleteClone;
        public Palette(int accent, int selectedBg, int variantBg, int darkerBg,
                int textBright, int textMuted, int textDim, int mainBg, int cloneBg,
                int cloneTextBright, int cloneTextDim,
                int createClone, int deleteClone) {
            this.accent = accent;
            this.selectedBg = selectedBg;
            this.variantBg = variantBg;
            this.darkerBg = darkerBg;
            this.textBright = textBright;
            this.textMuted = textMuted;
            this.textDim = textDim;
            this.mainBg = mainBg;
            this.cloneBg = cloneBg;
            this.cloneTextBright = cloneTextBright;
            this.cloneTextDim = cloneTextDim;
            this.createClone = createClone;
            this.deleteClone = deleteClone;
        }
    }
    public static final Palette PURPLE = new Palette(
    0xFF7C3AED, 0xFF231F3A, 0xFF1C1A2E, 0xFF0F0E17,
    0xFFE2D9F3, 0xFF9D93BB, 0xFF837A9E, 0xFA18162A,
    0xFF1A1728, 0xFFD0A0FF, 0xFF8570A5,
    0xFF4A3485, 0xFF853434
    );
    public static final Palette LAVA = new Palette(
    0xFFFF4500, 0xFF3A0F0F, 0xFF2A1410, 0xFF170A09,
    0xFFFFD9C3, 0xFFBB8E78, 0xFF8E6F5C, 0xFA2A0E0A,
    0xFF2A1810, 0xFFFFB0A0, 0xFFB07560,
    0xFF853434, 0xFF4A3485
    );
    public static final Palette TOXIC = new Palette(
    0xFF4ADE80, 0xFF0F3A23, 0xFF142A1F, 0xFF09170F,
    0xFFD9F3E2, 0xFF8FBC9D, 0xFF6E947E, 0xFA0E2A18,
    0xFF142818, 0xFFB0FFD0, 0xFF75B090,
    0xFF2A6A4A, 0xFF6A2A2A
    );
    public static final Palette DARK = new Palette(
    0xFFE0E0E0, 0xFF2E2E2E, 0xFF1E1E1E, 0xFF111111,
    0xFFE8E8E8, 0xFF8C8C8C, 0xFF6E6E6E, 0xFA141414,
    0xFF1A1A1A, 0xFFC8C8C8, 0xFF707070,
    0xFF3A3A3A, 0xFF4A4A4A
    );
    /** Devuelve la paleta activa leyendo la setting Theme. Fallback a Purple. */
    private Palette getCurrentPalette() {
        try {
            Setting themeSet = ClickGUI.getThemeSetting();
            if (themeSet != null) {
                String name = themeSet.getValString();
                if (name != null) {
                    String n = name.trim();
                    if (n.equalsIgnoreCase("Purple")) return PURPLE;
                    if (n.equalsIgnoreCase("Lava")) return LAVA;
                    if (n.equalsIgnoreCase("Toxic")) return TOXIC;
                    if (n.equalsIgnoreCase("Dark")) return DARK;
                }
            }
        } catch (Throwable ignored) {
        }
        return PURPLE;
    }
    @Override
    public void initGui() {
        super.initGui();
        guiWidth = Math.min(500, this.width * 0.9f);
        guiHeight = Math.min(330, this.height * 0.85f);
        if (windowX == -1) {
            windowX = (this.width - guiWidth) / 2.0f;
            windowY = (this.height - guiHeight) / 2.0f;
        }
        catWidth = guiWidth * 0.23f;
        modWidth = guiWidth * 0.35f;
        setWidth = guiWidth * 0.42f;
        if (selectedModule == null) {
            ArrayList<
                    Module> mods = Dark.instance.moduleManager.getModulesInCategory(selectedCategory);
            if (!mods.isEmpty()) selectedModule = mods.get(0);
        }
    }
    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Palette p = getCurrentPalette();
        Gui.drawRect(0, 0, this.width, this.height, 0xA00A0914);
        if (!Mouse.isButtonDown(0)) {
            draggingWindow = false;
            draggingModScroll = false;
            draggingSetScroll = false;
            activeSlider = null;
        }
        if (draggingWindow) {
            windowX = mouseX - dragX;
            windowY = mouseY - dragY;
        }
        ArrayList<Module> mods = Dark.instance.moduleManager.getModulesInCategory(selectedCategory);
        float maxModScroll = (mods.size() * 35) - (guiHeight - 40);
        if (maxModScroll < 0) maxModScroll = 0;
        float totalSettingsHeight = 90;
        ArrayList<
                Setting> settings = selectedModule != null ? Dark.instance.settingsManager.getSettingsByMod(selectedModule) : null;
        if (settings != null) {
            for (Setting s : settings) {
                totalSettingsHeight += (s.isSlider() || s.isRangeSlider()) ? 40 : 30;
            }
        }
        float maxSetScroll = totalSettingsHeight - (guiHeight - 40);
        if (maxSetScroll < 0) maxSetScroll = 0;
        if (draggingModScroll) {
            modScrollTarget += (mouseY - lastMouseY);
            lastMouseY = mouseY;
        } else if (draggingSetScroll) {
            setScrollTarget += (mouseY - lastMouseY);
            lastMouseY = mouseY;
        }
        if (modScrollTarget < -maxModScroll) modScrollTarget = -maxModScroll;
        if (modScrollTarget > 0) modScrollTarget = 0;
        if (setScrollTarget < -maxSetScroll) setScrollTarget = -maxSetScroll;
        if (setScrollTarget > 0) setScrollTarget = 0;
        modScrollAnim += (modScrollTarget - modScrollAnim) * 0.2f;
        setScrollAnim += (setScrollTarget - setScrollAnim) * 0.2f;
        float catX = windowX;
        float modX = windowX + catWidth;
        float setX = modX + modWidth;
        RenderUtils.drawRoundedRect(windowX, windowY, guiWidth, guiHeight, 8, p.mainBg);
        RenderUtils.drawRoundedRect(catX, windowY, catWidth, guiHeight, 8, p.darkerBg);
        Gui.drawRect((int) modX, (int) windowY + 30, (int) (windowX + guiWidth), (int) windowY + 31, p.selectedBg);
        GL11.glPushMatrix();
        float titleScale = 1.1f;
        GL11.glScalef(titleScale, titleScale, titleScale);
        this.mc.fontRendererObj.drawStringWithShadow("ghostdark", (catX + 8) / titleScale, (windowY + 10) / titleScale, p.accent);
        GL11.glPopMatrix();
        this.mc.fontRendererObj.drawStringWithShadow("Modules", modX + 10, windowY + 12, p.textBright);
        this.mc.fontRendererObj.drawStringWithShadow(selectedModule != null ? "Settings: " + selectedModule.getName() : "Settings", setX + 10, windowY + 12, p.textMuted);
        float catY = windowY + 45;
        for (Category category : Category.values()) {
            boolean isSelected = (category == selectedCategory);
            if (isSelected) {
                RenderUtils.drawRoundedRect(catX + 5, catY, catWidth - 10, 25, 4, p.selectedBg);
                RenderUtils.drawRoundedRect(catX + 5, catY + 4, 3, 17, 1, p.accent);
            } else if (mouseX >= catX + 5 && mouseX <= catX + catWidth - 5 && mouseY >= catY && mouseY <= catY + 25) {
                RenderUtils.drawRoundedRect(catX + 5, catY, catWidth - 10, 25, 4, p.variantBg);
            }
            int catColor = isSelected ? p.textBright : p.textDim;
            this.mc.fontRendererObj.drawStringWithShadow(category.name(), catX + 15, catY + 8, catColor);
            catY += 30;
        }
        RenderUtils.prepareScissorBox(modX, windowY + 31, setX, windowY + guiHeight);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        float modY = windowY + 40 + modScrollAnim;
        for (Module m : mods) {
            boolean isSelected = (m == selectedModule);
            int boxColor;
            if (isSelected) boxColor = p.selectedBg;
            else if (m.isClone) boxColor = p.cloneBg;
            else boxColor = p.variantBg;
            RenderUtils.drawRoundedRect(modX + 5, modY, modWidth - 10, 30, 4, boxColor);
            float switchX = modX + modWidth - 35;
            RenderUtils.drawRoundedRect(switchX, modY + 7, 26, 14, 7, m.isToggled() ? p.accent : p.darkerBg);
            RenderUtils.drawRoundedRect(switchX + 2 + (m.isToggled() ? 12 : 0), modY + 9, 10, 10, 5, p.textBright);
            int textColor;
            if (m.isClone) textColor = m.isToggled() ? p.cloneTextBright : p.cloneTextDim;
            else textColor = m.isToggled() ? p.textBright : p.textMuted;
            this.mc.fontRendererObj.drawStringWithShadow(m.getName(), modX + 12, modY + 11, textColor);
            modY += 35;
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        if (selectedModule != null) {
            RenderUtils.prepareScissorBox(setX, windowY + 31, windowX + guiWidth, windowY + guiHeight);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            float setY = windowY + 40 + setScrollAnim;
            float sWidth = setWidth - 15;
            if (settings != null) {
                for (Setting s : settings) {
                    if (s.isCheck()) {
                        RenderUtils.drawRoundedRect(setX + 5, setY, sWidth, 25, 4, p.variantBg);
                        this.mc.fontRendererObj.drawStringWithShadow(s.getName(), setX + 10, setY + 8, p.textBright);
                        float switchX = setX + 5 + sWidth - 30;
                        RenderUtils.drawRoundedRect(switchX, setY + 5, 26, 14, 7, s.getValBoolean() ? p.accent : p.darkerBg);
                        RenderUtils.drawRoundedRect(switchX + 2 + (s.getValBoolean() ? 12 : 0), setY + 7, 10, 10, 5, p.textBright);
                        setY += 30;
                    } else if (s.isCombo()) {
                        RenderUtils.drawRoundedRect(setX + 5, setY, sWidth, 25, 4, p.variantBg);
                        this.mc.fontRendererObj.drawStringWithShadow(s.getName() + ":", setX + 10, setY + 8, p.textMuted);
                        this.mc.fontRendererObj.drawStringWithShadow(s.getValString(), setX + 10 + this.mc.fontRendererObj.getStringWidth(s.getName() + ": ") + 2, setY + 8, p.textBright);
                        setY += 30;
                    }else if (s.isSlider()) {
                        RenderUtils.drawRoundedRect(setX + 5, setY, sWidth, 35, 4, p.variantBg);
                        this.mc.fontRendererObj.drawStringWithShadow(s.getName() + ": " + s.getValDouble(), setX + 10, setY + 5, p.textBright);
                        float sliderW = sWidth - 10;
                        double percent = (s.getValDouble() - s.getMin()) / (s.getMax() - s.getMin());
                        RenderUtils.drawRoundedRect(setX + 10, setY + 22, sliderW, 4, 2, p.darkerBg);
                        float fillW = (float) (sliderW * percent);
                        if (fillW > 0) {
                            RenderUtils.drawRoundedRect(setX + 10, setY + 22, fillW, 4, 2, p.accent);
                        }
                        float handleX = setX + 10 + fillW - 2; 
                        RenderUtils.drawRoundedRect(handleX, setY + 18, 4, 12, 2, p.textBright);
                        if (activeSlider == s && Mouse.isButtonDown(0)) {
                            double diff = Math.min(sliderW, Math.max(0, mouseX - (setX + 10)));
                            double newValue = roundToPlace((diff / sliderW) * (s.getMax() - s.getMin()) + s.getMin(), 2);
                            s.setValDouble(newValue);
                        }
                        setY += 40;
                    }
                    else if (s.isRangeSlider()) {
                        RenderUtils.drawRoundedRect(setX + 5, setY, sWidth, 35, 4, p.variantBg);
                        this.mc.fontRendererObj.drawStringWithShadow(s.getName() + ": " + s.getValDouble() + " - " + s.getValDouble2(), setX + 10, setY + 5, p.textBright);
                        float sliderW = sWidth - 10;
                        double percentMin = (s.getValDouble() - s.getMin()) / (s.getMax() - s.getMin());
                        double percentMax = (s.getValDouble2() - s.getMin()) / (s.getMax() - s.getMin());
                        RenderUtils.drawRoundedRect(setX + 10, setY + 22, sliderW, 4, 2, p.darkerBg);
                        float startX = (float) (setX + 10 + (sliderW * percentMin));
                        float endX = (float) (setX + 10 + (sliderW * percentMax));
                        float fillW = endX - startX;
                        if (fillW > 0) {
                            RenderUtils.drawRoundedRect(startX, setY + 22, fillW, 4, 2, p.accent);
                        }
                        RenderUtils.drawRoundedRect(startX - 2, setY + 18, 4, 12, 2, p.textBright); 
                        RenderUtils.drawRoundedRect(endX - 2, setY + 18, 4, 12, 2, p.textBright);   
                        if (activeSlider == s && Mouse.isButtonDown(0)) {
                            double diff = Math.min(sliderW, Math.max(0, mouseX - (setX + 10)));
                            double newValue = roundToPlace((diff / sliderW) * (s.getMax() - s.getMin()) + s.getMin(), 2);
                            if (activeSliderIsMin) {
                                s.setValDouble(Math.min(newValue, s.getValDouble2()));
                            } else {
                                s.setValDouble2(Math.max(newValue, s.getValDouble()));
                            }
                        }
                        setY += 40;
                    }
                }
            }
            RenderUtils.drawRoundedRect(setX + 5, setY, sWidth, 25, 4, p.variantBg);
            String keyText = isBinding ? "Presiona una tecla..." : Keyboard.getKeyName(selectedModule.getKey());
            this.mc.fontRendererObj.drawStringWithShadow("Tecla: " + keyText, setX + 10, setY + 8, isBinding ? p.accent : p.textBright);
            setY += 30;
            RenderUtils.drawRoundedRect(setX + 5, setY, sWidth, 25, 4, p.variantBg);
            this.mc.fontRendererObj.drawStringWithShadow("Visible en HUD", setX + 10, setY + 8, p.textBright);
            float switchX = setX + 5 + sWidth - 30;
            RenderUtils.drawRoundedRect(switchX, setY + 5, 26, 14, 7, selectedModule.visible ? p.accent : p.darkerBg);
            RenderUtils.drawRoundedRect(switchX + 2 + (selectedModule.visible ? 12 : 0), setY + 7, 10, 10, 5, p.textBright);
            setY += 30;
            if (!selectedModule.isClone) {
                RenderUtils.drawRoundedRect(setX + 5, setY, sWidth, 25, 4, p.createClone);
                this.mc.fontRendererObj.drawStringWithShadow("+ Crear Clon", setX + (sWidth / 2) - (this.mc.fontRendererObj.getStringWidth("+ Crear Clon") / 2), setY + 8, p.textBright);
            } else {
                RenderUtils.drawRoundedRect(setX + 5, setY, sWidth, 25, 4, p.deleteClone);
                this.mc.fontRendererObj.drawStringWithShadow("- Eliminar Clon", setX + (sWidth / 2) - (this.mc.fontRendererObj.getStringWidth("- Eliminar Clon") / 2), setY + 8, p.textBright);
            }
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        float catX = windowX;
        float modX = windowX + catWidth;
        float setX = modX + modWidth;
        if (mouseY >= windowY && mouseY <= windowY + 30 && mouseX >= windowX && mouseX <= windowX + guiWidth) {
            draggingWindow = true;
            dragX = mouseX - windowX;
            dragY = mouseY - windowY;
            return;
        }
        if (mouseX >= catX && mouseX <= modX) {
            float catY = windowY + 45;
            for (Category category : Category.values()) {
                if (mouseY >= catY && mouseY <= catY + 25) {
                    selectedCategory = category;
                    ArrayList<
                            Module> mods = Dark.instance.moduleManager.getModulesInCategory(selectedCategory);
                    selectedModule = mods.isEmpty() ? null : mods.get(0);
                    modScrollTarget = 0;
                    setScrollTarget = 0;
                    isBinding = false;
                    return;
                }
                catY += 30;
            }
        }
        if (mouseX >= modX && mouseX <= setX && mouseY > windowY + 30 && mouseY < windowY + guiHeight) {
            float modY = windowY + 40 + modScrollAnim;
            boolean clickedModule = false;
            for (Module m : Dark.instance.moduleManager.getModulesInCategory(selectedCategory)) {
                if (mouseY >= modY && mouseY <= modY + 30) {
                    float switchX = modX + modWidth - 35;
                    if (mouseX >= switchX - 10) {
                        m.toggle();
                    } else {
                        selectedModule = m;
                        setScrollTarget = 0;
                        isBinding = false;
                    }
                    clickedModule = true;
                    return;
                }
                modY += 35;
            }
            if (!clickedModule) {
                draggingModScroll = true;
                lastMouseY = mouseY;
            }
        }
        if (selectedModule != null && mouseX >= setX && mouseX <= windowX + guiWidth && mouseY > windowY + 30 && mouseY < windowY + guiHeight) {
            float setY = windowY + 40 + setScrollAnim;
            boolean clickedSetting = false;
            ArrayList<
                    Setting> settings = Dark.instance.settingsManager.getSettingsByMod(selectedModule);
            float sWidth = setWidth - 15;
            if (settings != null) {
                for (Setting s : settings) {
                    float height = (s.isSlider() || s.isRangeSlider()) ? 40 : 30;
                    if (mouseY >= setY && mouseY <= setY + height - 5) {
                        clickedSetting = true;
                        if (s.isCheck()) {
                            s.setValBoolean(!s.getValBoolean());
                        } else if (s.isCombo()) {
                            int idx = s.getOptions().indexOf(s.getValString());
                            idx = (idx + 1) % s.getOptions().size();
                            s.setValString(s.getOptions().get(idx));
                        } else if (s.isSlider()) {
                            activeSlider = s;
                        } else if (s.isRangeSlider()) {
                            double minX = setX + 10 + ((sWidth - 10) * ((s.getValDouble() - s.getMin()) / (s.getMax() - s.getMin())));
                            double maxX = setX + 10 + ((sWidth - 10) * ((s.getValDouble2() - s.getMin()) / (s.getMax() - s.getMin())));
                            double distMin = Math.abs(mouseX - minX);
                            double distMax = Math.abs(mouseX - maxX);
                            double grabRadius = 8; 
                            if (distMin <= grabRadius || distMax <= grabRadius) {
                                activeSlider = s;
                                activeSliderIsMin = distMin <= distMax;
                            }
                            if (s.getValDouble() == s.getMin() && s.getValDouble2() == s.getMin()) {
                                activeSliderIsMin = false; 
                            } 
                            else if (s.getValDouble() == s.getMax() && s.getValDouble2() == s.getMax()) {
                                activeSliderIsMin = true;
                            }
                            return;
                        }
                        return;
                    }
                    setY += height;
                }
            }
            if (mouseY >= setY && mouseY <= setY + 25) {
                isBinding = !isBinding;
                return;
            }
            setY += 30;
            if (mouseY >= setY && mouseY <= setY + 25) {
                selectedModule.visible = !selectedModule.visible;
                return;
            }
            setY += 30;
            if (mouseY >= setY && mouseY <= setY + 25) {
                if (!selectedModule.isClone) {
                    Dark.instance.moduleManager.createClone(selectedModule);
                } else {
                    Dark.instance.moduleManager.removeClone(selectedModule);
                    selectedModule = Dark.instance.moduleManager.getModulesInCategory(selectedCategory).get(0);
                    setScrollTarget = 0;
                }
                return;
            }
            if (!clickedSetting) {
                draggingSetScroll = true;
                lastMouseY = mouseY;
            }
        }
    }
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (isBinding && selectedModule != null) {
            if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_SPACE || keyCode == Keyboard.KEY_BACK || keyCode == Keyboard.KEY_DELETE) {
                selectedModule.setKey(0);
            } else {
                selectedModule.setKey(keyCode);
            }
            isBinding = false;
            return;
        }
        super.keyTyped(typedChar, keyCode);
    }
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0) {
            int mouseX = Mouse.getEventX() * this.width / this.mc.displayWidth;
            float modX = windowX + catWidth;
            float setX = modX + modWidth;
            if (mouseX >= modX && mouseX <= setX) {
                modScrollTarget += (wheel > 0) ? 35 : -35;
            } else if (mouseX >= setX && mouseX <= windowX + guiWidth) {
                setScrollTarget += (wheel > 0) ? 35 : -35;
            }
        }
    }
    private double roundToPlace(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}