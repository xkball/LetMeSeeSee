package com.xkball.let_me_see_see.common.data;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ExportsDataManager {
    private static final Set<Class<?>> recordedClasses = new HashSet<>();
    private static final Set<Class<?>> allowsExportsClasses = new HashSet<>();
    public static final Queue<Component> resultQueue = new ConcurrentLinkedQueue<>();
    
    public static void addExportClass(Collection<Class<?>> clazz) {
        allowsExportsClasses.addAll(clazz);
    }
    
    public static boolean canExport(Class<?> clazz) {
        return allowsExportsClasses.contains(clazz);
    }
    
    public static void finishClassExport(Class<?> clazz) {
        allowsExportsClasses.remove(clazz);
        recordedClasses.add(clazz);
    }
    
    public static void sentMessages() {
        var player = Minecraft.getInstance().player;
        if(player == null) return;
        Component message;
        while ((message = resultQueue.poll()) != null) {
            player.sendSystemMessage(message);
        }
    }
    
}
