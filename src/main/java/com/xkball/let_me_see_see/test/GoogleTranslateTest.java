package com.xkball.let_me_see_see.test;

import com.xkball.let_me_see_see.utils.GoogleTranslate;

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
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

public class GoogleTranslateTest {
    
    private static final URI THE_URI = URI.create("https://translate.google.com");
    private static final URI INTERNAL_URI = URI.create("https://translate.google.com/_/TranslateWebserverUi/data/batchexecute");
    
    public static void main(String[] args) throws IOException, InterruptedException {
        var text = "translate \n";
        //var targetLanguage = "en_us";
        var targetLanguage = "zh-CN";
        System.out.println(translate(text, targetLanguage));
    }
    
    public static String translate(String text, String targetLanguage) throws IOException, InterruptedException {
        var cookieManager = new CookieManager();
        var client = HttpClient.newBuilder()
                .proxy(ProxySelector.of(new InetSocketAddress(7890)))
                .cookieHandler(cookieManager)
                .build();
        var reqGetCookies = HttpRequest.newBuilder(THE_URI)
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();
        var resGetCookies = client.send(reqGetCookies, HttpResponse.BodyHandlers.ofString());
        if(resGetCookies.statusCode() != 200) return "";
        var postContent = "[[[\"MkEWBc\",\"[[\\\""+text+"\\\",\\\"auto\\\",\\\""+targetLanguage+"\\\",true],[null]]\",null,\"generic\"]]]";
        var reqPost = HttpRequest.newBuilder(INTERNAL_URI)
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("f.req="+ URLEncoder.encode(postContent, StandardCharsets.UTF_8)))
                .build();
        var resPost = client.send(reqPost,HttpResponse.BodyHandlers.ofString());
        Files.writeString(Path.of("run","result.json"),resPost.body());
        return GoogleTranslate.getTranslateResult(resPost.body());
    }
    
//    public static String getTranslateResult(String str){
//        str = str.substring(4);
//        var gson = new Gson();
//        var array1 = gson.fromJson(str, JsonArray.class);
//        var array2 = array1.get(0).getAsJsonArray();
//        var innerStr = array2.get(2).getAsString();
//        var array3 = gson.fromJson(innerStr, JsonArray.class);
//        var array4 = array3.get(1).getAsJsonArray();
//        var array5 = array4.get(0).getAsJsonArray();
//        var array6 = array5.get(0).getAsJsonArray();
//        var array7 = array6.get(5).getAsJsonArray();
//        var array8 = array7.get(0).getAsJsonArray();
//        return array8.get(0).getAsString();
//    }
}
