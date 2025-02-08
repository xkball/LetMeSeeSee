package com.xkball.let_me_see_see.client.gui.frame.screen;

import com.xkball.let_me_see_see.client.gui.frame.core.HorizontalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.IUpdateMarker;
import com.xkball.let_me_see_see.client.gui.frame.core.PanelConfig;
import com.xkball.let_me_see_see.client.gui.frame.core.VerticalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.render.GuiDecorations;
import com.xkball.let_me_see_see.client.gui.frame.core.render.SimpleBackgroundRenderer;
import com.xkball.let_me_see_see.client.gui.frame.widget.BlankWidget;
import com.xkball.let_me_see_see.client.gui.frame.widget.Label;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.AutoResizeWidgetWrapper;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.HorizontalPanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.VerticalPanel;
import com.xkball.let_me_see_see.client.gui.widget.ObjectInputBox;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.loading.FMLPaths;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class FrameScreen extends Screen implements IUpdateMarker {
    
    public static final float THE_SCALE = 0.3731f;
    
    protected volatile boolean needUpdate = false;
    
    public FrameScreen(Component title) {
        super(title);
    }
    
    public void updateScreen() {
        for (var child : this.children()) {
            if (child instanceof IPanel panel) {
                panel.update(this);
            }
        }
    }
    
    @Override
    public void setNeedUpdate() {
        this.needUpdate = true;
    }
    
    @Override
    public boolean needUpdate() {
        return needUpdate;
    }
    
    @Override
    public void tick() {
        super.tick();
        if (needUpdate) {
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
    
    public AutoResizeWidgetWrapper createEditBox(Supplier<String> valueGetter, Consumer<String> valueSetter) {
        return createEditBox(() -> new EditBox(font, 0, 0, 0, 0, Component.empty()), valueGetter, valueSetter);
    }
    
    public AutoResizeWidgetWrapper createEditBox(Supplier<? extends EditBox> editBoxSupplier, Supplier<String> valueGetter, Consumer<String> valueSetter) {
        var editBox = editBoxSupplier.get();
        setupSimpleEditBox(editBox);
        editBox.setValue(valueGetter.get());
        editBox.displayPos = 0;
        editBox.setResponder(str -> {
            valueSetter.accept(str);
            setNeedUpdate();
        });
        return new AutoResizeWidgetWrapper(editBox);
    }
    
    public <T> AutoResizeWidgetWrapper createObjInputBox(Predicate<String> validator, Function<String, T> responder, Consumer<T> valueSetter) {
        var editBox = new ObjectInputBox<T>(font, 0, 0, 0, 0, Component.empty(), validator, responder);
        setupSimpleEditBox(editBox);
        editBox.setResponder(str -> {
            valueSetter.accept(editBox.get());
            setNeedUpdate();
        });
        return new AutoResizeWidgetWrapper(editBox);
    }
    
    public static void setupSimpleEditBox(EditBox editBox) {
        editBox.setMaxLength(114514);
        editBox.setCanLoseFocus(true);
        editBox.scrollTo(0);
    }
    
    public static AutoResizeWidgetWrapper iconButton(Button.OnPress onPress, ResourceLocation sprite) {
        var btn = SpriteIconButton.builder(Component.empty(), onPress, true)
                .sprite(sprite, 16, 16)
                .build();
        return AutoResizeWidgetWrapper.of(btn);
    }
    
    public static AutoResizeWidgetWrapper createCheckBox(Component message, BooleanSupplier valueGetter, BooleanConsumer valueSetter) {
        var checkBox = Checkbox.builder(message, Minecraft.getInstance().font)
                .onValueChange((c, b) -> valueSetter.accept(b))
                .build();
        if (valueGetter.getAsBoolean() != checkBox.selected()) checkBox.onPress();
        return AutoResizeWidgetWrapper.of(checkBox);
    }
    
    public static AutoResizeWidgetWrapper createButton(Component message, Runnable onPress) {
        var button = Button.builder(message, btn -> onPress.run()).build();
        return AutoResizeWidgetWrapper.of(button);
    }
}
