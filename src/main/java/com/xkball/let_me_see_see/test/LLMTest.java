package com.xkball.let_me_see_see.test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

public class LLMTest {
    
    private static final String API_KEY = "sk-393f058ddc1c481ea978d31f37ef22c1";
    private static final String CONTENT_BASE = """
{
    "model": "qwen-plus",
    "messages": [
        {
            "role": "system",
            "content": "You are a helpful assistant."
        },
        {
            "role": "user",
            "content": "%s"
        }
    ]
}
""";
    private static final URI THE_URI = URI.create("https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions");
    
    public static void main(String[] args) throws IOException, InterruptedException {
        var client = HttpClient.newHttpClient();
        var content = "你好";
        System.out.println(Charset.defaultCharset().displayName());
        System.out.println("好好好");
//        var postContent = CONTENT_BASE.formatted(content);
//        var reqPost = HttpRequest.newBuilder(THE_URI)
//                .header("Authorization","Bearer "+API_KEY)
//                .header("Content-Type","application/json")
//                .POST(HttpRequest.BodyPublishers.ofString(postContent))
//                .build();
//        var res = client.send(reqPost, HttpResponse.BodyHandlers.ofString());
//        Files.writeString(Path.of("run","result.json"),res.body());
//        System.out.println(res.statusCode());
//        System.out.println(res.body());
    }
}
