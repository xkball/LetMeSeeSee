package com.xkball.let_me_see_see.client.gui.screen;

import com.mojang.logging.LogUtils;
import com.xkball.let_me_see_see.LetMeSeeSeeClient;
import com.xkball.let_me_see_see.antlr.java.ColoringListener;
import com.xkball.let_me_see_see.antlr.java.JavaLexer;
import com.xkball.let_me_see_see.antlr.java.JavaParser;
import com.xkball.let_me_see_see.client.gui.xkwidget.ClassLabelWidget;
import com.xkball.let_me_see_see.client.gui.xkwidget.ClassTreeModel;
import com.xkball.let_me_see_see.config.ColorMapping;
import com.xkball.xklib.XKLib;
import com.xkball.xklib.ui.system.GuiSystem;
import com.xkball.xklib.ui.widget.Widget;
import com.xkball.xklibmc.ui.XKLibBaseScreen;
import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import com.xkball.let_me_see_see.config.LMSConfig;
import com.xkball.let_me_see_see.utils.ClassDecompiler;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.xklib.resource.ResourceLocation;
import com.xkball.xklib.api.gui.input.IMouseButtonEvent;
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
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DataBaseScreen extends XKLibScreen {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final List<ClassPreviewTab> OPENED_TABS = new ArrayList<>();
    @Nullable
    private static ClassPreviewTab savedActiveTab;

    protected String searchBarValue = "";

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
            if (savedActiveTab != null) openInIDEA(savedActiveTab);
        });
        openInIdeBtn.inlineStyle("size: 14rpx 14rpx; margin-left: 2rpx; flex-shrink: 0;");
        if (LMSConfig.IDEA_PATH.isEmpty()) {
            openInIdeBtn.withTooltip(IComponent.translatable("let_me_see_see.gui.data_base.no_idea"));
        } else {
            openInIdeBtn.withTooltip(IComponent.translatable("let_me_see_see.gui.data_base.open_in_idea"));
        }

        var reExportBtn = new IconButton(new ResourceLocation("minecraft", "icon/search"), () -> {
            if (savedActiveTab != null) {
                reExport(savedActiveTab);
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
        openClassTab(label.clazz, label.className, label.classSimpleName);
        label.updateState();
    }

    private void openClassTab(Class<?> clazz, String className, String classSimpleName) {
        var tab = findOpenedTab(className);
        var newTab = tab == null;
        if (tab == null) {
            tab = new ClassPreviewTab(clazz, className, classSimpleName);
            OPENED_TABS.add(tab);
        }
        savedActiveTab = tab;
        GuiSystem.INSTANCE.get().submitTreeUpdate(() -> {
            if (newTab) {
                refreshTabBar();
            } else {
                updateTabStates();
            }
            refreshPreview();
        });
    }

    private void openClassTab(String fullClassName) {
        for (var clazz : ClassSearcher.ofClassName(fullClassName)) {
            var className = ClassSearcher.className(clazz);
            var classSimpleName = cleanClassName(className).substring(fullClassName.lastIndexOf('.') + 1);
            openClassTab(clazz, className, classSimpleName);
            return;
        }
    }

    private void activateClassTab(String className) {
        if (savedActiveTab != null && savedActiveTab.className.equals(className)) return;
        savedActiveTab = findOpenedTab(className);
        updateTabStates();
        refreshPreview();
    }

    @Nullable
    private ClassPreviewTab findOpenedTab(String className) {
        for (var tab : OPENED_TABS) {
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
        for (var tab : OPENED_TABS) {
            var tabWidget = createTabButton(tab);
            tabWidgets.add(tabWidget);
            tabBar.addChild(tabWidget);
        }
        tabBar.markDirty();
    }

    private void updateTabStates() {
        for (var i = 0; i < OPENED_TABS.size() && i < tabWidgets.size(); i++) {
            updateTabState(OPENED_TABS.get(i), tabWidgets.get(i));
        }
    }

    private void updateTabState(ClassPreviewTab tab, ContainerWidget tabWidget) {
        var active = tab.equals(savedActiveTab);
        tabWidget.inlineStyle("background-color: %s;".formatted(active ? "0xAA2D405C" : "0x66333333"));
        tabWidget.markDirty();
    }

    private ContainerWidget createTabButton(ClassPreviewTab tab) {
        var active = tab.equals(savedActiveTab);
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

        var button = new TabButton(IComponent.literal(tab.classSimpleName), () -> activateClassTab(tab.className),
                () -> closeClassTab(tab.className))
                .inlineStyle("""
                    size: auto 100%;
                    padding-left: 4rpx;
                    padding-right: 2rpx;
                    flex-shrink: 0;
                    text-height: 8rpx;
                    text-color: -1;
                    text-scale: expand-width;
                    text-drop-shadow: false;
                """)
                .withTooltip(IComponent.literal(cleanClassName(tab.className)));

        var closeButton = new Button(IComponent.literal("x"), () -> closeClassTab(tab.className))
                .inlineStyle("""
                    size: 8rpx 100%;
                    margin-right: 2rpx;
                    flex-shrink: 0;
                    text-height: 7rpx;
                    text-scale: expand-width;
                    text-color: 0xFFAAAAAA;
                    text-drop-shadow: false;
                """);

        tabWidget.addChild(button);
        tabWidget.addChild(closeButton);
        return tabWidget;
    }

    private void closeClassTab(String className) {
        var closingIndex = -1;
        for (var i = 0; i < OPENED_TABS.size(); i++) {
            if (OPENED_TABS.get(i).className.equals(className)) {
                closingIndex = i;
                break;
            }
        }
        if (closingIndex < 0) return;
        var closingActive = OPENED_TABS.get(closingIndex).equals(savedActiveTab);
        OPENED_TABS.remove(closingIndex);
        if (closingIndex < tabWidgets.size()) {
            var tabWidget = tabWidgets.remove(closingIndex);
            if (tabBar != null) {
                tabBar.removeChild(tabWidget);
            }
        }
        if (closingActive) {
            if (OPENED_TABS.isEmpty()) {
                savedActiveTab = null;
            } else {
                savedActiveTab = OPENED_TABS.get(Math.min(closingIndex, OPENED_TABS.size() - 1));
            }
        }
        updateTabStates();
        refreshPreview();
    }

    public void refreshPreview() {
        previewBody.clearChildren();

        if (savedActiveTab == null) {
            previewBody.addChild(new Label(IComponent.translatable("let_me_see_see.gui.data_base.preview.no_focused"))
                    .inlineStyle("text-color: -1; margin: 4rpx; size: 100% auto; flex-shrink: 0;"));
        } else {
            var classPath = getClassPath(savedActiveTab);
            if (!classPath.toFile().exists()) {
                reExport(savedActiveTab);
            }
            if (!classPath.toFile().exists()) {
                addPreviewMessage(IComponent.translatable("let_me_see_see.gui.data_base.preview.no_file"), -1);
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
                    addPreviewMessage(IComponent.translatable("let_me_see_see.gui.data_base.preview.decompiling"), -1);
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
                    addPreviewMessage(IComponent.translatable("let_me_see_see.gui.data_base.preview.decompile_error"), 0xFFFF5555);
                }
            }
        }
        previewBody.markDirty();
    }

    private void addPreviewMessage(IComponent message, int color) {
        previewBody.addChild(new Label(message)
                .inlineStyle("text-color: %s; margin: 4rpx; size: 100%% auto; flex-shrink: 0;".formatted(color)));
    }

    protected Path getClassPath(ClassLabelWidget label) {
        return getClassPath(label.className);
    }

    protected Path getClassPath(ClassPreviewTab tab) {
        return getClassPath(tab.className);
    }

    protected Path getClassPath(String className) {
        return Path.of(LetMeSeeSeeClient.EXPORT_DIR_PATH,
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
        LetMeSeeSeeClient.scanClasses(label.clazz);
    }

    protected void reExport(ClassPreviewTab tab) {
        ClassDecompiler.clear(getClassPath(tab));
        LetMeSeeSeeClient.scanClasses(tab.clazz);
    }

    protected String cleanClassName(String className) {
        return className.substring(0, className.lastIndexOf('['));
    }

    public List<IComponent> parseJavaSrc(String src) {
        var lexer = new JavaLexer(CharStreams.fromString(src));
        var tokens = new CommonTokenStream(lexer);
        var parser = new JavaParser(tokens);
        var tree = parser.compilationUnit();
        var walker = new ParseTreeWalker();
        Int2ObjectMap<ColorMapping> map = new Int2ObjectOpenHashMap<>();
        var listener = new ColoringListener(map);
        walker.walk(listener, tree);
        var imports = new java.util.HashMap<>(getClassTree().collectImplicitImports(listener.getPackageName()));
        imports.putAll(listener.getImports());
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
                var component = IComponent.literal(text).withColor(color);
                var fullName = imports.get(text);
                if (fullName != null) {
                    component = component.withTooltip(() -> createTooltipFactory(IComponent.literal(fullName), IComponent.translatable("let_me_see_see.gui.data_base.preview.class_tooltip.open")).get())
                            .withClickEvent(() -> {
                                if(!GuiSystem.INSTANCE.get().isCtrlDown()) return;
                                openClassTab(fullName);
                            });
                }
                line = line.append(component);
            }
        }
        if (!line.visit().isEmpty()) {
            result.add(line);
        }
        return result;
    }
    
    public static Supplier<Widget> createTooltipFactory(IComponent... text) {
        return () -> {
            var font = XKLib.RENDER_CONTEXT.get().getGUIGraphics().defaultFont();
            var wMax = 0f;
            for(var c : text){
                wMax = Math.max(wMax,font.width(c, 20f));
            }
            var result =  new ContainerWidget()
                    .inlineStyle(String.format("""
                            size: %spx %spx;
                            flex-direction: column;
                            justify-content: space-around;
                            margin-left: 6rpx;
                            margin-top: 6rpx;
                            background-color: 0xdd263136;
                            border: 2px;
                            border-color: -1;
                            """, wMax+16, text.length * 20 + 10));
            for(var c : text){
                result.addChild(
                        new Label(c).inlineStyle("""
                            size: 100% 20px;
                            flex-shrink: 0;
                            margin-left: 8px;
                            text-color: -1;
                            text-height: 20;
                            text-align: left;
                            text-drop-shadow: false;
                        """));
            }
            return new ContainerWidget()
                    .addChild(result);
        };
    }

    protected static ClassTreeModel getClassTree() {
        return new ClassTreeModel(ClassSearcher.classMap.values());
    }

    protected record ClassPreviewTab(Class<?> clazz, String className, String classSimpleName) {
    }

    private static class TabButton extends Button {

        private final Runnable middleClickCallback;

        public TabButton(IComponent text, Runnable leftClickCallback, Runnable middleClickCallback) {
            super(text, leftClickCallback);
            this.middleClickCallback = middleClickCallback;
        }

        @Override
        public boolean mouseClicked(IMouseButtonEvent event, boolean doubleClick) {
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
                middleClickCallback.run();
                return true;
            }
            if (event.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                return super.mouseClicked(event, doubleClick);
            }
            return false;
        }
    }
}

