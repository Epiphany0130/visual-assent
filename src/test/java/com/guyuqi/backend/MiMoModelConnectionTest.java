package com.guyuqi.backend;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MiMoModelConnectionTest {

    @Autowired
    private ChatClient.Builder chatClientBuilder;

    @Value("${spring.ai.openai.base-url:未配置}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.completions-path:/v1/chat/completions}")
    private String completionsPath;

    @Value("${spring.ai.openai.chat.options.model:未配置}")
    private String model;

    @Value("${mimo.test.image-url:https://visual-assent-1330648838.cos.ap-beijing.myqcloud.com/public/2060612508794576897/2026-06-12_bTzgVYKPTTkRuqpN.webp}")
    private String testImageUrl;

    @Test
    @Order(1)
    void testTextModelAvailable() {
        printResolvedConfig();
        ChatClient chatClient = chatClientBuilder.build();

        String response = chatClient.prompt()
                .user("介绍一下你自己")
                .call()
                .content();

        System.out.println("========== MiMo 文本模型测试 ==========");
        System.out.println(response);

        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isBlank());
    }

    @Test
    @Order(2)
    void testVisionModelAvailable() {
        printResolvedConfig();
        ChatClient chatClient = chatClientBuilder.build();
        System.out.println("test image url: " + testImageUrl);
        Media media = new Media(MimeTypeUtils.parseMimeType(resolveMimeType(testImageUrl)), URI.create(testImageUrl));

        String response = chatClient.prompt()
                .system("""
                        你是图片颜色分析助手。请分析用户提供的图片。
                        只返回 JSON，不要返回 Markdown，不要返回解释文字。
                        JSON 格式：
                        {
                          "dominantColor": "主色中文名",
                          "dominantHex": "#RRGGBB",
                          "colors": [
                            {"name": "颜色中文名", "hex": "#RRGGBB", "ratio": 0.5}
                          ]
                        }
                        """)
                .user(userSpec -> userSpec.text("请分析这张图片的主要颜色").media(media))
                .call()
                .content();

        System.out.println("========== MiMo 视觉模型测试 ==========");
        System.out.println(response);

        Assertions.assertNotNull(response);
        Assertions.assertFalse(response.isBlank());
    }

    private void printResolvedConfig() {
        System.out.println("========== MiMo Spring AI 配置 ==========");
        System.out.println("base-url: " + baseUrl);
        System.out.println("chat.completions-path: " + completionsPath);
        System.out.println("model: " + model);
        System.out.println("expected endpoint: " + baseUrl + completionsPath);
    }

    private String resolveMimeType(String imageUrl) {
        String lowerUrl = imageUrl.toLowerCase();
        if (lowerUrl.endsWith(".jpg") || lowerUrl.endsWith(".jpeg")) {
            return MimeTypeUtils.IMAGE_JPEG_VALUE;
        }
        if (lowerUrl.endsWith(".webp")) {
            return "image/webp";
        }
        return MimeTypeUtils.IMAGE_PNG_VALUE;
    }
}
