package com.xkball.let_me_see_see.utils;

import com.xkball.let_me_see_see.LetMeSeeSee;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassSearcher {
    
    public static final Map<String, Class<?>> classMap = new HashMap<>();
    
    public static String className(Class<?> clazz) {
        var classLoaderName = clazz.getClassLoader() == null ? "null" : clazz.getClassLoader().getName();
        return clazz.getName() + "[" + classLoaderName + "]";
    }
    
    public static void buildClassMap() {
        var classes = LetMeSeeSee.getInst().getAllLoadedClasses();
        for (var clazz : classes) {
            classMap.put(className(clazz), clazz);
        }
    }
    
    public static boolean containsClass(String className) {
        return classMap.containsKey(className);
    }
    
    public static List<String> search(String str) {
        return VanillaUtils.search(str, classMap.keySet());
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
