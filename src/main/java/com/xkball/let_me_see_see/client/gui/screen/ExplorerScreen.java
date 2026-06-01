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

@EventBusSubscriber(Dist.CLIENT)
public class ExplorerScreen extends DataBaseScreen {

    private static ClassTreeModel classTree = new ClassTreeModel(ClassSearcher.classMap.values());
    private ContainerWidget treeContainer;

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
            refreshTree();
        });
        searchInput.inlineStyle("size: 100% 14rpx; flex-shrink: 0;");

        treeContainer = new ContainerWidget();
        treeContainer.inlineStyle("size: 100% 100%-16rpx; flex-direction: column; overflow-y: scroll; flex-shrink: 1;");

        panel.addChild(searchInput);
        panel.addChild(treeContainer);
        return panel;
    }

    @Override
    public void refreshClassList() {
        refreshTree();
    }

    public void refreshTree() {
        if (treeContainer == null) return;
        treeContainer.clearChildren();
        classTree.addToContainer(treeContainer, this, this::refreshTree);
        treeContainer.markDirty();
    }
}
