package com.xkball.let_me_see_see.client.gui.screen;

import com.mojang.logging.LogUtils;
import com.xkball.let_me_see_see.client.gui.frame.core.HorizontalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.IUpdateMarker;
import com.xkball.let_me_see_see.client.gui.frame.core.PanelConfig;
import com.xkball.let_me_see_see.client.gui.frame.core.UpdateChecker;
import com.xkball.let_me_see_see.client.gui.frame.core.VerticalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.render.GuiDecorations;
import com.xkball.let_me_see_see.client.gui.frame.screen.FrameScreen;
import com.xkball.let_me_see_see.client.gui.frame.widget.Label;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.AutoResizeWidgetWrapper;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.HorizontalPanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.ScrollableVerticalPanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.VerticalPanel;
import com.xkball.let_me_see_see.client.gui.widget.ClassLabel;
import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import com.xkball.let_me_see_see.config.LMSConfig;
import com.xkball.let_me_see_see.utils.ClassDecompiler;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class DataBaseScreen extends FrameScreen {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private final UpdateChecker searchBarUpdateChecker = new UpdateChecker();
    private String searchBarValue = "";
    
    @Nullable
    private ClassLabel lastFocused = null;
    
    public DataBaseScreen() {
        super(Component.empty());
        ClassSearcher.buildClassMap();
    }
    
    @Override
    protected void init() {
        super.init();
        this.searchBarUpdateChecker.forceUpdate();
        var classListPanel = PanelConfig.of(FrameScreen.THE_SCALE, 1)
                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                .decoRenderer(GuiDecorations.RIGHT_DARK_BORDER_LINE)
                .apply(new VerticalPanel()
                        .addWidget(PanelConfig.of(1, 1)
                                .fixHeight(24)
                                .apply(createEditBox(this::getSearchBarValue, this::setSearchBarValue)))
                        .addWidget(PanelConfig.of(1, 1)
                                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                                .apply(new ScrollableVerticalPanel() {
                                    @Override
                                    public boolean update(IUpdateMarker marker) {
                                        if (!searchBarUpdateChecker.checkUpdate(searchBarValue)) return false;
                                        clearWidget();
                                        var labelConfig = PanelConfig.of().fixHeight(16).fixWidth(getBoundary().inner().width() - 6);
                                        for (var str : VanillaUtils.searchInLowerCase(searchBarValue, ExportsDataManager.recordedClasses.keySet())) {
                                            addWidget(labelConfig.apply(new ClassLabel(str, ExportsDataManager.recordedClasses.get(str)) {
                                                @Override
                                                public void setFocused(boolean focused) {
                                                    super.setFocused(focused);
                                                    if (focused) lastFocused = this;
                                                    if (!focused && this.equals(lastFocused)) lastFocused = null;
                                                    setNeedUpdate();
                                                }
                                            }));
                                        }
                                        return true;
                                    }
                                })));
        var openIDEATooltip = Component.translatable("let_me_see_see.gui.data_base.open_in_idea");
        if (LMSConfig.IDEA_PATH.isEmpty())
            openIDEATooltip.append(Component.translatable("let_me_see_see.gui.data_base.no_idea").withStyle(ChatFormatting.RED));
        var classPreviewHeader = PanelConfig.of(1, 1)
                .fixHeight(24)
                .align(HorizontalAlign.RIGHT, VerticalAlign.CENTER)
                .decoRenderer(GuiDecorations.BOTTOM_DARK_BORDER_LINE)
                .apply(new HorizontalPanel()
                        .addWidget(PanelConfig.of()
                                .fixSize(20, 20)
                                .paddingRight(4)
                                .tooltip(Tooltip.create(openIDEATooltip))
                                .apply(iconButton(btn -> {
                                    if (lastFocused != null) lastFocused.openInIDEA();
                                }, ResourceLocation.withDefaultNamespace("statistics/item_used"))))
                        .addWidget(PanelConfig.of()
                                .fixSize(20, 20)
                                .paddingRight(4)
                                .tooltip("let_me_see_see.gui.data_base.re_export")
                                .apply(iconButton(btn -> {
                                    if (lastFocused != null) {
                                        lastFocused.reExport();
                                        searchBarUpdateChecker.forceUpdate();
                                        setNeedUpdate();
                                    }
                                }, ResourceLocation.withDefaultNamespace("icon/search")))));
        var classPreviewPanel = PanelConfig.of(1, 1)
                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                .apply(new VerticalPanel() {
                    @Override
                    public boolean update(IUpdateMarker marker) {
                        clearWidget();
                        if (lastFocused != null) {
                            addWidget(classPreviewHeader);
                            addWidget(buildClassPreviewPanelBody());
                        }
                        return true;
                    }
                });
        
        var content = PanelConfig.of()
                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                .apply(new HorizontalPanel()
                        .addWidget(classListPanel)
                        .addWidget(classPreviewPanel));
        var screen = this.screenFrame("let_me_see_see.gui.data_base", content);
        screen.resize();
        this.addRenderableWidget(screen);
        this.updateScreen();
    }
    
    @SuppressWarnings("unchecked")
    public <T extends AbstractWidget & IPanel> T buildClassPreviewPanelBody(){
        var config = PanelConfig.of(1, 1)
                .paddingTop(0.4f)
                .trim();
        if(LMSConfig.FERN_FLOWER_PATH.isEmpty()){
            return (T) config.apply(Label.ofKey("let_me_see_see.gui.data_base.preview.no_fernflower"));
        }
        else if(this.lastFocused == null){
            return (T) config.apply(Label.ofKey("let_me_see_see.gui.data_base.preview.no_focused"));
        }
        else {
            var classPath = this.lastFocused.getClassPath();
            if(!classPath.toFile().exists()){
                return (T) config.apply(Label.ofKey("let_me_see_see.gui.data_base.preview.no_file"));
            }
            var state = ClassDecompiler.getState(classPath);
            if(state == null || state == ClassDecompiler.DecompilerState.DECOMPILING){
                if(state == null){
                    ClassDecompiler.decompile(classPath).whenCompleteAsync((v,t) -> {
                        if(t != null){
                            LOGGER.error("can not decompile file: {}",classPath,t);
                        }
                        this.setNeedUpdate();
                    });
                }
                return (T) config.apply(Label.ofKey("let_me_see_see.gui.data_base.preview.decompiling"));
            }
            else if(state == ClassDecompiler.DecompilerState.SUCCESS){
                List<String> lines = new ArrayList<>();
                var dstPath = ClassDecompiler.toResultPath(classPath);
                if(dstPath.toFile().exists()){
                    try {
                        lines = Files.readAllLines(dstPath);
                    } catch (IOException e) {
                        LOGGER.error("can not read file: {}",dstPath,e);
                    }
                }
                var config_ = PanelConfig.of().trim().paddingLeft(2);
                return (T) PanelConfig.of(1,1)
                        .apply(AutoResizeWidgetWrapper.of(
                                PanelConfig.of(1,1)
                                        .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                                        .apply(new ScrollableVerticalPanel()
                                                .addWidgets(lines.stream().map(str -> config_.apply(Label.of(str))).toList(),false))));
            }
            else {
                assert state == ClassDecompiler.DecompilerState.ERROR;
                return (T) config.apply(Label.ofKey("let_me_see_see.gui.data_base.preview.decompile_error"));
            }
        }
    }
    
    public String getSearchBarValue() {
        return searchBarValue;
    }
    
    public void setSearchBarValue(String searchBarValue) {
        this.searchBarValue = searchBarValue;
    }
}
