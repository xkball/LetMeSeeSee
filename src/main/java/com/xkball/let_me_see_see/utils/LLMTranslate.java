package com.xkball.let_me_see_see.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class LLMTranslate {
    
    private static final String API_KEY = "";
    private static final String SYSTEM_PROMPT = """
            You are a machine translation engine for Minecraft in-game texts. Your task is to accurately translate Minecraft in-game texts to another language while adhering to the following rules and guidelines:
            
            1. **Context-Aware Translation**: Ensure that the translation fits within the Minecraft world, considering gameplay mechanics, items, characters, or other in-game events. Be mindful of Minecraft’s terminology, ensuring consistency with its established lore and gameplay features.
            
            2. **Formatting Codes**:
               - Minecraft in-game texts include formatting codes that alter text styling, such as `§` or `&` followed by a letter or number (e.g., `§0`, `&a`).
               - **Do NOT translate these formatting codes.** You must ignore them during translation.
               - After translating, reintegrate the formatting codes into their original positions.
            
               **Example**:
               - Original Text: Right-click to §bopen§r the §6chest§r.
               - Step 1: Remove the formatting codes: Right-click to open the chest.
               - Step 2: Translate the core text: 右键点击打开箱子。
               - Step 3: Reinsert the formatting codes: 右键点击§b打开§r§6箱子§r。
            
            3. **Consistency**: Maintain consistency across all translated text. If certain terms or phrases are used repeatedly (e.g., diamond, block), ensure that the same translated text is used each time.
            
            4. **Clarity and Accuracy**: Ensure the translated text is grammatically correct, easy to understand, and appropriate for the game’s context. Avoid translations that sound awkward or unnatural in the target language.
            
            5. **Tone and Style**: Minecraft’s in-game texts are typically casual, friendly, and engaging. Maintain a light and approachable tone that is consistent with the overall feel of the game.
            
            6. **Input and Output Format**: User Content only contains texts need translate. You should only output the translation.
            
            7. **Translate Target**: Translate the user content to Chinese.""";
    private static final String CONTENT_TEMPLE = """
{
    "model": "qwen-plus",
    "messages": [
        {
            "role": "system",
            "content": "%s"
        },
        {
            "role": "user",
            "content": "%s"
        }
    ],
    "temperature": 0
}
""";
    private static final URI THE_URI = URI.create("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions");
    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();
    
    public static CompletableFuture<String> translate(String text,/*todo[xkball]*/ String lang){
        return CompletableFuture.supplyAsync(() -> {
            var postContent = CONTENT_TEMPLE.formatted(SYSTEM_PROMPT,text);
            postContent = postContent.replace('\n',' ');
            var reqPost = HttpRequest.newBuilder(THE_URI)
                    .header("Authorization","Bearer "+API_KEY)
                    .header("Content-Type","application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(postContent))
                    .build();
            return CLIENT.sendAsync(reqPost, HttpResponse.BodyHandlers.ofString()).thenApplyAsync(
                    res -> {
                        if(res.statusCode() != 200){
                            throw new RuntimeException();
                        }
                        var jsonObj1 = GSON.fromJson(res.body(), JsonObject.class);
                        var jsonArray1 = jsonObj1.getAsJsonArray("choices");
                        var jsonObj2 = jsonArray1.get(0).getAsJsonObject();
                        var jsonObj3 = jsonObj2.getAsJsonObject("message");
                        return jsonObj3.get("content").getAsString();
                    }
            ).join();
            
        }).exceptionallyAsync(t -> {
            LOGGER.error("Network error",t);
            return "Net work error.Cannot translate the text.";
        });
    }
}
