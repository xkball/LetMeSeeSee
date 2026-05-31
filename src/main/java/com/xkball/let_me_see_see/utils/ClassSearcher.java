package com.xkball.let_me_see_see.utils;

import com.xkball.let_me_see_see.LetMeSeeSee;
import com.xkball.let_me_see_see.common.event.RebuildClassMapEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassSearcher {
    
    public static boolean init = false;
    public static final Map<String, Class<?>> classMap = new HashMap<>();
    
    
    public static String className(Class<?> clazz) {
        var classLoaderName = clazz.getClassLoader() == null ? "null" : clazz.getClassLoader().getName();
        return clazz.getName() + "[" + classLoaderName + "]";
    }
    
    public static void init(){
        if (!init) {
            init = true;
            buildClassMap();
        }
    }
    
    public static void buildClassMap() {
        var classes = LetMeSeeSee.getInst().getAllLoadedClasses();
        classMap.clear();
        for (var clazz : classes) {
            var className = className(clazz);
            classMap.put(className, clazz);
        }
        NeoForge.EVENT_BUS.post(new RebuildClassMapEvent());
    }
    
    public static boolean containsClass(String className) {
        return classMap.containsKey(className);
    }
    
    public static List<String> search(String str) {
        return VanillaUtils.searchInLowerCase(str.toLowerCase(), classMap.keySet());
    }
    
    public static List<Class<?>> ofClassName(String className) {
        var classes = search(className);
        for(var clazz : classes) {
            if(!clazz.substring(0,clazz.lastIndexOf('[')).equals(className)) return List.of();
        }
        var result = new ArrayList<Class<?>>();
        for(var clazz : classes) {
            result.add(classMap.get(clazz));
        }
        return result;
    }

    
    public static List<String> ofModid(String modid) {
        var mod = ModList.get().getSortedMods().stream().filter(c -> c.getModId().equals(modid)).findFirst();
        if(mod.isEmpty()) return List.of();
        var iModFile = mod.get().getModInfo().getOwningFile().getFile();
        var classNameList = new ArrayList<String>();
        var path = iModFile.getFilePath();
        try {
            if (Files.isDirectory(path)) {
                try (var stream = Files.walk(path)) {
                    stream.filter(p -> p.toString().endsWith(".class"))
                          .forEach(p -> classNameList.add(path.relativize(p).toString()));
                }
            } else {
                try (var fs = FileSystems.newFileSystem(path)) {
                    var root = fs.getPath("/");
                    try (var stream = Files.walk(root)) {
                        stream.filter(p -> p.toString().endsWith(".class"))
                              .forEach(p -> classNameList.add(p.toString().substring(1)));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return classNameList;
//        在专用服务器不可行 仍然会触发OnlyInClient类的加载
//        var clazz = Class.forName(className,false,ClassSearcher.class.getClassLoader());
    }
    
    public static void findAllClassFiles() {
        var result = new ArrayList<Path>();
        for(var path_ : LetMeSeeSee.CLASS_PATH){
            var path = Path.of(path_);
//            if(path.toFile().isFile() && path_.endsWith(".jar")){
//                try(var jar = new JarFile(path_)){
//                    jar.stream().filter(entry -> entry.getName().endsWith(".class")).forEach(entry -> result.add(Path.of(entry.getRealName())));
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
            if(path.toFile().isDirectory()){
                try {
                    //noinspection resource
                    result.addAll(Files.find(path,Integer.MAX_VALUE,(p, a) -> p.getNameCount() > 0 && p.getFileName().toString().endsWith(".class")).toList());
                    result.size();
                }catch (IOException e){
                    throw new RuntimeException(e);
                }
            }
        }
        result.size();
    }
    
    
//    public static List<?> searchOld(String str) {
//        var startWithList = classMap.entrySet().stream()
//                .filter(e -> e.getKey().startsWith(str))
//                .sorted(Map.Entry.comparingByKey())
//                .toList();
//        var containsList = classMap.entrySet().stream()
//                .filter(e -> e.getKey().contains(str) && !startWithList.contains(e))
//                .sorted(Map.Entry.comparingByKey())
//                .toList();
//        var result = new ArrayList<>(startWithList);
//        result.addAll(containsList);
//        return result.stream().map(Map.Entry::getKey)
//                .map(Component::literal)
//                .map(c -> PanelConfig.of().trim().apply(Label.of(c)))
//                .toList();
//
//    }
    
}
