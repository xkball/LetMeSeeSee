package com.xkball.let_me_see_see.test;

import com.xkball.let_me_see_see.utils.ClassSearcher;

public class ClassSearcherTest {
    
    public static Object obj;
    
    public static void test() {
        for (int i = 0; i < 200; i++) {
            obj = ClassSearcher.search("net");
            System.out.println(obj.toString().substring(100, 120));
        }
        long sum1 = 0;
        for (int i = 0; i < 1000; i++) {
            var t1 = System.nanoTime();
            obj = ClassSearcher.search("net");
            sum1 += System.nanoTime() - t1;
            System.out.println(obj.toString().substring(100, 120));
            System.out.println(i);
        }
        for (int i = 0; i < 200; i++) {
            //obj = ClassSearcher.searchNew("net");
            System.out.println(obj.toString().substring(100, 120));
        }
        long sum2 = 0;
        for (int i = 0; i < 1000; i++) {
            var t1 = System.nanoTime();
            //obj = ClassSearcher.searchNew("net");
            sum2 += System.nanoTime() - t1;
            System.out.println(obj.toString().substring(100, 120));
            System.out.println(i);
        }
        System.out.println(sum1 / 1000);
        System.out.println(sum2 / 1000);
    }
}
