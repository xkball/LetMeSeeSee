package com.xkball.let_me_see_see.utils;

import com.xkball.let_me_see_see.LetMeSeeSee;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassSearcher {
    
    public static final Map<String,Class<?>> classMap = new HashMap<>();
    
    public static void rebuildClassMap() {
        var classes = LetMeSeeSee.INST.getAllLoadedClasses();
        for(var clazz : classes){
            classMap.put(clazz.getName(),clazz);
        }
    }
    
    public static List<Map.Entry<String,Class<?>>> search(String str) {
        var startWithList = classMap.entrySet().stream()
                .filter(e -> e.getKey().startsWith(str))
                .sorted(Map.Entry.comparingByKey())
                .toList();
        var containsList = classMap.entrySet().stream()
                .filter(e -> e.getKey().contains(str) && !startWithList.contains(e))
                .sorted(Map.Entry.comparingByKey())
                .toList();
        var result = new ArrayList<>(startWithList);
        result.addAll(containsList);
        return result;
    }
}
