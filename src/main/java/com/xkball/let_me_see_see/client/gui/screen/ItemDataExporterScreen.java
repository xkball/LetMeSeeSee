package com.xkball.let_me_see_see.client.gui.screen;

import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.client.gui.frame.core.HorizontalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.PanelConfig;
import com.xkball.let_me_see_see.client.gui.frame.core.VerticalAlign;
import com.xkball.let_me_see_see.client.gui.frame.core.render.GuiDecorations;
import com.xkball.let_me_see_see.client.gui.frame.screen.FrameScreen;
import com.xkball.let_me_see_see.client.gui.frame.widget.RawTexturePanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.SquareWidgetWrapper;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.HorizontalPanel;
import com.xkball.let_me_see_see.client.gui.frame.widget.basic.VerticalPanel;
import com.xkball.let_me_see_see.client.gui.widget.NumInputFrame;
import com.xkball.let_me_see_see.client.offscreen.OffScreenFBO;
import com.xkball.let_me_see_see.client.offscreen.OffScreenRenders;
import com.xkball.let_me_see_see.config.LMSConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.language.ClientLanguage;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import org.jetbrains.annotations.Nullable;
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

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = LetMeSeeSee.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ItemDataExporterScreen extends FrameScreen {
    
    private static final Map<CreativeModeTab, Set<Item>> CREATIVE_MODEL_TABS_ITEM_CACHE = new HashMap<>();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, ClientLanguage> LANGUAGES = new HashMap<>();
    private static final Codec<List<TagKey<Item>>> TAG_LIST_CODEC = TagKey.codec(Registries.ITEM).listOf();
    private String namespaceFilterValue = "";
    private boolean dumpPNG = false;
    @Nullable
    private Integer imageSize = 128;
    @Nullable
    private Float imageScale = 1f;
    
    public ItemDataExporterScreen() {
        super(Component.translatable("let_me_see_see.gui.item_data_exporter"));
    }
    
    public static void updateLanguageMap(ResourceManager resourceManager) {
        LOGGER.info("Updating language map");
        var map = new HashMap<String, ClientLanguage>();
        var list = new ArrayList<>(LMSConfig.EXPORT_LANG);
        if (!LMSConfig.EXPORT_LANG.contains("en_us")) list.add("en_us");
        if (!LMSConfig.EXPORT_LANG.contains("zh_cn")) list.add("zh_cn");
        for (var key : list) {
            var langInfo = Minecraft.getInstance().getLanguageManager().getLanguage(key);
            map.put(key, ClientLanguage.loadFrom(resourceManager, List.of(key), langInfo != null && langInfo.bidirectional()));
        }
        
        synchronized (LANGUAGES) {
            LANGUAGES.clear();
            LANGUAGES.putAll(map);
        }
    }
    
    @Override
    protected void init() {
        super.init();
        var centerWidthScale = 1 - THE_SCALE;
        var aConfig = PanelConfig.of(0.5f, 1).fixHeight(20).paddingTop(8);
        var leftPanel = PanelConfig.of((1 - THE_SCALE) * centerWidthScale, 1)
                .align(HorizontalAlign.LEFT, VerticalAlign.CENTER)
                .apply(new VerticalPanel()
                        .addWidget(aConfig.fork()
                                .decoRenderer(GuiDecorations.leftCenterString(Component.translatable("let_me_see_see.gui.item_data_exporter.image_size"), -1, true, 1.2f))
                                .apply(new NumInputFrame.Pow2IntInput(0, 12, 7).setValueSetter(this::setImageSize)))
                        .addWidget(aConfig.fork()
                                .decoRenderer(GuiDecorations.leftCenterString(Component.translatable("let_me_see_see.gui.item_data_exporter.item_scale"), -1, true, 1.2f))
                                .apply(new NumInputFrame.FloatInput(0, 8, 0.1f, 1, false).setValueSetter(this::setImageScale)))
                        .addWidget(PanelConfig.of(0.6f, 1)
                                .fixHeight(20)
                                .paddingTop(8)
                                .paddingBottom(10)
                                .decoRenderer(GuiDecorations.leftCenterString(Component.translatable("let_me_see_see.gui.item_data_exporter.namespace"), -1, true, 1.2f))
                                .decoRenderer(GuiDecorations.bottomLeftString(Component.translatable("let_me_see_see.gui.item_data_exporter.namespace.hint"), 11184810, true, 1))
                                .apply(createEditBox(this::getNamespaceFilterValue, this::setNamespaceFilterValue))))
                .addWidget(aConfig
                        .apply(createCheckBox(Component.translatable("let_me_see_see.gui.item_data_exporter.save_png"), this::isDumpPNG, this::setDumpPNG)))
                .addWidget(PanelConfig.of(0.3f, 1)
                        .fixHeight(20)
                        .paddingTop(8)
                        .apply(createButton(Component.translatable("let_me_see_see.gui.item_data_exporter.export"), this::runExport)))
                .addWidget(PanelConfig.of(0.3f, 1)
                        .fixHeight(20)
                        .paddingTop(8)
                        .paddingBottom(0.1f)
                        .decoRenderer(GuiDecorations.bottomLeftString(Component.translatable("let_me_see_see.gui.item_data_exporter.export_mcmod_hint"), 11184810, true, 1))
                        .apply(createButton(Component.translatable("let_me_see_see.gui.item_data_exporter.export_mcmod"), this::runExportMcMod)));
        var rightPanel = PanelConfig.of(THE_SCALE * centerWidthScale, 1)
                .align(HorizontalAlign.CENTER, VerticalAlign.CENTER)
                .apply(new VerticalPanel()
                        .addWidget(PanelConfig.of(1, 1).apply(new SquareWidgetWrapper(
                                PanelConfig.of()
                                        .decoRenderer(GuiDecorations.bottomCenterString(Component.translatable("let_me_see_see.gui.item_data_exporter.export_hint")))
                                        .apply(new RawTexturePanel(OffScreenRenders.FBO::getTextureID))))));
        var content = PanelConfig.of(1, 1)
                .align(HorizontalAlign.CENTER, VerticalAlign.TOP)
                .apply(new HorizontalPanel()
                        .addWidget(leftPanel)
                        .addWidget(rightPanel));
        var screen = screenFrame("let_me_see_see.gui.item_data_exporter", content);
        screen.resize();
        this.addRenderableWidget(screen);
        this.updateScreen();
    }
    
    @Override
    public void updateScreen() {
        super.updateScreen();
        if (imageSize != null && imageScale != null) {
            OffScreenRenders.FBO.resize(imageSize, imageSize);
            OffScreenRenders.FBO.renderOffScreen(() -> OffScreenRenders.renderItemStack(Items.CRAFTING_TABLE.getDefaultInstance(), imageSize, imageSize, imageScale));
            //OffScreenRenders.exportItemStackAsPng(Items.CRAFTING_TABLE.getDefaultInstance(),imageSize,imageSize,imageScale);
        }
        
    }
    
    public void runExport() {
        if (imageSize == null || imageScale == null) return;
        var ops = RegistryOps.create(JsonOps.INSTANCE, Objects.requireNonNull(Minecraft.getInstance().level).registryAccess());
        var map = ArrayListMultimap.<String, JsonObject>create();
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            var key = entry.getKey().location();
            var value = entry.getValue();
            var namespace = key.getNamespace();
            if (!namespaceFilterValue.isEmpty() && !namespace.equals(namespaceFilterValue)) continue;
            map.put(namespace, itemData(key, value, ops));
        }
        for (var entry : map.asMap().entrySet()) {
            var list = entry.getValue().stream().sorted(Comparator.comparing(j -> j.get("item_name").getAsString())).toList();
            var array = new JsonArray();
            list.forEach(array::add);
            var path = Path.of(LetMeSeeSee.EXPORT_DIR_PATH, entry.getKey() + ".json");
            try {
                Files.createDirectories(path.getParent());
                Files.writeString(path, GsonHelper.toStableString(array));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        this.setNeedUpdate();
    }
    
    public void runExportMcMod() {
        if (imageSize == null || imageScale == null) return;
        rebuildCreativeModeTabsItemCache();
        var ops = RegistryOps.create(JsonOps.INSTANCE, Objects.requireNonNull(Minecraft.getInstance().level).registryAccess());
        var map = ArrayListMultimap.<String, JsonObject>create();
        var bigFBO = new OffScreenFBO(128, 128);
        var smallFBO = new OffScreenFBO(32, 32);
        for (var entry : BuiltInRegistries.ITEM.entrySet()) {
            var itemID = entry.getKey().location();
            var item = entry.getValue();
            var namespace = itemID.getNamespace();
            if (!namespaceFilterValue.isEmpty() && !namespace.equals(namespaceFilterValue)) continue;
            var json = itemDataMcMod(itemID, item, ops);
            json.addProperty("smallIcon", OffScreenRenders.exportItemStackAsPng(smallFBO, item.getDefaultInstance(), 1, false));
            json.addProperty("largeIcon", OffScreenRenders.exportItemStackAsPng(bigFBO, item.getDefaultInstance(), 1, false));
            map.put(namespace, json);
        }
        for (var entry : map.asMap().entrySet()) {
            var list = entry.getValue().stream().sorted(Comparator.comparing(j -> j.get("registerName").getAsString())).toList();
            var path = Path.of(LetMeSeeSee.EXPORT_DIR_PATH, entry.getKey() + ".json");
            StringBuilder str = new StringBuilder();
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
        this.setNeedUpdate();
    }
    
    public JsonObject itemData(ResourceLocation id, Item item, DynamicOps<JsonElement> ops) {
        LOGGER.debug("Exporting {} ", id);
        assert imageSize != null && imageScale != null;
        var defaultMaxStackSize = item.getDefaultMaxStackSize();
        var canRepair = item.canRepair;
        @SuppressWarnings("deprecation")
        var craftRemainItem = item.getCraftingRemainingItem();
        var defaultComponent = item.getDefaultInstance().getComponents();
        var tags = item.getDefaultInstance().getTags().toList();
        var result = new JsonObject();
        result.addProperty("item_name", id.toString());
        result.addProperty("default_max_stack_size", defaultMaxStackSize);
        result.addProperty("can_repair", canRepair);
        result.addProperty("craftRemainItem", craftRemainItem == null ? null : BuiltInRegistries.ITEM.getKey(craftRemainItem).toString());
        result.addProperty("item_image", OffScreenRenders.exportItemStackAsPng(item.getDefaultInstance(), imageSize, imageSize, imageScale, dumpPNG));
        addDataResult(result, "default_component", () -> DataComponentMap.CODEC.encodeStart(ops, defaultComponent), "ERROR WHEN ENCODING");
        addDataResult(result, "tags", () -> TAG_LIST_CODEC.encodeStart(ops, tags), "ERROR WHEN ENCODING");
        for (var key : LMSConfig.EXPORT_LANG) {
            result.addProperty(key + "_name", LANGUAGES.get(key).getOrDefault(item.getDescriptionId()));
        }
        return result;
    }
    
    public JsonObject itemDataMcMod(ResourceLocation id, Item item, DynamicOps<JsonElement> ops) {
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
        var tags = item.getDefaultInstance().getTags().toList();
        StringBuilder oreDic = new StringBuilder("[");
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
        CreativeModeTabs.tryRebuildTabContents(Minecraft.getInstance().player.connection.enabledFeatures(), true, Minecraft.getInstance().level.registryAccess());
        var map = new HashMap<CreativeModeTab, Set<Item>>();
        for (var tab : BuiltInRegistries.CREATIVE_MODE_TAB.stream().toList()) {
            map.put(tab, tab.getSearchTabDisplayItems().stream().map(ItemStack::getItem).collect(Collectors.toSet()));
        }
        CREATIVE_MODEL_TABS_ITEM_CACHE.clear();
        CREATIVE_MODEL_TABS_ITEM_CACHE.putAll(map);
    }
    
    
    public static Optional<CreativeModeTab> getCreativeModeTab(Item item) {
        return CREATIVE_MODEL_TABS_ITEM_CACHE.entrySet().stream().filter(entry -> entry.getValue().contains(item)).map(Map.Entry::getKey).findFirst();
    }
    
    public static void addDataResult(JsonObject jsonObject, String key, Supplier<DataResult<JsonElement>> resultSupplier, String errorMessage) {
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
    
    public String getNamespaceFilterValue() {
        return namespaceFilterValue;
    }
    
    public void setNamespaceFilterValue(String namespaceFilterValue) {
        this.namespaceFilterValue = namespaceFilterValue;
    }
    
    public boolean isDumpPNG() {
        return dumpPNG;
    }
    
    public void setDumpPNG(boolean dumpPNG) {
        this.dumpPNG = dumpPNG;
    }
    
    public @Nullable Integer getImageSize() {
        return imageSize;
    }
    
    public void setImageSize(@Nullable Integer imageSize) {
        this.setNeedUpdate();
        this.imageSize = imageSize;
    }
    
    public @Nullable Float getImageScale() {
        return imageScale;
    }
    
    public void setImageScale(@Nullable Float imageScale) {
        this.setNeedUpdate();
        this.imageScale = imageScale;
    }
    
    @SubscribeEvent
    public static void onConfigReload(ModConfigEvent.Reloading event) {
        updateLanguageMap(Minecraft.getInstance().getResourceManager());
    }
    
    @SubscribeEvent
    public static void onResourceReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) ItemDataExporterScreen::updateLanguageMap);
    }
    
}
