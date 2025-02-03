package com.xkball.let_me_see_see.client.gui.screen;

import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.client.gui.frame.core.HorizontalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.PanelConfig;
import com.xkball.let_me_see_see.client.gui.frame.core.VerticalAlign;
import com.xkball.let_me_see_see.client.gui.frame.screen.FrameScreen;
import com.xkball.let_me_see_see.client.gui.frame.widget.Label;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.AutoResizeWidgetWrapper;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.HorizontalPanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.VerticalPanel;
import com.xkball.let_me_see_see.client.gui.widget.ClassSearchResultPanel;
import com.xkball.let_me_see_see.test.ClassSearcherTest;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class RetrieverScreen extends FrameScreen {
    
    @Nullable
    public EditBox searchBar;
    @Nullable
    public ClassSearchResultPanel classSearchResultPanel;
    public String searchBarValue = "";
    
    public RetrieverScreen() {
        super(Component.translatable("let_me_see_see.gui.retriever"));
        ClassSearcher.buildClassMap();
        //ClassSearcherTest.test();
    }
    
    @Override
    protected void init() {
        super.init();
        searchBar = new EditBox(font, 0, 0, 0, 0, Component.empty()) {
            {
                setupSimpleEditBox(this);
                setValue(searchBarValue);
                setResponder(str -> {
                    searchBarValue = str;
                    setNeedUpdate();
                });
            }
            
            @Nullable
            @Override
            public ComponentPath nextFocusPath(FocusNavigationEvent event) {
                if (classSearchResultPanel == null || event.getVerticalDirectionForInitialFocus() != ScreenDirection.DOWN)
                    return null;
                return ComponentPath.leaf(classSearchResultPanel);
            }
        };
        var content = PanelConfig.of(1, 0.9f)
                .paddingTop(0.1f)
                .align(HorizontalAlign.CENTER, VerticalAlign.TOP)
                .apply(new HorizontalPanel()
                        .addWidget(PanelConfig.of()
                                .paddingTop(16)
                                .apply(Label.of(Component.translatable("let_me_see_see.gui.retriever.search"), 1.2f)))
                        .addWidget(PanelConfig.of(0.6f, 1)
                                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                                .apply(new VerticalPanel()
                                        .addWidget(PanelConfig.of()
                                                .trim()
                                                .apply(new Label(Component.empty(), 1, -1, true) {
                                                    @Override
                                                    public boolean update() {
                                                        var old = getMessage();
                                                        if (ClassSearcher.containsClass(searchBarValue))
                                                            setMessage(Component.empty());
                                                        else
                                                            setMessage(Component.translatable("let_me_see_see.gui.retriever.class_not_found").withStyle(ChatFormatting.RED));
                                                        return old.equals(getMessage());
                                                    }
                                                }))
                                        .addWidget(PanelConfig.of(1, 0)
                                                .fixHeight(20)
                                                .apply(AutoResizeWidgetWrapper.of(searchBar)))
                                        .addWidget(PanelConfig.of(1, 1)
                                                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                                                .apply(classSearchResultPanel = new ClassSearchResultPanel(() -> searchBarValue,
                                                        str -> {
                                                            if (searchBar != null) searchBar.setValue(str);
                                                        })
                                                ))))
                        .addWidget(PanelConfig.of()
                                .fixSize(20, 20)
                                .paddingTop(9)
                                .paddingLeft(4)
                                .tooltip("let_me_see_see.gui.retriever.export")
                                .apply(iconButton((btn) -> {
                                            if (searchBar == null) return;
                                            var value = ClassSearcher.classMap.get(searchBarValue);
                                            if (value == null) return;
                                            LetMeSeeSee.scanClasses(value);
                                        },
                                        ResourceLocation.withDefaultNamespace("icon/search"))))
                        .addWidget(PanelConfig.of()
                                .fixSize(20, 20)
                                .paddingTop(9)
                                .paddingLeft(4)
                                .tooltip(Tooltip.create(Component.translatable("let_me_see_see.gui.retriever.rebuild_cache")))
                                .apply(iconButton((btn) -> ClassSearcher.buildClassMap(), ResourceLocation.withDefaultNamespace("icon/search"))))
                
                );
        var screen = this.screenFrame("let_me_see_see.gui.retriever", content);
        screen.resize();
        this.addRenderableWidget(screen);
        this.updateScreen();
    }
    
}
