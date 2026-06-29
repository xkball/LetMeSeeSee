package com.xkball.let_me_see_see.client.gui.screen;

import com.mojang.logging.LogUtils;
import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.antlr.java.ColoringListener;
import com.xkball.let_me_see_see.antlr.java.JavaLexer;
import com.xkball.let_me_see_see.antlr.java.JavaParser;
import com.xkball.let_me_see_see.client.gui.xkwidget.ClassLabelWidget;
import com.xkball.let_me_see_see.config.ColorMapping;
import com.xkball.xklib.ui.system.GuiSystem;
import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklibmc.ui.XKLibBaseScreen;
import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import com.xkball.let_me_see_see.config.LMSConfig;
import com.xkball.let_me_see_see.utils.ClassDecompiler;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.xklib.resource.ResourceLocation;
import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.Button;
import com.xkball.xklib.ui.widget.IconButton;
import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.widget.ObjectInputWidget;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DataBaseScreen extends XKLibScreen {

    private static final Logger LOGGER = LogUtils.getLogger();
    protected String searchBarValue = "";
    @Nullable
    protected ClassPreviewTab activeTab;

    private final List<ClassPreviewTab> openedTabs = new ArrayList<>();
    private final List<ContainerWidget> tabWidgets = new ArrayList<>();
    private ContainerWidget classListContainer;
    private ContainerWidget tabBar;
    private ContainerWidget previewBody;

    public DataBaseScreen() {
        super();
        ClassSearcher.buildClassMap();
    }

    @Override
    protected String getTitleKey() {
        return "let_me_see_see.gui.data_base";
    }

    @Override
    protected void setupFrame() {
        var leftPanel = createClassListPanel();
        var rightPanel = createClassPreviewPanel();
        this.addScreenLayer(XKLibBaseScreen.biPanelFrame(
                IComponent.translatable(getTitleKey()), leftPanel, rightPanel).inlineStyle("background-color: 0xDD030407;"));
        refreshClassList();
    }

    @Override
    protected void buildUI(ContainerWidget root) {
        // not used - setupFrame handles the frame
    }

    protected ContainerWidget createClassListPanel() {
        var panel = new ContainerWidget();
        panel.inlineStyle("size: 100% 100%; flex-direction: column;");

        var searchInput = ObjectInputWidget.ofString();
        searchInput.setAsString(searchBarValue);
        searchInput.setCallback(w -> {
            searchBarValue = w.getAsString();
            GuiSystem.INSTANCE.get().submitTreeUpdate(this::refreshClassList);
        });
        searchInput.inlineStyle("size: 100% 14rpx; flex-shrink: 0;");

        classListContainer = new ContainerWidget();
        classListContainer.inlineStyle("size: 100% 100%-16rpx; flex-direction: column; overflow-y: scroll; scrollbar-width: 8; flex-shrink: 1;");

        panel.addChild(searchInput);
        panel.addChild(classListContainer);
        return panel;
    }

    public void refreshClassList() {
        classListContainer.clearChildren();
        for (var entry : ExportsDataManager.recordedClasses.entrySet()) {
            var classKey = entry.getKey();
            var clazz = ClassSearcher.classMap.get(classKey);
            if (clazz == null) continue;

            var cleanName = classKey.substring(0, classKey.lastIndexOf('['));
            if (!searchBarValue.isEmpty() && !cleanName.startsWith(searchBarValue)) {
                continue;
            }

            classListContainer.addChild(createClassLabel(clazz)
                    .inlineStyle("size: 100% 8rpx; flex-shrink: 0; margin-top: 1rpx; text-height: 8rpx;"));
        }
        classListContainer.markDirty();
    }

    protected ClassLabelWidget createClassLabel(Class<?> clazz) {
        return new ClassLabelWidget(clazz, this);
    }

    protected ContainerWidget createClassPreviewPanel() {
        var panel = new ContainerWidget();
        panel.inlineStyle("size: 100% 100%; flex-direction: column;");

        var header = new ContainerWidget();
        header.inlineStyle("flex-direction: row; size: 100% 18rpx; flex-shrink: 0; border-bottom: 1rpx; border-color: 0x55666666; align-items: center;");

        var searchIconBtn = new IconButton(new ResourceLocation("minecraft", "icon/search"), () -> {
            ClassSearcher.buildClassMap();
            refreshClassList();
        });
        searchIconBtn.inlineStyle("size: 14rpx 14rpx; margin-left: 2rpx; flex-shrink: 0;")
                .withTooltip(IComponent.translatable("let_me_see_see.gui.retriever.rebuild_cache"));

        var openInIdeBtn = new IconButton(new ResourceLocation("minecraft", "statistics/item_used"), () -> {
            if (activeTab != null) openInIDEA(activeTab);
        });
        openInIdeBtn.inlineStyle("size: 14rpx 14rpx; margin-left: 2rpx; flex-shrink: 0;");
        if (LMSConfig.IDEA_PATH.isEmpty()) {
            openInIdeBtn.withTooltip(IComponent.translatable("let_me_see_see.gui.data_base.no_idea"));
        } else {
            openInIdeBtn.withTooltip(IComponent.translatable("let_me_see_see.gui.data_base.open_in_idea"));
        }

        var reExportBtn = new IconButton(new ResourceLocation("minecraft", "icon/search"), () -> {
            if (activeTab != null) {
                reExport(activeTab);
                refreshPreview();
            }
        });
        reExportBtn.inlineStyle("size: 14rpx 14rpx; margin-left: 2rpx; flex-shrink: 0;")
                .withTooltip(IComponent.translatable("let_me_see_see.gui.data_base.re_export"));

        header.addChild(searchIconBtn);
        header.addChild(openInIdeBtn);
        header.addChild(reExportBtn);
        addClassPreviewHeaderButtons(header);

        panel.addChild(header);

        tabBar = new ContainerWidget();
        tabBar.inlineStyle("""
                flex-direction: row;
                size: 100% 16rpx;
                flex-shrink: 0;
                overflow-x: scroll;
                overflow-y: visible;
                scrollbar-width: 4;
                align-items: center;
                border-bottom: 1rpx;
                border-color: 0x55666666;
                """);
        panel.addChild(tabBar);

        previewBody = new ContainerWidget();
        previewBody.inlineStyle("size: 100% 100%-34rpx; flex-direction: column; overflow: scroll; scrollbar-width: 8;");
        panel.addChild(previewBody);

        refreshTabBar();
        refreshPreview();
        return panel;
    }

    protected void addClassPreviewHeaderButtons(ContainerWidget header) {
    }

    public void openClassTab(ClassLabelWidget label) {
        var tab = findOpenedTab(label.className);
        var newTab = tab == null;
        if (tab == null) {
            tab = new ClassPreviewTab(label.clazz, label.className, label.classSimpleName);
            openedTabs.add(tab);
        }
        activeTab = tab;
        label.updateState();
        GuiSystem.INSTANCE.get().submitTreeUpdate(() -> {
            if (newTab) {
                refreshTabBar();
            } else {
                updateTabStates();
            }
            refreshPreview();
        });
    }

    private void activateClassTab(String className) {
        activeTab = findOpenedTab(className);
        updateTabStates();
        refreshPreview();
    }

    @Nullable
    private ClassPreviewTab findOpenedTab(String className) {
        for (var tab : openedTabs) {
            if (tab.className.equals(className)) {
                return tab;
            }
        }
        return null;
    }

    private void refreshTabBar() {
        if (tabBar == null) return;
        tabBar.clearChildren();
        tabWidgets.clear();
        for (var tab : openedTabs) {
            var tabWidget = createTabButton(tab);
            tabWidgets.add(tabWidget);
            tabBar.addChild(tabWidget);
        }
        tabBar.markDirty();
    }

    private void updateTabStates() {
        for (var i = 0; i < openedTabs.size() && i < tabWidgets.size(); i++) {
            updateTabState(openedTabs.get(i), tabWidgets.get(i));
        }
    }

    private void updateTabState(ClassPreviewTab tab, ContainerWidget tabWidget) {
        var active = tab.equals(activeTab);
        tabWidget.inlineStyle("background-color: %s;".formatted(active ? "0xAA2D405C" : "0x66333333"));
        tabWidget.markDirty();
    }

    private ContainerWidget createTabButton(ClassPreviewTab tab) {
        var active = tab.equals(activeTab);
        var state = ClassLabelWidget.State.of(tab.className);
        var tabWidget = new ContainerWidget();
        tabWidget.inlineStyle("""
                flex-direction: row;
                size: auto 12rpx;
                min-width: 24rpx;
                margin-left: 2rpx;
                flex-shrink: 0;
                align-items: center;
                background-color: %s;
                """.formatted(active ? "0xAA2D405C" : "0x66333333"));

        var button = new Button(IComponent.literal(tab.classSimpleName), () -> activateClassTab(tab.className));
        button.inlineStyle("""
                size: auto 100%;
                padding-left: 4rpx;
                padding-right: 2rpx;
                flex-shrink: 0;
                text-height: 8rpx;
                text-color: -1;
                text-scale: expand-width;
                text-drop-shadow: false;
                button-shape: rect;
                button-bg-color: 0x00000000;
                """);
        button.withTooltip(IComponent.literal(cleanClassName(tab.className)));

        var closeButton = new Button(IComponent.literal("x"), () -> closeClassTab(tab.className));
        closeButton.inlineStyle("""
                size: 8rpx 100%;
                margin-right: 2rpx;
                flex-shrink: 0;
                text-height: 7rpx;
                text-scale: expand-width;
                text-color: 0xFFAAAAAA;
                text-drop-shadow: false;
                button-shape: rect;
                button-bg-color: 0x00000000;
                """);

        tabWidget.addChild(button);
        tabWidget.addChild(closeButton);
        return tabWidget;
    }

    private void closeClassTab(String className) {
        var closingIndex = -1;
        for (var i = 0; i < openedTabs.size(); i++) {
            if (openedTabs.get(i).className.equals(className)) {
                closingIndex = i;
                break;
            }
        }
        if (closingIndex < 0) return;
        var closingActive = openedTabs.get(closingIndex).equals(activeTab);
        openedTabs.remove(closingIndex);
        if (closingIndex < tabWidgets.size()) {
            var tabWidget = tabWidgets.remove(closingIndex);
            if (tabBar != null) {
                tabBar.removeChild(tabWidget);
            }
        }
        if (closingActive) {
            if (openedTabs.isEmpty()) {
                activeTab = null;
            } else {
                activeTab = openedTabs.get(Math.min(closingIndex, openedTabs.size() - 1));
            }
        }
        updateTabStates();
        refreshPreview();
    }

    public void refreshPreview() {
        previewBody.clearChildren();

        if (activeTab == null) {
            previewBody.addChild(new Label(IComponent.translatable("let_me_see_see.gui.data_base.preview.no_focused"))
                    .inlineStyle("text-color: -1; margin: 4rpx; size: 100% auto; flex-shrink: 0;"));
        } else {
            var classPath = getClassPath(activeTab);
            if (!classPath.toFile().exists()) {
                reExport(activeTab);
                previewBody.addChild(new Label(IComponent.translatable("let_me_see_see.gui.data_base.preview.no_file"))
                        .inlineStyle("text-color: -1; margin: 4rpx; size: 100% auto; flex-shrink: 0;"));
            } else {
                var state = ClassDecompiler.getState(classPath);
                updateTabStates();
                if (state == null || state == ClassDecompiler.DecompilerState.DECOMPILING) {
                    if (state == null) {
                        var guiSystem = GuiSystem.INSTANCE.get();
                        ClassDecompiler.decompile(classPath).whenCompleteAsync((v, t) -> {
                            if (t != null) {
                                LOGGER.error("can not decompile file: {}", classPath, t);
                            }
                            guiSystem.submitTreeUpdate(this::refreshPreview);
                        });
                    }
                    previewBody.addChild(new Label(IComponent.translatable("let_me_see_see.gui.data_base.preview.decompiling"))
                            .inlineStyle("text-color: -1; margin: 4rpx; size: 100% auto; flex-shrink: 0;"));
                } else if (state == ClassDecompiler.DecompilerState.SUCCESS) {
                    List<String> lines = new ArrayList<>();
                    var dstPath = ClassDecompiler.toResultPath(classPath);
                    if (dstPath.toFile().exists()) {
                        try {
                            lines = Files.readAllLines(dstPath);
                        } catch (IOException e) {
                            LOGGER.error("can not read file: {}", dstPath, e);
                        }
                    }
                    var all = String.join(" \n", lines);
                    var parsedLines = parseJavaSrc(all);
                    for (var lineComp : parsedLines) {
                        previewBody.addChild(new Label(lineComp)
                                .inlineStyle("""
                                        width: auto;
                                        height: 10rpx;
                                        flex-shrink: 0;
                                        text-height: 9rpx;
                                        text-drop-shadow: false;
                                        text-scale: expand-width;
                                        align-self: flex-start;
                                        """));
                    }
                } else {
                    previewBody.addChild(new Label(IComponent.translatable("let_me_see_see.gui.data_base.preview.decompile_error"))
                            .inlineStyle("text-color: 0xFFFF5555; margin: 4rpx; size: 100% auto; flex-shrink: 0;"));
                }
            }
        }
        previewBody.markDirty();
    }

    protected Path getClassPath(ClassLabelWidget label) {
        return getClassPath(label.className);
    }

    protected Path getClassPath(ClassPreviewTab tab) {
        return getClassPath(tab.className);
    }

    protected Path getClassPath(String className) {
        return Path.of(LetMeSeeSee.EXPORT_DIR_PATH,
                className.substring(0, className.lastIndexOf('['))
                        .replace('.', File.separatorChar) + ".class");
    }

    protected void openInIDEA(ClassLabelWidget label) {
        var ideaPath = LMSConfig.IDEA_PATH;
        if (ideaPath.isEmpty()) return;
        var classPath = getClassPath(label).toString();
        var pb = new ProcessBuilder('"' + ideaPath + '"', classPath);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        try {
            pb.start();
        } catch (IOException e) {
            LOGGER.error("Failed to open file {}", classPath, e);
        }
    }

    protected void openInIDEA(ClassPreviewTab tab) {
        var ideaPath = LMSConfig.IDEA_PATH;
        if (ideaPath.isEmpty()) return;
        var classPath = getClassPath(tab).toString();
        var pb = new ProcessBuilder('"' + ideaPath + '"', classPath);
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        try {
            pb.start();
        } catch (IOException e) {
            LOGGER.error("Failed to open file {}", classPath, e);
        }
    }

    protected void reExport(ClassLabelWidget label) {
        ClassDecompiler.clear(getClassPath(label));
        LetMeSeeSee.scanClasses(label.clazz);
    }

    protected void reExport(ClassPreviewTab tab) {
        ClassDecompiler.clear(getClassPath(tab));
        LetMeSeeSee.scanClasses(tab.clazz);
    }

    protected String cleanClassName(String className) {
        return className.substring(0, className.lastIndexOf('['));
    }

    public static List<IComponent> parseJavaSrc(String src) {
        var lexer = new JavaLexer(CharStreams.fromString(src));
        var tokens = new CommonTokenStream(lexer);
        var parser = new JavaParser(tokens);
        var tree = parser.compilationUnit();
        var walker = new ParseTreeWalker();
        Int2ObjectMap<ColorMapping> map = new Int2ObjectOpenHashMap<>();
        var listener = new ColoringListener(map);
        walker.walk(listener, tree);
        var result = new ArrayList<IComponent>();
        IComponent line = IComponent.literal("");
        for (var token : tokens.getTokens()) {
            if (token.getType() == JavaLexer.EOF) continue;
            var index = token.getTokenIndex();
            var text = token.getText();
            int color = map.containsKey(index) ? map.get(index).color : -1;
            if (text.contains("\n")) {
                var lt = text.lines().toList();
                for (var i = 0; i < lt.size(); i++) {
                    if (!lt.get(i).isEmpty()) {
                        line = line.append(IComponent.literal(lt.get(i)));
                    }
                    if (i != lt.size() - 1 || text.endsWith("\n")) {
                        result.add(line);
                        line = IComponent.literal("");
                    }
                }
            } else {
                line = line.append(IComponent.literal(text).withColor(color).withTooltip(() -> Widget.createTooltipFactory(IComponent.literal(text)).get()));
            }
        }
        if (!line.visit().isEmpty()) {
            result.add(line);
        }
        return result;
    }

    protected record ClassPreviewTab(Class<?> clazz, String className, String classSimpleName) {
    }
}
