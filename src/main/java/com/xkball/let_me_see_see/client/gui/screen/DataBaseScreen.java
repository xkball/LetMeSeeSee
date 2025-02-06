package com.xkball.let_me_see_see.client.gui.screen;

import com.xkball.let_me_see_see.client.gui.frame.core.HorizontalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.PanelConfig;
import com.xkball.let_me_see_see.client.gui.frame.core.UpdateChecker;
import com.xkball.let_me_see_see.client.gui.frame.core.VerticalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.render.GuiDecorations;
import com.xkball.let_me_see_see.client.gui.frame.screen.FrameScreen;
import com.xkball.let_me_see_see.client.gui.frame.widget.Label;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.HorizontalPanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.ScrollableVerticalPanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.VerticalPanel;
import com.xkball.let_me_see_see.client.gui.widget.ClassLabel;
import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import com.xkball.let_me_see_see.config.LMSConfig;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class DataBaseScreen extends FrameScreen {
    
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
                                .apply(createEditBox(this::getSearchBarValue,this::setSearchBarValue)))
                        .addWidget(PanelConfig.of(1, 1)
                                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                                .apply(new ScrollableVerticalPanel() {
                                    @Override
                                    public boolean update() {
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
        var classPreviewBody = PanelConfig.of(1, 1)
                .paddingTop(0.2f)
                .trim()
                .apply(Label.of(Component.translatable("let_me_see_see.gui.data_base.preview.wip")));
        var classPreviewPanel = PanelConfig.of(1, 1)
                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                .apply(new VerticalPanel() {
                    @Override
                    public boolean update() {
                        clearWidget();
                        if (lastFocused != null) {
                            addWidget(classPreviewHeader);
                            addWidget(classPreviewBody);
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
    
    public String getSearchBarValue() {
        return searchBarValue;
    }
    
    public void setSearchBarValue(String searchBarValue) {
        this.searchBarValue = searchBarValue;
    }
}
