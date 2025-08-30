package com.xkball.let_me_see_see.client.gui.screen;

import com.mojang.logging.LogUtils;
import com.xkball.let_me_see_see.antlr.java.ColoringListener;
import com.xkball.let_me_see_see.antlr.java.JavaLexer;
import com.xkball.let_me_see_see.antlr.java.JavaParser;
import com.xkball.let_me_see_see.client.gui.frame.core.HorizontalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.IPanel;
import com.xkball.let_me_see_see.client.gui.frame.core.IUpdateMarker;
import com.xkball.let_me_see_see.client.gui.frame.core.PanelConfig;
import com.xkball.let_me_see_see.client.gui.frame.core.UpdateChecker;
import com.xkball.let_me_see_see.client.gui.frame.core.VerticalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.render.GuiDecorations;
import com.xkball.let_me_see_see.client.gui.frame.core.render.SimpleBackgroundRenderer;
import com.xkball.let_me_see_see.client.gui.frame.screen.FrameScreen;
import com.xkball.let_me_see_see.client.gui.frame.widget.Label;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.AutoResizeWidgetWrapper;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.BaseContainerWidget;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.HorizontalPanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.ScrollableVHPanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.ScrollableVerticalPanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.VerticalPanel;
import com.xkball.let_me_see_see.client.gui.widget.ClassLabel;
import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import com.xkball.let_me_see_see.config.ColorMapping;
import com.xkball.let_me_see_see.config.LMSConfig;
import com.xkball.let_me_see_see.utils.ClassDecompiler;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DataBaseScreen extends FrameScreen {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Style CODE_BASE_STYLE = Style.EMPTY;
    public final UpdateChecker searchBarUpdateChecker = new UpdateChecker();
    protected String searchBarValue = "";
    
    @Nullable
    public ClassLabel lastFocused = null;
    
    public DataBaseScreen() {
        super(Component.empty());
        ClassSearcher.buildClassMap();
    }
    
    protected BaseContainerWidget createClassListView(){
        return PanelConfig.of(FrameScreen.THE_SCALE, 1)
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
                                            var clazz = ClassSearcher.classMap.get(str);
                                            if (clazz == null) continue;
                                            addWidget(labelConfig.apply(
                                                    new ClassLabel.ClassLabelInDBS(clazz,DataBaseScreen.this)));
                                        }
                                        return true;
                                    }
                                })));
    }
    
    @Override
    protected void init() {
        super.init();
        this.searchBarUpdateChecker.forceUpdate();
        var classListPanel = this.createClassListView();
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
                                .tooltip(Tooltip.create(Component.translatable("let_me_see_see.gui.retriever.rebuild_cache")))
                                .apply(iconButton((btn) -> ClassSearcher.buildClassMap(), ResourceLocation.withDefaultNamespace("icon/search"))))
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
        var screen = this.screenFrame(this.getTitleKey(), content);
        screen.setDecoRenderer(new SimpleBackgroundRenderer(0x60000000));
        screen.resize();
        this.addRenderableWidget(screen);
        this.updateScreen();
    }
    
    public String getTitleKey(){
        return "let_me_see_see.gui.data_base";
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
                lastFocused.reExport();
                return (T) config.apply(Label.ofKey("let_me_see_see.gui.data_base.preview.no_file"));
            }
            var state = ClassDecompiler.getState(classPath);
            lastFocused.updateState();
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
                var all = String.join(" \n", lines);
                LOGGER.debug("parsing class: {}",classPath);
                var formatedLines = parseJavaSrc(all);
                var config_ = PanelConfig.of().trim().paddingLeft(2);
                return (T) PanelConfig.of(1,1)
                        .apply(AutoResizeWidgetWrapper.of(
                                PanelConfig.of(1,1)
                                        .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                                        .apply(new ScrollableVHPanel()
                                                .addWidgets(formatedLines.stream().map(c -> config_.apply(Label.of(c))).toList(),false))));
            }
            else {
                assert state == ClassDecompiler.DecompilerState.ERROR;
                return (T) config.apply(Label.ofKey("let_me_see_see.gui.data_base.preview.decompile_error"));
            }
        }
    }
    
    public static List<Component> parseJavaSrc(String src){
        var lexer = new JavaLexer(CharStreams.fromString(src));
        var tokens = new CommonTokenStream(lexer);
        var parser = new JavaParser(tokens);
        var tree = parser.compilationUnit();
        var walker = new ParseTreeWalker();
        Int2ObjectMap<ColorMapping> map = new Int2ObjectOpenHashMap<>();
        var listener = new ColoringListener(map);
        walker.walk(listener,tree);
        var result = new ArrayList<Component>();
        var ctx = Component.empty();
        for(var token : tokens.getTokens()){
            if(token.getType() == JavaLexer.EOF) continue;
            var index = token.getTokenIndex();
            var text = token.getText();
            if(text.contains("\n")){
                var lt = text.lines().toList();
                for(var i = 0; i < lt.size(); i++){
                    ctx.append(Component.literal(lt.get(i)).withStyle(CODE_BASE_STYLE));
                    if(i != lt.size() - 1 || text.endsWith("\n")){
                        result.add(ctx);
                        ctx = Component.empty();
                    }
                }
            }
            else{
                if(map.containsKey(index)){
                    ctx.append(Component.literal(text).withStyle(CODE_BASE_STYLE.withColor(map.get(index).color)));
                }
                else {
                    ctx.append(Component.literal(text).withStyle(CODE_BASE_STYLE));
                }
            }
            
        }
        result.add(ctx);
        return result;
    }
    
    public String getSearchBarValue() {
        return searchBarValue;
    }
    
    public void setSearchBarValue(String searchBarValue) {
        this.searchBarValue = searchBarValue;
    }
}
