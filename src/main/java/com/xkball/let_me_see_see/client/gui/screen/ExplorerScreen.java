package com.xkball.let_me_see_see.client.gui.screen;

import com.mojang.logging.LogUtils;
import com.xkball.let_me_see_see.LetMeSeeSeeClient;
import com.xkball.let_me_see_see.client.gui.xkwidget.ClassTreeModel;
import com.xkball.let_me_see_see.common.event.RebuildClassMapEvent;
import com.xkball.let_me_see_see.utils.ClassDecompiler;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.xklib.resource.ResourceLocation;
import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.system.GuiSystem;
import com.xkball.xklib.ui.widget.IconButton;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.widget.ObjectInputWidget;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(Dist.CLIENT)
public class ExplorerScreen extends DataBaseScreen {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static ClassTreeModel classTree = new ClassTreeModel(ClassSearcher.classMap.values());
    private static boolean exportingAll = false;
    private ContainerWidget treeContainer;
    // 只看已导出类的功能不实用，暂时注释
    // private final BooleanLayoutVariable showExportedOnly = new BooleanLayoutVariable(false);

    public ExplorerScreen() {
        super();
    }

    @SubscribeEvent
    public static void onRebuildClassMap(RebuildClassMapEvent event) {
        var newTree = new ClassTreeModel(ClassSearcher.classMap.values());
        newTree.copyOpenStates(classTree);
        classTree = newTree;
        if (Minecraft.getInstance().screen instanceof ExplorerScreen explorerScreen) {
            explorerScreen.refreshTree();
        }
    }

    @Override
    public String getTitleKey() {
        return "let_me_see_see.gui.explorer";
    }

    @Override
    protected ContainerWidget createClassListPanel() {
        var panel = new ContainerWidget();
        panel.inlineStyle("size: 100% 100%; flex-direction: column;");

        var searchInput = ObjectInputWidget.ofString();
        searchInput.setAsString(searchBarValue);
        searchInput.setCallback(w -> {
            searchBarValue = w.getAsString();
            rebuildTree();
        });
        searchInput.inlineStyle("size: 100% 14rpx; flex-shrink: 0;");

//        // Exported-only checkbox row
//        var checkboxRow = new ContainerWidget();
//        checkboxRow.inlineStyle("flex-direction: row; size: 100% 14rpx; flex-shrink: 0; align-items: center;");
//        var mcCheckbox = Checkbox.builder(Component.empty(), Minecraft.getInstance().font)
//                .pos(0, 0)
//                .selected(showExportedOnly.get())
//                .onValueChange((cb, val) -> showExportedOnly.set(val))
//                .build();
//        var checkWrapper = new WidgetWrapper(mcCheckbox);
//        checkWrapper.setUserInput(true);
//        checkWrapper.inlineStyle("size: 12rpx 12rpx; flex-shrink: 0; margin-left: 1rpx;")
//                .withTooltip(IComponent.translatable("let_me_see_see.gui.data_base.show_exported_only"));
//        checkboxRow.addChild(checkWrapper);
//        checkboxRow.addChild(new Label(IComponent.translatable("let_me_see_see.gui.data_base.show_exported_only"))
//                .inlineStyle("text-color: -1; size: auto 100%; margin-left: 2rpx; flex-shrink: 0;"));
//        var guiSys = GuiSystem.INSTANCE.get();
//        showExportedOnly.addCallback(v -> guiSys.submitTreeUpdate(this::refreshTree));

        treeContainer = new ContainerWidget();
        treeContainer.inlineStyle("size: 100% 100%-16rpx; flex-direction: column; overflow-y: scroll; scrollbar-width: 8; flex-shrink: 1;");

        panel.addChild(searchInput);
//        panel.addChild(checkboxRow);
        panel.addChild(treeContainer);
        return panel;
    }

    @Override
    protected void addClassPreviewHeaderButtons(ContainerWidget header) {
        var exportAllBtn = new IconButton(new ResourceLocation("let_me_see_see", "missing/export_all_decompile"), this::exportAndDecompileAllLoadedClasses);
        exportAllBtn.inlineStyle("size: 14rpx 14rpx; margin-left: 2rpx; flex-shrink: 0;")
                .withTooltip(IComponent.translatable("let_me_see_see.gui.explorer.export_all_decompile"));
        header.addChild(exportAllBtn);
    }

    @Override
    public void refreshClassList() {
        refreshTree();
    }

    private void rebuildTree() {
        var classes = filterClasses(ClassSearcher.classMap.values());
        var newTree = new ClassTreeModel(classes);
        newTree.copyOpenStates(classTree);
        classTree = newTree;
        refreshTree();
    }

    public void refreshTree() {
        if (treeContainer == null) return;
        treeContainer.clearChildren();
        classTree.addToContainer(treeContainer, this, this::refreshTree);
        treeContainer.markDirty();
    }

    private Collection<Class<?>> filterClasses(Collection<Class<?>> classes) {
        if (searchBarValue.isEmpty()) {
            return classes;
        }
        return classes.stream()
                .filter(clazz -> {
                    var classKey = ClassSearcher.className(clazz);
                    var cleanName = classKey.substring(0, classKey.lastIndexOf('['));
                    return cleanName.contains(searchBarValue);
                }).toList();
    }

    private void exportAndDecompileAllLoadedClasses() {
        if (exportingAll) {
            return;
        }
        exportingAll = true;
        var guiSystem = GuiSystem.INSTANCE.get();
        var classes = ClassSearcher.classMap.values().stream()
                .filter(ClassTreeModel::isNormalClass)
                .toList();
        CompletableFuture.runAsync(() -> exportAndDecompileAllLoadedClasses(classes))
                .whenCompleteAsync((v, t) -> {
                    exportingAll = false;
                    if (t != null) {
                        LOGGER.error("Failed to export and decompile all loaded classes", t);
                    }
                    guiSystem.submitTreeUpdate(() -> {
                        refreshTree();
                        refreshPreview();
                    });
                });
    }

    private void exportAndDecompileAllLoadedClasses(Collection<Class<?>> classes) {
        LetMeSeeSeeClient.scanClasses(classes.stream().toList());
        var futures = classes.stream()
                .map(ClassSearcher::className)
                .map(this::getClassPath)
                .filter(path -> path.toFile().exists())
                .peek(ClassDecompiler::clear)
                .map(ClassDecompiler::decompile)
                .toArray(CompletableFuture<?>[]::new);
        CompletableFuture.allOf(futures).join();
    }
}
