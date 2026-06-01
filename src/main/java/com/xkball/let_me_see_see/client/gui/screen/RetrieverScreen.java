package com.xkball.let_me_see_see.client.gui.screen;

import com.mojang.logging.LogUtils;
import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.system.GuiSystem;
import com.xkball.xklib.ui.widget.Button;
import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.widget.ObjectInputWidget;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class RetrieverScreen extends XKLibScreen {

    private static final Logger LOGGER = LogUtils.getLogger();
    private String searchBarValue = "";
    private CompletableFuture<List<String>> searchTask;
    private String lastSearches = "";

    private ObjectInputWidget<String> searchInput;
    private ContainerWidget resultList;
    private ContainerWidget statusRow;

    public RetrieverScreen() {
        super();
        ClassSearcher.buildClassMap();
    }

    @Override
    protected String getTitleKey() {
        return "let_me_see_see.gui.retriever";
    }

    @Override
    protected void buildUI(ContainerWidget root) {
        searchInput = ObjectInputWidget.ofString();
        searchInput.setAsString(searchBarValue);
        searchInput.setCallback(w -> {
            searchBarValue = w.getAsString();
            refreshResults();
        });
        searchInput.inlineStyle("size: 60% 14rpx; margin-top: 2rpx; flex-shrink: 0;");

        statusRow = new ContainerWidget();
        statusRow.inlineStyle("size: 100% 10rpx; flex-shrink: 0;");

        resultList = new ContainerWidget();
        resultList.inlineStyle("size: 60% auto; flex-direction: column; overflow-y: scroll; flex-shrink: 1; flex-grow: 1;");
        resultList.asRootStyle("""
                .result_btn {
                    size: 100% 10rpx;
                    flex-shrink: 0;
                    text-align: left;
                    text-scale: fit_to_max;
                    text-height: 8rpx;
                    button-shape: rect;
                    button-bg-color: 0x00FFFFFF;
                    text-color: -1;
                    text-drop-shadow: false;
                }
                """);

        var exportBtn = new Button(IComponent.translatable("let_me_see_see.gui.retriever.export"), () -> {
            if (searchBarValue.isEmpty()) return;
            var value = ClassSearcher.classMap.get(searchBarValue);
            if (value == null) return;
            LetMeSeeSee.scanClasses(value);
        });
        exportBtn.inlineStyle("""
                size: content 14rpx;
                margin-top: 2rpx;
                margin-left: 2rpx;
                text-align: center;
                text-scale: expand-width;
                button-shape: rect;
                button-bg-color: rgb(229,233,239);
                text-drop-shadow: false;
                text-extra-width: 2rpx;
                text-height: 8rpx;
                """);

        var rebuildBtn = new Button(IComponent.translatable("let_me_see_see.gui.retriever.rebuild_cache"), () -> {
            ClassSearcher.buildClassMap();
            refreshResults();
        });
        rebuildBtn.inlineStyle("""
                size: content 14rpx;
                margin-top: 2rpx;
                margin-left: 2rpx;
                text-align: center;
                text-scale: expand-width;
                button-shape: rect;
                button-bg-color: rgb(229,233,239);
                text-drop-shadow: false;
                text-extra-width: 2rpx;
                text-height: 8rpx;
                """);

        // Center row: search on left, buttons on right
        var centerRow = new ContainerWidget();
        centerRow.inlineStyle("flex-direction: row; size: 100% auto; align-items: flex-start; flex-shrink: 1; flex-grow: 1;");

        var searchColumn = new ContainerWidget();
        searchColumn.inlineStyle("flex-direction: column; size: 100% auto; align-items: center; flex-shrink: 1; flex-grow: 1;");
        searchColumn.addChild(searchInput);
        searchColumn.addChild(statusRow);
        searchColumn.addChild(resultList);

        var btnColumn = new ContainerWidget();
        btnColumn.inlineStyle("flex-direction: column; size: auto auto; flex-shrink: 0; margin-left: 4rpx;");
        btnColumn.addChild(exportBtn);
        btnColumn.addChild(rebuildBtn);

        centerRow.addChild(searchColumn);
        centerRow.addChild(btnColumn);

        root.addChild(new Label(IComponent.translatable("let_me_see_see.gui.retriever.search"))
                .inlineStyle("text-color: -1; size: 100% auto; margin-top: 5rpx; margin-left: 4rpx; flex-shrink: 0;"));
        root.addChild(centerRow);

        refreshResults();
    }

    private void refreshResults() {
        statusRow.clearChildren();
        resultList.clearChildren();

        if (!searchBarValue.isEmpty()) {
            if (!lastSearches.equals(searchBarValue)) {
                if (searchTask != null) searchTask.cancel(true);
                lastSearches = searchBarValue;
                var guiSystem = GuiSystem.INSTANCE.get();
                searchTask = CompletableFuture.supplyAsync(() -> ClassSearcher.search(searchBarValue));
                searchTask.thenAcceptAsync(results -> {
                    guiSystem.submitTreeUpdate(this::refreshResults);
                });
            }

            if (searchTask != null && searchTask.isDone()) {
                var results = searchTask.getNow(List.of());
                for (var str : results) {
                    resultList.addChild(new Button(IComponent.literal(str), () -> {
                        searchBarValue = str;
                        searchInput.setAsString(str);
                        refreshResults();
                    }).setCSSClassName("result_btn"));
                }
                if (results.isEmpty() && !ClassSearcher.containsClass(searchBarValue)) {
                    statusRow.addChild(new Label(IComponent.translatable("let_me_see_see.gui.retriever.class_not_found"))
                            .inlineStyle("text-color: 0xFFFF5555; size: 100% 10rpx; flex-shrink: 0;"));
                }
            } else {
                statusRow.addChild(new Label(IComponent.translatable("let_me_see_see.gui.retriever.searching"))
                        .inlineStyle("text-color: -1; size: 100% 10rpx; flex-shrink: 0;"));
            }
        }
        statusRow.markDirty();
        resultList.markDirty();
    }
}
