package com.xkball.let_me_see_see.client.gui.screen;

import com.xkball.let_me_see_see.client.gui.xkwidget.ClassTreeModel;
import com.xkball.let_me_see_see.common.event.RebuildClassMapEvent;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.widget.ObjectInputWidget;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Collection;

@EventBusSubscriber(Dist.CLIENT)
public class ExplorerScreen extends DataBaseScreen {

    private static ClassTreeModel classTree = new ClassTreeModel(ClassSearcher.classMap.values());
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
}
