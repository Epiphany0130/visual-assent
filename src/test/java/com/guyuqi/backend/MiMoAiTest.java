package com.guyuqi.backend;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;

@SpringBootTest
class MiMoAiTest {

    @Resource
    private ChatClient.Builder chatClientBuilder;

    /**
     * 测试1：纯文本调用，验证 API 连通性
     */
    @Test
    void testTextChat() {
        ChatClient chatClient = chatClientBuilder.build();
        String response = chatClient.prompt()
                .user("你好，请用一句话介绍自己")
                .call()
                .content();
        System.out.println("========== 纯文本调用结果 ==========");
        System.out.println(response);
    }

    /**
     * 测试2：多模态调用，发送图片 URL 进行分析
     */
    @Test
    void testImageAnalysis() {
        ChatClient chatClient = chatClientBuilder.build();

        // 使用一张公开的测试图片
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/PNG_transparency_demonstration_1.png/280px-PNG_transparency_demonstration_1.png";

        Media media = new Media(MimeTypeUtils.IMAGE_PNG, URI.create(imageUrl));

        String response = chatClient.prompt()
                .user(u -> u.text("请描述这张图片中包含的物体、场景和颜色，用中文回答。").media(media))
                .call()
                .content();

        System.out.println("========== 图片分析结果 ==========");
        System.out.println(response);
    }

    /**
     * 测试3：结构化输出，测试 JSON 格式返回
     */
    @Test
    void testStructuredAnalysis() {
        ChatClient chatClient = chatClientBuilder.build();

        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/4/47/PNG_transparency_demonstration_1.png/280px-PNG_transparency_demonstration_1.png";

        Media media = new Media(MimeTypeUtils.IMAGE_PNG, URI.create(imageUrl));

        String systemPrompt = """
                你是一个图片分析助手。请分析给定的图片，返回严格的JSON格式结果，不要包含任何其他文字。
                JSON格式：
                {
                  "objects": ["物体1", "物体2"],
                  "scene": "场景名称",
                  "colors": ["颜色1", "颜色2"],
                  "tags": ["标签1", "标签2"],
                  "category": "推荐分类"
                }
                """;

        String response = chatClient.prompt()
                .system(systemPrompt)
                .user(u -> u.text("请分析这张图片").media(media))
                .call()
                .content();

        System.out.println("========== 结构化分析结果 ==========");
        System.out.println(response);
    }
}
