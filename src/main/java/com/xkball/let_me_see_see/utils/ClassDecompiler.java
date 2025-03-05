package com.xkball.let_me_see_see.utils;

import com.mojang.logging.LogUtils;
import com.xkball.let_me_see_see.config.LMSConfig;
import org.slf4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ClassDecompiler {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<Path,DecompilerState> decompiledClasses = new ConcurrentHashMap<>();
    
    public static CompletableFuture<Void> decompile(Path file) {
        if (decompiledClasses.containsKey(file)) {
            return CompletableFuture.completedFuture(null);
        }
        decompiledClasses.put(file,DecompilerState.DECOMPILING);
        return CompletableFuture.runAsync(() -> {
            var srcPath = file.toAbsolutePath().toString();
            var dstPath = toResultPath(file).toString();
            var javaHome = System.getProperty("java.home");
            var process = new ProcessBuilder("java", "-jar", LMSConfig.FERN_FLOWER_PATH, LMSConfig.FERN_FLOWER_OPTION, srcPath,file.getParent().toAbsolutePath().toString());
            process.directory(new File(javaHome, "bin"));
            process.redirectErrorStream(true);
            process.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            try {
                var exitCode = process.start().waitFor();
                if(exitCode == 0 && new File(dstPath).exists()){
                    decompiledClasses.put(file,DecompilerState.SUCCESS);
                    return;
                }
            } catch (InterruptedException | IOException e) {
                LOGGER.error("can not decompile file: {}",srcPath,e);
            }
            decompiledClasses.put(file,DecompilerState.ERROR);
        });
    }
    
    public static Path toResultPath(Path file) {
        var srcPath = file.toAbsolutePath().toString();
        var dstPath = srcPath.substring(0,srcPath.length()-5) + "java";
        return Path.of(dstPath);
    }
    
    @Nullable
    public static DecompilerState getState(Path file) {
        var state = decompiledClasses.get(file);
        if(state == null){
            var dstFile = toResultPath(file).toFile();
            if(dstFile.exists()){
                decompiledClasses.put(file,DecompilerState.SUCCESS);
            }
        }
        return decompiledClasses.get(file);
    }
    
    public enum DecompilerState {
        SUCCESS,
        DECOMPILING,
        ERROR;
    }
}
