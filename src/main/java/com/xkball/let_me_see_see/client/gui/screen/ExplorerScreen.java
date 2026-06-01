package com.xkball.let_me_see_see.client.gui.screen;

import com.xkball.let_me_see_see.client.gui.xkwidget.ClassTreeModel;
import com.xkball.let_me_see_see.common.data.ExportsDataManager;
import com.xkball.let_me_see_see.common.event.RebuildClassMapEvent;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.xklib.resource.ResourceLocation;
import com.xkball.xklib.ui.layout.BooleanLayoutVariable;
import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.system.GuiSystem;
import com.xkball.xklib.ui.widget.IconCheckBox;
import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.widget.ObjectInputWidget;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@EventBusSubscriber(Dist.CLIENT)
public class ExplorerScreen extends DataBaseScreen {

    private static ClassTreeModel classTree = new ClassTreeModel(ClassSearcher.classMap.values());
    private ContainerWidget treeContainer;
    private final BooleanLayoutVariable showExportedOnly = new BooleanLayoutVariable(false);

    public ExplorerScreen() {
        super();
    }

    @SubscribeEvent
    public static void onRebuildClassMap(RebuildClassMapEvent event) {
        classTree = new ClassTreeModel(ClassSearcher.classMap.values());
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

        // Exported-only checkbox row
        var checkboxRow = new ContainerWidget();
        checkboxRow.inlineStyle("flex-direction: row; size: 100% 14rpx; flex-shrink: 0; align-items: center;");
        checkboxRow.addChild(new IconCheckBox(new ResourceLocation("minecraft", "icon/arrow_down"))
                .bind(showExportedOnly)
                .inlineStyle("size: 12rpx 12rpx; flex-shrink: 0;")
                .withTooltip(IComponent.translatable("let_me_see_see.gui.data_base.show_exported_only")));
        checkboxRow.addChild(new Label(IComponent.translatable("let_me_see_see.gui.data_base.show_exported_only"))
                .inlineStyle("text-color: -1; size: auto 100%; margin-left: 2rpx; flex-shrink: 0;"));
        var guiSys = GuiSystem.INSTANCE.get();
        showExportedOnly.addCallback(v -> guiSys.submitTreeUpdate(this::refreshTree));

        treeContainer = new ContainerWidget();
        treeContainer.inlineStyle("size: 100% 100%-30rpx; flex-direction: column; overflow-y: scroll; flex-shrink: 1;");

        panel.addChild(searchInput);
        panel.addChild(checkboxRow);
        panel.addChild(treeContainer);
        return panel;
    }

    @Override
    public void refreshClassList() {
        refreshTree();
    }

    private void rebuildTree() {
        var classes = filterClasses(ClassSearcher.classMap.values());
        classTree = new ClassTreeModel(classes);
        refreshTree();
    }

    public void refreshTree() {
        if (treeContainer == null) return;
        treeContainer.clearChildren();
        classTree.addToContainer(treeContainer, this, this::refreshTree);
        treeContainer.markDirty();
    }

    private Collection<Class<?>> filterClasses(Collection<Class<?>> classes) {
        if (!showExportedOnly.get() && searchBarValue.isEmpty()) {
            return classes;
        }
        var result = new ArrayList<Class<?>>();
        for (var clazz : classes) {
            var classKey = ClassSearcher.className(clazz);
            var cleanName = classKey.substring(0, classKey.lastIndexOf('['));
            if (!searchBarValue.isEmpty() && !cleanName.startsWith(searchBarValue)) {
                continue;
            }
            if (showExportedOnly.get() && !ExportsDataManager.recordedClasses.containsKey(classKey)) {
                continue;
            }
            result.add(clazz);
        }
        return result;
    }
}
