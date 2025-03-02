package com.xkball.let_me_see_see.utils;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.CookieManager;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

public class GoogleTranslate {
    
    //todo 写进配置文件
    private static final int MAX_RETRIES = 40;
    private static final URI THE_URI = URI.create("https://translate.google.com");
    private static final URI INTERNAL_URI = URI.create("https://translate.google.com/_/TranslateWebserverUi/data/batchexecute");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CookieManager cookieManager = new CookieManager();
    private static final AtomicInteger cookieUsed = new AtomicInteger(0);
    private static final HttpClient CLIENT = createClient();
    
    public static final String ZN_CH = "zh_cn";
    
    public static CompletableFuture<String> translate(String text, String lang) {
        var strArray = text.split("\n");
        var task = CompletableFuture.runAsync(() -> updateCookies(false))
                .thenApplyAsync((v) -> tryRunTranslate(strArray[0],lang,0));
        for(var i = 1; i < strArray.length; i++) {
            int finalI = i;
            task = task.thenApplyAsync(str -> str + "\n" + tryRunTranslate(strArray[finalI],lang,0));
        }
        task.exceptionallyAsync(t -> {
            LOGGER.error("Network error",t);
            return "Net work error.Cannot translate the text.";
        });
        return task;
    }
    
    private static synchronized void updateCookies(boolean force) {
        if (!force && !cookieManager.getCookieStore().getCookies().isEmpty()) return;
        cookieManager.getCookieStore().removeAll();
        var reqGetCookies = HttpRequest.newBuilder(THE_URI)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        try {
            var res = CLIENT.send(reqGetCookies, HttpResponse.BodyHandlers.ofString());
            //CLIENT.send(reqGetCookies, HttpResponse.BodyHandlers.ofString());
            res.body();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        cookieUsed.set(0);
    }
    
    private static String tryRunTranslate(String text, String lang, int retries) {
        if(retries > MAX_RETRIES) {
            throw new RuntimeException("Network error: exceeded maximum retry times. " + text);
        }
        if(cookieUsed.incrementAndGet() > 30){
            updateCookies(true);
        }
        var postContent = "[[[\"MkEWBc\",\"[[\\\""+text+"\\\",\\\"auto\\\",\\\""+lang+"\\\",true],[null]]\",null,\"generic\"]]]";
        var reqPost = HttpRequest.newBuilder(INTERNAL_URI)
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("f.req=" + URLEncoder.encode(postContent, StandardCharsets.UTF_8)))
                .build();
        var resPost = CLIENT.sendAsync(reqPost,HttpResponse.BodyHandlers.ofString());
        return resPost.thenApplyAsync(res -> {
            if(res.statusCode() != 200){
                LockSupport.parkNanos(100000);
                return tryRunTranslate(text,lang,retries+1);
            }
            return getTranslateResult(res.body());
        }).join();
    }
    
    private static HttpClient createClient() {
        return HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress("127.0.0.1",7890)))
                .cookieHandler(cookieManager)
                .build();
    }
    
    public static String getTranslateResult(String str){
        str = str.substring(4);
        var gson = new Gson();
        var array1 = gson.fromJson(str, JsonArray.class);
        var array2 = array1.get(0).getAsJsonArray();
        var innerStr = array2.get(2).getAsString();
        var array3 = gson.fromJson(innerStr, JsonArray.class);
        var array4 = array3.get(1).getAsJsonArray();
        var array5 = array4.get(0).getAsJsonArray();
        var array6 = array5.get(0).getAsJsonArray();
        var array7 = array6.get(5).getAsJsonArray();
        var array8 = array7.get(0).getAsJsonArray();
        return array8.get(0).getAsString();
    }
}
