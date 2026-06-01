package com.xkball.let_me_see_see.utils;

import com.mojang.logging.LogUtils;
import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.Manifest;

public class ClassDecompiler {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Path, DecompilerState> decompiledClasses = new ConcurrentHashMap<>();

    public static void clear(Path path) {
        decompiledClasses.remove(path);
    }

    public static CompletableFuture<Void> decompile(Path file) {
        if (decompiledClasses.containsKey(file)) {
            return CompletableFuture.completedFuture(null);
        }
        decompiledClasses.put(file, DecompilerState.DECOMPILING);
        return CompletableFuture.runAsync(() -> {
            var srcPath = file.toAbsolutePath().toString();
            var dstPath = toResultPath(file).toAbsolutePath();
            LOGGER.debug("Decompiling: {}", srcPath);

            var options = new HashMap<String, Object>();
            options.put("mpm", "60");

            var logger = new IFernflowerLogger() {
                @Override
                public void writeMessage(String message, Severity severity) {
                    if (severity.ordinal() >= Severity.WARN.ordinal()) {
                        LOGGER.warn("Fernflower: {}", message);
                    }
                }
                @Override
                public void writeMessage(String message, Severity severity, Throwable t) {
                    LOGGER.error("Fernflower: {}", message, t);
                }
            };

            var saver = new IResultSaver() {
                @Override public void saveFolder(String path) {}
                @Override public void copyFile(String source, String path, String entryName) {}
                @Override
                public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
                    try {
                        Files.createDirectories(dstPath.getParent());
                        Files.writeString(dstPath, content);
                    } catch (IOException e) {
                        LOGGER.error("Failed to write decompiled file: {}", dstPath, e);
                    }
                }
                @Override public void createArchive(String path, String archiveName, Manifest manifest) {}
                @Override public void saveDirEntry(String path, String archiveName, String entryName) {}
                @Override public void copyEntry(String source, String path, String archiveName, String entry) {}
                @Override public void closeArchive(String path, String archiveName) {}
                @Override
                public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
                    try {
                        Files.createDirectories(dstPath.getParent());
                        Files.writeString(dstPath, content);
                    } catch (IOException e) {
                        LOGGER.error("Failed to write decompiled file: {}", dstPath, e);
                    }
                }
            };

            var engine = new Fernflower(saver, options, logger);
            try {
                engine.addSource(new File(srcPath));
                engine.decompileContext();

                if (dstPath.toFile().exists()) {
                    LOGGER.debug("Decompile success: {}", dstPath);
                    decompiledClasses.put(file, DecompilerState.SUCCESS);
                } else {
                    LOGGER.warn("Decompile completed but output not found: {}", dstPath);
                    decompiledClasses.put(file, DecompilerState.ERROR);
                }
            } catch (Exception e) {
                LOGGER.error("Can not decompile file: {}", srcPath, e);
                decompiledClasses.put(file, DecompilerState.ERROR);
            }
        });
    }

    public static Path toResultPath(Path file) {
        var srcPath = file.toAbsolutePath().toString();
        return Path.of(srcPath.substring(0, srcPath.length() - 5) + "java");
    }

    @Nullable
    public static DecompilerState getState(Path file) {
        var state = decompiledClasses.get(file);
        if (state == null) {
            var dstFile = toResultPath(file).toFile();
            if (dstFile.exists()) {
                decompiledClasses.put(file, DecompilerState.SUCCESS);
                return DecompilerState.SUCCESS;
            }
        }
        return state;
    }

    public enum DecompilerState {
        SUCCESS,
        DECOMPILING,
        ERROR
    }
}
