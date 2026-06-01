package com.xkball.let_me_see_see.client.gui.screen;

import com.mojang.logging.LogUtils;
import com.xkball.let_me_see_see.antlr.java.ColoringListener;
import com.xkball.let_me_see_see.antlr.java.JavaLexer;
import com.xkball.let_me_see_see.antlr.java.JavaParser;
import com.xkball.let_me_see_see.client.gui.xkwidget.ClassLabelWidget;
import com.xkball.xklib.ui.layout.BooleanLayoutVariable;
import com.xkball.xklib.ui.system.GuiSystem;
import net.minecraft.network.chat.TextColor;
import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import com.xkball.let_me_see_see.config.LMSConfig;
import com.xkball.let_me_see_see.utils.ClassDecompiler;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import com.xkball.xklib.resource.ResourceLocation;
import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.IconButton;
import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.widget.ObjectInputWidget;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DataBaseScreen extends XKLibScreen {

    private static final Logger LOGGER = LogUtils.getLogger();
protected String searchBarValue = "";
    @Nullable
    public ClassLabelWidget lastFocused;

    private ContainerWidget classListContainer;
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
        this.addScreenLayer(com.xkball.xklibmc.ui.XKLibBaseScreen.biPanelFrame(
                IComponent.translatable(getTitleKey()), leftPanel, rightPanel));
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
        classListContainer.inlineStyle("size: 100% 100%-16rpx; flex-direction: column; overflow-y: scroll; flex-shrink: 1;");

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
            if (lastFocused != null) openInIDEA(lastFocused);
        });
        openInIdeBtn.inlineStyle("size: 14rpx 14rpx; margin-left: 2rpx; flex-shrink: 0;");
        if (LMSConfig.IDEA_PATH.isEmpty()) {
            openInIdeBtn.withTooltip(IComponent.translatable("let_me_see_see.gui.data_base.no_idea"));
        } else {
            openInIdeBtn.withTooltip(IComponent.translatable("let_me_see_see.gui.data_base.open_in_idea"));
        }

        var reExportBtn = new IconButton(new ResourceLocation("minecraft", "icon/search"), () -> {
            if (lastFocused != null) {
                reExport(lastFocused);
                refreshPreview();
            }
        });
        reExportBtn.inlineStyle("size: 14rpx 14rpx; margin-left: 2rpx; flex-shrink: 0;")
                .withTooltip(IComponent.translatable("let_me_see_see.gui.data_base.re_export"));

        header.addChild(searchIconBtn);
        header.addChild(openInIdeBtn);
        header.addChild(reExportBtn);

        panel.addChild(header);

        previewBody = new ContainerWidget();
        previewBody.inlineStyle("size: 100% 100%-18rpx; flex-direction: column; overflow-y: scroll; flex-shrink: 1;");
        panel.addChild(previewBody);

        refreshPreview();
        return panel;
    }

    public void refreshPreview() {
        previewBody.clearChildren();

        if (LMSConfig.FERN_FLOWER_PATH.isEmpty()) {
            previewBody.addChild(new Label(IComponent.translatable("let_me_see_see.gui.data_base.preview.no_fernflower"))
                    .inlineStyle("text-color: -1; margin: 4rpx; size: 100% auto; flex-shrink: 0;"));
        } else if (lastFocused == null) {
            previewBody.addChild(new Label(IComponent.translatable("let_me_see_see.gui.data_base.preview.no_focused"))
                    .inlineStyle("text-color: -1; margin: 4rpx; size: 100% auto; flex-shrink: 0;"));
        } else {
            var classPath = getClassPath(lastFocused);
            if (!classPath.toFile().exists()) {
                reExport(lastFocused);
                previewBody.addChild(new Label(IComponent.translatable("let_me_see_see.gui.data_base.preview.no_file"))
                        .inlineStyle("text-color: -1; margin: 4rpx; size: 100% auto; flex-shrink: 0;"));
            } else {
                var state = ClassDecompiler.getState(classPath);
                lastFocused.updateState();
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
                                        size: auto 10rpx;
                                        flex-shrink: 0;
                                        text-height: 9rpx;
                                        text-drop-shadow: false;
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
        return Path.of(com.xkball.let_me_see_see.LetMeSeeSee.EXPORT_DIR_PATH,
                label.className.substring(0, label.className.lastIndexOf('['))
                        .replace('.', java.io.File.separatorChar) + ".class");
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

    protected void reExport(ClassLabelWidget label) {
        ClassDecompiler.clear(getClassPath(label));
        com.xkball.let_me_see_see.LetMeSeeSee.scanClasses(label.clazz);
    }

    public static List<IComponent> parseJavaSrc(String src) {
        var lexer = new JavaLexer(CharStreams.fromString(src));
        var tokens = new CommonTokenStream(lexer);
        var parser = new JavaParser(tokens);
        var tree = parser.compilationUnit();
        var walker = new ParseTreeWalker();
        Int2ObjectMap<com.xkball.let_me_see_see.config.ColorMapping> map = new Int2ObjectOpenHashMap<>();
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
                line = line.append(IComponent.literal(text).withColor(color));
            }
        }
        if (!line.visit().isEmpty()) {
            result.add(line);
        }
        return result;
    }
}
