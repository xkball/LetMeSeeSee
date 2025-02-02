package com.xkball.let_me_see_see.test;

import com.xkball.let_me_see_see.utils.ClassSearcher;

public class ClassSearcherTest {
    
    public static Object obj;
    
    public static void test() {
//        for (int i = 0; i < 20; i++) {
//            obj = ClassSearcher.searchOld("net");
//            System.out.println(obj);
//        }
//        long sum1 = 0;
//        for (int i = 0; i < 100; i++) {
//            var t1 = System.nanoTime();
//            obj = ClassSearcher.searchOld("net");
//            sum1 += System.nanoTime() - t1;
//            System.out.println(obj);
//            System.out.println(i);
//        }
        for (int i = 0; i < 20; i++) {
            obj = ClassSearcher.search("net");
            System.out.println(obj);
        }
        long sum2 = 0;
        for (int i = 0; i < 100; i++) {
            var t1 = System.nanoTime();
            obj = ClassSearcher.search("net");
            sum2 += System.nanoTime() - t1;
            System.out.println(obj);
            System.out.println(i);
        }
        //System.out.println(sum1/100);
        System.out.println(sum2 / 100);
    }
}
