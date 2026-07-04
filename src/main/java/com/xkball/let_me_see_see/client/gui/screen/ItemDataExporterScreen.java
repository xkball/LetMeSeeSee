package com.xkball.let_me_see_see.client.gui.screen;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.xkball.let_me_see_see.LetMeSeeSeeClient;
import com.xkball.let_me_see_see.client.gui.xkwidget.OffScreenPreviewWidget;
import com.xkball.let_me_see_see.client.offscreen.OffScreenRenders;
import com.xkball.let_me_see_see.config.LMSConfig;
import com.xkball.let_me_see_see.utils.VanillaUtils;
import com.xkball.xklib.ui.render.IComponent;
import com.xkball.xklib.ui.widget.Button;
import com.xkball.xklib.ui.widget.Label;
import com.xkball.xklib.ui.widget.container.ContainerWidget;
import com.xkball.xklibmc.ui.widget.ObjectInputWidget;
import com.xkball.xklibmc.ui.widget.WidgetWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = LetMeSeeSeeClient.MODID, value = Dist.CLIENT)
public class ItemDataExporterScreen extends XKLibScreen {

    @SuppressWarnings("deprecation")
    private static final Codec<List<TagKey<Item>>> TAG_LIST_CODEC = TagKey.codec(Registries.ITEM).listOf();
    private static final Map<CreativeModeTab, Set<Item>> CREATIVE_MODEL_TABS_ITEM_CACHE = new HashMap<>();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, ClientLanguage> LANGUAGES = new HashMap<>();

    private String namespaceFilterValue = "";
    private int imageSizeN = 7;
    private float imageScale = 1f;

    public ItemDataExporterScreen() {
        super();
    }

    @Override
    protected String getTitleKey() {
        return "let_me_see_see.gui.item_data_exporter";
    }

    @Override
    protected void buildUI(ContainerWidget root) {
        var imageSize = 1 << imageSizeN;

        // Left panel - centers the controls wrapper
        var leftPanel = new ContainerWidget();
        leftPanel.inlineStyle("size: 50% 100%; flex-direction: column; align-items: center; justify-content: center;");

        var controlsWrapper = new ContainerWidget();
        controlsWrapper.inlineStyle("flex-direction: column; width: 60%; flex-shrink: 0;");

        // Right panel's size label - declared early so button callbacks can reference it
        var previewSizeLabel = new Label(IComponent.literal(imageSize + " x " + imageSize));

        // Image size: label + [-] [ 128 x 128 ] [+]
        controlsWrapper.addChild(new Label(IComponent.translatable("let_me_see_see.gui.item_data_exporter.image_size"))
                .inlineStyle("text-color: -1; size: 100% 8rpx; margin-top: 4rpx; flex-shrink: 0;"));
        var sizeRow = new ContainerWidget();
        sizeRow.inlineStyle("flex-direction: row; size: 100% 14rpx; flex-shrink: 0; align-items: center; justify-content: space-between;");
        var sizeLabel = new Label(IComponent.literal(imageSize + " x " + imageSize));
        sizeLabel.inlineStyle("text-color: -1; size: auto 100%; flex-grow: 1; text-align: center; flex-shrink: 0;");
        var halfBtn = WidgetWrapper.button("-", btn -> {
            if (imageSizeN > 1) {
                imageSizeN--;
                onImageSizeChanged(sizeLabel, previewSizeLabel);
            }
        });
        halfBtn.inlineStyle("size: 20rpx 12rpx; flex-shrink: 0;");
        var doubleBtn = WidgetWrapper.button("+", btn -> {
            if (imageSizeN < 12) {
                imageSizeN++;
                onImageSizeChanged(sizeLabel, previewSizeLabel);
            }
        });
        doubleBtn.inlineStyle("size: 20rpx 12rpx; flex-shrink: 0;");
        sizeRow.addChild(halfBtn);
        sizeRow.addChild(sizeLabel);
        sizeRow.addChild(doubleBtn);
        controlsWrapper.addChild(sizeRow);

        // Scale input
        controlsWrapper.addChild(new Label(IComponent.translatable("let_me_see_see.gui.item_data_exporter.item_scale"))
                .inlineStyle("text-color: -1; size: 100% 8rpx; margin-top: 4rpx; flex-shrink: 0;"));
        var scaleInput = ObjectInputWidget.ofString();
        scaleInput.setAsString(String.valueOf(imageScale));
        scaleInput.setCallback(w -> {
            try {
                imageScale = Float.parseFloat(w.getAsString());
                submitRenderTask(this::previewRender);
            } catch (NumberFormatException ignored) {
            }
        });
        scaleInput.inlineStyle("size: 100% 14rpx; flex-shrink: 0;");
        controlsWrapper.addChild(scaleInput);

        // Namespace filter
        controlsWrapper.addChild(new Label(IComponent.translatable("let_me_see_see.gui.item_data_exporter.namespace"))
                .inlineStyle("text-color: -1; size: 100% 8rpx; margin-top: 4rpx; flex-shrink: 0;"));
        var nsInput = ObjectInputWidget.ofString();
        nsInput.setAsString(namespaceFilterValue);
        nsInput.setCallback(w -> namespaceFilterValue = w.getAsString());
        nsInput.inlineStyle("size: 100% 14rpx; flex-shrink: 0;");
        controlsWrapper.addChild(nsInput);

        // Export image button
        var exportBtn = new Button(IComponent.translatable("let_me_see_see.gui.item_data_exporter.export"),
                () -> submitRenderTask(this::runExport));
        exportBtn.inlineStyle("""
                size: 100% 14rpx;
                margin-top: 8rpx;
                text-align: center;
                text-scale: expand-width;
                button-shape: rect;
                button-bg-color: rgb(229,233,239);
                text-drop-shadow: false;
                text-extra-width: 2rpx;
                text-height: 8rpx;
                flex-shrink: 0;
                """);
        controlsWrapper.addChild(exportBtn);

        // Export mcmod button
        var mcmodBtn = new Button(IComponent.translatable("let_me_see_see.gui.item_data_exporter.export_mcmod"),
                () -> submitRenderTask(this::runExportMcMod));
        mcmodBtn.inlineStyle("""
                size: 100% 14rpx;
                margin-top: 4rpx;
                text-align: center;
                text-scale: expand-width;
                button-shape: rect;
                button-bg-color: rgb(229,233,239);
                text-drop-shadow: false;
                text-extra-width: 2rpx;
                text-height: 8rpx;
                flex-shrink: 0;
                """);
        controlsWrapper.addChild(mcmodBtn);

        leftPanel.addChild(controlsWrapper);

        // Right panel - preview
        var rightPanel = new ContainerWidget();
        rightPanel.inlineStyle("size: 50% 100%; flex-direction: column; align-items: center; justify-content: center;");

        previewSizeLabel.inlineStyle("text-color: -1; size: 100% 8rpx; flex-shrink: 0; text-align: center;");
        rightPanel.addChild(previewSizeLabel);

        var previewWidget = new OffScreenPreviewWidget();
        previewWidget.inlineStyle("size: 128rpx 128rpx; flex-shrink: 0;");
        rightPanel.addChild(previewWidget);

        rightPanel.addChild(new Label(IComponent.translatable("let_me_see_see.gui.item_data_exporter.export_hint"))
                .inlineStyle("text-color: -1; size: 100% 8rpx; margin-top: 4rpx; flex-shrink: 0; text-align: center;"));

        var content = new ContainerWidget();
        content.inlineStyle("flex-direction: row; size: 100% 100%;");
        content.addChild(leftPanel);
        content.addChild(rightPanel);

        root.addChild(content);

        submitRenderTask(this::previewRender);
    }

    private void onImageSizeChanged(Label... sizeLabels) {
        var imageSize = 1 << imageSizeN;
        var text = imageSize + " x " + imageSize;
        for (var label : sizeLabels) {
            label.setText(text);
        }
        submitRenderTask(this::previewRender);
    }

    private void previewRender() {
        var imageSize = 1 << imageSizeN;
        OffScreenRenders.renderTarget.resize(imageSize, imageSize);
        OffScreenRenders.renderItemStack(Items.CRAFTING_TABLE.getDefaultInstance(),
                OffScreenRenders.renderTarget, imageScale);
    }

    @SuppressWarnings("unused")
    private boolean filterItem(Identifier item) {
        if (namespaceFilterValue.isEmpty()) return true;
        if (namespaceFilterValue.contains(":")) {
            return item.toString().equals(namespaceFilterValue);
        }
        return item.getNamespace().equals(namespaceFilterValue);
    }

    public void runExport() {
        var imageSize = 1 << imageSizeN;
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            var item = entry.getValue();
            var rl = BuiltInRegistries.ITEM.getKey(item);
            if (!namespaceFilterValue.isEmpty() && !matchesFilter(namespaceFilterValue, rl)) continue;
            var exportPath = Path.of(LetMeSeeSeeClient.EXPORT_DIR_PATH, "_images",
                    rl.getNamespace(), rl.getPath() + ".png");
            OffScreenRenders.exportItemStackAsPng(item.getDefaultInstance(), imageSize, imageSize, imageScale, true, exportPath);
        }
    }

    private static boolean matchesFilter(String filter, Identifier rl) {
        if (filter.contains(":")) return rl.toString().equals(filter);
        return rl.getNamespace().equals(filter);
    }

    @SuppressWarnings("unused")
    public void runExportMcMod() {
        rebuildCreativeModeTabsItemCache();
        var ops = RegistryOps.create(JsonOps.INSTANCE,
                Objects.requireNonNull(Minecraft.getInstance().level).registryAccess());
        var map = ArrayListMultimap.<String, JsonObject>create();
        var bigFBO = new TextureTarget(null, 128, 128, true);
        var smallFBO = new TextureTarget(null, 32, 32, true);
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            var item = entry.getValue();
            var rl = BuiltInRegistries.ITEM.getKey(item);
            var namespace = rl.getNamespace();
            if (namespaceFilterValue.isEmpty() || matchesFilter(namespaceFilterValue, rl)) {
                var json = itemDataMcMod(rl, item, ops);
                json.addProperty("smallIcon", OffScreenRenders.exportItemStackAsPng(smallFBO, item.getDefaultInstance(), 1, false));
                json.addProperty("largeIcon", OffScreenRenders.exportItemStackAsPng(bigFBO, item.getDefaultInstance(), 1, false));
                map.put(namespace, json);
            }
        }
        for (var entry : map.asMap().entrySet()) {
            var list = entry.getValue().stream()
                    .sorted(Comparator.comparing(j -> j.get("registerName").getAsString()))
                    .toList();
            var path = Path.of(LetMeSeeSeeClient.EXPORT_DIR_PATH, entry.getKey() + ".json");
            var str = new StringBuilder();
            for (var json : list) {
                str.append(json.toString());
                str.append('\n');
            }
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, str.toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public JsonObject itemDataMcMod(Identifier id, Item item, DynamicOps<JsonElement> ops) {
        var result = new JsonObject();
        result.addProperty("name", LANGUAGES.get("zh_cn").getOrDefault(item.getDescriptionId()));
        result.addProperty("englishName", LANGUAGES.get("en_us").getOrDefault(item.getDescriptionId()));
        result.addProperty("registerName", id.toString());
        result.addProperty("type", item instanceof BlockItem ? "Block" : "Item");
        result.addProperty("maxStacksSize", item.getDefaultMaxStackSize());
        result.addProperty("maxDurability", item.getDefaultInstance().getMaxDamage());
        result.addProperty("CreativeTabName", getCreativeModeTab(item)
                .map(CreativeModeTab::getDisplayName)
                .map(Component::getString)
                .orElse("未知"));
        var tags = item.builtInRegistryHolder().tags().toList();
        var oreDic = new StringBuilder("[");
        boolean flag = false;
        for (var tag : tags) {
            flag = true;
            oreDic.append(tag.location()).append(",");
        }
        if (flag) oreDic.deleteCharAt(oreDic.length() - 1);
        oreDic.append("]");
        result.addProperty("OredictList", oreDic.toString());
        return result;
    }

    public static void rebuildCreativeModeTabsItemCache() {
        assert Minecraft.getInstance().player != null;
        assert Minecraft.getInstance().level != null;
        CreativeModeTabs.tryRebuildTabContents(
                Minecraft.getInstance().player.connection.enabledFeatures(), true,
                Minecraft.getInstance().level.registryAccess());
        var map = new HashMap<CreativeModeTab, Set<Item>>();
        for (var tab : BuiltInRegistries.CREATIVE_MODE_TAB.stream().toList()) {
            map.put(tab, tab.getSearchTabDisplayItems().stream()
                    .map(ItemStack::getItem).collect(Collectors.toSet()));
        }
        CREATIVE_MODEL_TABS_ITEM_CACHE.clear();
        CREATIVE_MODEL_TABS_ITEM_CACHE.putAll(map);
    }

    public static Optional<CreativeModeTab> getCreativeModeTab(Item item) {
        return CREATIVE_MODEL_TABS_ITEM_CACHE.entrySet().stream()
                .filter(entry -> entry.getValue().contains(item))
                .map(Map.Entry::getKey).findFirst();
    }

    public static void addDataResult(JsonObject jsonObject, String key,
                                     Supplier<DataResult<JsonElement>> resultSupplier, String errorMessage) {
        try {
            var result = resultSupplier.get();
            if (result.isSuccess()) {
                jsonObject.add(key, result.getOrThrow());
                return;
            }
        } catch (Exception e) {
            LOGGER.error("Cannot Encode Object: ", e);
        }
        jsonObject.addProperty(key, errorMessage);
    }

    public static void updateLanguageMap(ResourceManager resourceManager) {
        LOGGER.info("Updating language map");
        var map = new HashMap<String, ClientLanguage>();
        var list = new ArrayList<>(LMSConfig.EXPORT_LANG);
        if (!LMSConfig.EXPORT_LANG.contains("en_us")) list.add("en_us");
        if (!LMSConfig.EXPORT_LANG.contains("zh_cn")) list.add("zh_cn");
        for (var key : list) {
            var langInfo = Minecraft.getInstance().getLanguageManager().getLanguage(key);
            map.put(key, ClientLanguage.loadFrom(resourceManager, List.of(key),
                    langInfo != null && langInfo.bidirectional()));
        }

        synchronized (LANGUAGES) {
            LANGUAGES.clear();
            LANGUAGES.putAll(map);
        }
    }

    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        updateLanguageMap(Minecraft.getInstance().getResourceManager());
    }

    @SubscribeEvent
    public static void onResourceReload(AddClientReloadListenersEvent event) {
        event.addListener(VanillaUtils.modRL("update_language_map"),
                (ResourceManagerReloadListener) ItemDataExporterScreen::updateLanguageMap);
    }
}
