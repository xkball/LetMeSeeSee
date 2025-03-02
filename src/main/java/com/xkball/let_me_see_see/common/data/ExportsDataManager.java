package com.xkball.let_me_see_see.common.data;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.logging.LogUtils;
import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.utils.ClassSearcher;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ExportsDataManager {
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Path DATA_PATH = Path.of(LetMeSeeSee.EXPORT_DIR_PATH).resolve("metadata");
    public static final Path DATA_PATH_TEMP = Path.of(LetMeSeeSee.EXPORT_DIR_PATH).resolve("metadata-temp");
    public static final Path DATA_PATH_BACKUP = Path.of(LetMeSeeSee.EXPORT_DIR_PATH).resolve("metadata-backup");
    public static final Gson GSON = new Gson();
    public static final TypeToken<Map<String, ExportEnv>> TYPE_TOKEN = new TypeToken<>() {
    };
    public static final Map<String, ExportEnv> recordedClasses = new ConcurrentHashMap<>();
    private static final Set<Class<?>> allowsExportsClasses = ConcurrentHashMap.newKeySet();
    public static final Queue<Component> resultQueue = new ConcurrentLinkedQueue<>();
    
    public static ExportEnv EXPORT_ENV;
    
    public static void addExportClass(Collection<Class<?>> clazz) {
        allowsExportsClasses.addAll(clazz);
    }
    
    public static boolean canExport(Class<?> clazz) {
        return allowsExportsClasses.contains(clazz);
    }
    
    public static void finishClassExport(Class<?> clazz) {
        allowsExportsClasses.remove(clazz);
        recordedClasses.put(ClassSearcher.className(clazz), EXPORT_ENV);
        Util.ioPool().execute(ExportsDataManager::saveRecordedClasses);
    }
    
    public static void saveRecordedClasses() {
        var json = GSON.toJson(recordedClasses);
        try {
            //noinspection ResultOfMethodCallIgnored
            DATA_PATH.toFile().createNewFile();
            Files.writeString(DATA_PATH_TEMP, json);
            Util.safeReplaceFile(DATA_PATH, DATA_PATH_TEMP, DATA_PATH_BACKUP);
        } catch (IOException e) {
            LOGGER.error("Failed to write recorded classes", e);
        }
    }
    
    public static void readRecordedClasses() {
        if (!Files.exists(DATA_PATH)) return;
        try {
            var str = Files.readString(DATA_PATH);
            Map<String, ExportEnv> map = GSON.fromJson(str, TYPE_TOKEN.getType());
            for (var entry : map.entrySet()) {
                recordedClasses.putIfAbsent(entry.getKey(), entry.getValue());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read recorded classes", e);
        }
        
    }
    
    public static void sentMessages() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;
        Component message;
        while ((message = resultQueue.poll()) != null) {
            player.displayClientMessage(message,false);
        }
    }
    
    public record ExportEnv(UUID gameInstance, String modListMD5) {
    
    }
}
