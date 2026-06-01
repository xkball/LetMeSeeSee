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
            var dstDir = file.getParent().toAbsolutePath().toFile();

            var resultSaver = new IResultSaver() {
                @Override
                public void saveFolder(String path) {}

                @Override
                public void copyFile(String source, String path, String entryName) {}

                @Override
                public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {}

                @Override
                public void createArchive(String path, String archiveName, Manifest manifest) {}

                @Override
                public void saveDirEntry(String path, String archiveName, String entryName) {}

                @Override
                public void copyEntry(String source, String path, String archiveName, String entry) {}

                @Override
                public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
                    var outputFile = new File(dstDir, entryName);
                    try {
                        Files.createDirectories(outputFile.getParentFile().toPath());
                        Files.writeString(outputFile.toPath(), content);
                    } catch (IOException e) {
                        LOGGER.error("Failed to write decompiled class: {}", entryName, e);
                    }
                }

                @Override
                public void closeArchive(String path, String archiveName) {}
            };

            var logger = new IFernflowerLogger() {
                @Override
                public void writeMessage(String message, Severity severity) {
                    if (severity == Severity.ERROR) {
                        LOGGER.error("Fernflower: {}", message);
                    }
                }

                @Override
                public void writeMessage(String message, Severity severity, Throwable t) {
                    if (severity == Severity.ERROR) {
                        LOGGER.error("Fernflower: {}", message, t);
                    }
                }
            };

            Map<String, Object> options = Map.of("mpm", "60");
            var engine = new Fernflower(resultSaver, options, logger);
            try {
                engine.addSource(new File(srcPath));
                engine.decompileContext();
                if (dstPath.toFile().exists()) {
                    decompiledClasses.put(file, DecompilerState.SUCCESS);
                } else {
                    decompiledClasses.put(file, DecompilerState.ERROR);
                }
            } catch (Exception e) {
                LOGGER.error("Can not decompile file: {}", srcPath, e);
                decompiledClasses.put(file, DecompilerState.ERROR);
            } finally {
                engine.clearContext();
            }
        });
    }

    public static Path toResultPath(Path file) {
        var srcPath = file.toAbsolutePath().toString();
        var dstPath = srcPath.substring(0, srcPath.length() - 5) + "java";
        return Path.of(dstPath);
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
