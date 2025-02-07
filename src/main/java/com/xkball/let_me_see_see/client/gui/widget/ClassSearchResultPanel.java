package com.xkball.let_me_see_see.client.gui.widget;

import com.mojang.logging.LogUtils;
import com.xkball.let_me_see_see.client.gui.frame.core.IUpdateMarker;
import com.xkball.let_me_see_see.client.gui.frame.core.PanelConfig;
import com.xkball.let_me_see_see.client.gui.frame.widget.Label;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.ScrollableVerticalPanel;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import net.minecraft.Util;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import javax.naming.directory.SearchResult;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ClassSearchResultPanel extends ScrollableVerticalPanel {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Supplier<String> searchesGetter;
    private final Consumer<String> searchBarSetter;
    private String lastSearches = "";
    @Nullable private CompletableFuture<List<SearchResult>> searchTask;
    
    public ClassSearchResultPanel(Supplier<String> searchesGetter, Consumer<String> searchBarSetter) {
        this.searchesGetter = searchesGetter;
        this.searchBarSetter = searchBarSetter;
        this.setMessage(Component.literal("class_search_result"));
    }
    
    @Override
    public boolean update(IUpdateMarker marker) {
        var searches = searchesGetter.get();
        if (searches.isEmpty()) {
            if (!children.isEmpty()) {
                clearWidget();
                return true;
            }
            return false;
        }
        if(!lastSearches.equals(searches)) {
            if(searchTask != null) {
                searchTask.cancel(true);
            }
            searchTask = runSearch(searches,marker);
            
        }
        if(searchTask != null && searchTask.isDone()) {
            clearWidget();
            addWidgets(searchTask.getNow(List.of()), true);
        }
        else {
            clearWidget();
            var labelConfig = PanelConfig.of().trim().fixWidth(getBoundary().inner().width() - 6);
            addWidget(labelConfig.apply(Label.of(Component.translatable("let_me_see_see.gui.retriever.searching"))),true);
        }
        return false;
    }
    
    public CompletableFuture<List<SearchResult>> runSearch(String searches, IUpdateMarker updateMarker){
        lastSearches = searches;
        var future = CompletableFuture.supplyAsync(() -> {
            var labelConfig = PanelConfig.of().trim().fixWidth(getBoundary().inner().width() - 6);
            List<SearchResult> labels = new ArrayList<>();
            int id = 0;
            for (var str : ClassSearcher.search(searches)) {
                labels.add(labelConfig.apply(new SearchResult(str, id)));
                id += 1;
            }
            updateMarker.setNeedUpdate();
            return labels;
        });
        future.exceptionallyAsync(e -> {
            LOGGER.warn("Search task cancelled: {}",searches);
            return List.of();
        });
        return future;
    }
    
    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        if (this.selected == null && !this.children.isEmpty()) {
            this.setFocused(this.children.getFirst());
        }
    }
    
    public class SearchResult extends Label {
        
        private final String str;
        private final int id;
        
        public SearchResult(String str, int id) {
            super(Component.literal(str), 1, -1, true);
            this.str = str;
            this.id = id;
        }
        
        @Override
        protected boolean isValidClickButton(int button) {
            return button == 0;
        }
        
        @Override
        protected boolean clicked(double mouseX, double mouseY) {
            if (isFocused()) {
                ClassSearchResultPanel.this.searchBarSetter.accept(str);
            }
            return isMouseOver(mouseX, mouseY);
        }
        
        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (isFocused() && keyCode == GLFW.GLFW_KEY_TAB) {
                ClassSearchResultPanel.this.searchBarSetter.accept(str);
                return true;
            }
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        
        @Nullable
        @Override
        public ComponentPath nextFocusPath(FocusNavigationEvent event) {
            var dir = event.getVerticalDirectionForInitialFocus();
            var list = ClassSearchResultPanel.this.children;
            if (dir == ScreenDirection.UP && id > 0) {
                return ComponentPath.leaf(list.get(id - 1));
            }
            if (dir == ScreenDirection.DOWN && id < list.size() - 1) {
                return ComponentPath.leaf(list.get(id + 1));
            }
            return super.nextFocusPath(event);
        }
        
    }
}
