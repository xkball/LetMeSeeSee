package com.xkball.let_me_see_see.client.gui.screen;

import com.xkball.let_me_see_see.client.gui.frame.core.HorizontalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.IUpdateMarker;
import com.xkball.let_me_see_see.client.gui.frame.core.PanelConfig;
import com.xkball.let_me_see_see.client.gui.frame.core.VerticalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.render.GuiDecorations;
import com.xkball.let_me_see_see.client.gui.frame.screen.FrameScreen;
import com.xkball.let_me_see_see.client.gui.frame.widget.ClassTree;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.BaseContainerWidget;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.ScrollableVHPanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.VerticalPanel;
import com.xkball.let_me_see_see.common.event.RebuildClassMapEvent;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(Dist.CLIENT)
public class ExplorerScreen extends DataBaseScreen {
    
    private static ClassTree classTree = new ClassTree(ClassSearcher.classMap.values());
    public ExplorerScreen() {
        super();
    }
    
    @SubscribeEvent
    public static void onRebuildClassMap(RebuildClassMapEvent event) {
        classTree = new ClassTree(ClassSearcher.classMap.values());
        if(Minecraft.getInstance().screen instanceof ExplorerScreen explorerScreen) {
            explorerScreen.searchBarUpdateChecker.forceUpdate();
        }
    }
    
    @Override
    public String getTitleKey() {
        return "let_me_see_see.gui.explorer";
    }
    
    @Override
    protected BaseContainerWidget createClassListView() {
        return PanelConfig.of(FrameScreen.THE_SCALE-0.08F, 1)
                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                .decoRenderer(GuiDecorations.RIGHT_DARK_BORDER_LINE)
                .apply(new VerticalPanel()
                        .addWidget(PanelConfig.of(1, 1)
                                .fixHeight(24)
                                .apply(createEditBox(this::getSearchBarValue, this::setSearchBarValue)))
                        .addWidget(PanelConfig.of(1, 1)
                                .align(HorizontalAlign.LEFT, VerticalAlign.TOP)
                                .apply(new ScrollableVHPanel() {
                                    @Override
                                    public boolean update(IUpdateMarker marker) {
                                        if (!searchBarUpdateChecker.checkUpdate(searchBarValue)) return false;
                                        clearWidget();
                                        classTree.addToPanel(this,ExplorerScreen.this);
                                        super.update(marker);
                                        return true;
                                    }
                                })));
    }
}
