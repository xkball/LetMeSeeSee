package com.xkball.let_me_see_see.client.gui.frame.screen;

import com.xkball.let_me_see_see.client.gui.frame.core.HorizontalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.PanelConfig;
import com.xkball.let_me_see_see.client.gui.frame.core.VerticalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.render.GuiDecorations;
import com.xkball.let_me_see_see.client.gui.frame.core.render.SimpleBackgroundRenderer;
import com.xkball.let_me_see_see.client.gui.frame.widget.BlankWidget;
import com.xkball.let_me_see_see.client.gui.frame.widget.Label;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.AutoResizeWidgetWrapper;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.HorizontalPanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.VerticalPanel;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.loading.FMLPaths;

public class FrameScreen extends Screen {
    
    public static final float THE_SCALE = 0.3731f;
    
    protected boolean needUpdate = false;
    
    protected FrameScreen(Component title) {
        super(title);
    }
    
    public void updateScreen() {
        for (var child : this.children()) {
            if (child instanceof IPanel panel) {
                panel.update();
            }
        }
    }
    
    public void setNeedUpdate(){
        this.needUpdate = true;
    }
    
    @Override
    public void tick() {
        super.tick();
        if(needUpdate) {
            needUpdate = false;
            updateScreen();
        }
    }
    
    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        IPanel.DebugLine.drawAllDebugLines(guiGraphics);
    }
    
    @Override
    public boolean isPauseScreen() {
        return false;
    }
    
    public <T extends AbstractWidget & IPanel> VerticalPanel screenFrame(String titleTransKey, T content) {
        var screen = PanelConfig.of()
                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                .apply(VerticalPanel.of(this));
        screen.addWidget(PanelConfig.of(1, 0.04f)
                        .align(HorizontalAlign.CENTER, VerticalAlign.CENTER)
                        .sizeLimitYMin(25)
                        .decoRenderer(SimpleBackgroundRenderer.GRAY)
                        .decoRenderer(GuiDecorations.BOTTOM_DARK_BORDER_LINE)
                        .apply(new HorizontalPanel()
                                .addWidget(PanelConfig.of()
                                        .trim()
                                        .apply(Label.of(Component.translatable(titleTransKey), 1.5f)))
                                .addWidget(
                                        PanelConfig.of()
                                                .fixHeight(20)
                                                .fixWidth(20)
                                                .tooltip("let_me_see_see.gui.open_config_file")
                                                .apply(iconButton(btn -> Util.getPlatform().openFile(FMLPaths.CONFIGDIR.get().resolve("let_me_see_see-common.toml").toFile()),
                                                                ResourceLocation.withDefaultNamespace("icon/search"))))))
                .addWidget(PanelConfig.of(1, 0.92f)
                        .sizeLimitYMax(height - 40)
                        .decoRenderer(GuiDecorations.BOTTOM_DARK_BORDER_LINE)
                        .apply(AutoResizeWidgetWrapper.of(content)))
                .addWidget(PanelConfig.of(1, 0.04f)
                        .sizeLimitYMin(15)
                        .decoRenderer(SimpleBackgroundRenderer.GRAY)
                        .apply(new BlankWidget()));
        return screen;
    }
    
    public static void setupSimpleEditBox(EditBox editBox){
        editBox.setMaxLength(114514);
        editBox.setCanLoseFocus(false);
        editBox.scrollTo(0);
    }
    
    public static AutoResizeWidgetWrapper iconButton(Button.OnPress onPress,ResourceLocation sprite){
        var btn = SpriteIconButton.builder(Component.empty(), onPress, true)
                .sprite(sprite, 16, 16)
                .build();
        return AutoResizeWidgetWrapper.of(btn);
    }
}
