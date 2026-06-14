package com.guyuqi.backend.service.impl;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.guyuqi.backend.service.AiService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.content.Media;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * AI 服务实现类（基于 MiMO 视觉模型）
 */
@Slf4j
@Service
public class AiServiceImpl implements AiService {

    @Resource
    private ChatClient.Builder chatClientBuilder;

    private static final String SYSTEM_PROMPT = """
            你是图像分析助手。请分析用户提供的图片，同时完成图像内容描述和颜色分析。
            只返回 JSON，不要返回 Markdown，不要返回解释文字。
            JSON 格式：
            {
              "description": "对图片内容的详细描述，2-3句话",
              "tags": ["关键词1", "关键词2", "关键词3"],
              "style": "图片风格，如摄影作品、插画、海报、截图等",
              "scene": "图片场景，如自然风光、城市建筑、人物肖像等",
              "dominantColor": "主色中文名，如蓝色、红色、绿色",
              "dominantHex": "#RRGGBB 格式的主色值",
              "colors": [
                {"name": "颜色中文名", "hex": "#RRGGBB", "ratio": 0.5}
              ]
            }
            """;

    /**
     * 分析图片，返回 AI 洞察数据
     *
     * @param imageUrl 图片公网 URL
     * @return 分析结果 Map，包含 description、tags、style、scene、
     *         dominantColor、dominantHex、colorInfo、aiAnalysis
     */
    @Override
    public Map<String, String> analyzeImage(String imageUrl) {
        Map<String, String> result = new HashMap<>();
        try {
            ChatClient chatClient = chatClientBuilder.build();
            Media media = new Media(MimeTypeUtils.parseMimeType(resolveMimeType(imageUrl)),
                    URI.create(imageUrl));

            String response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(userSpec -> userSpec.text("请分析这张图片").media(media))
                    .call()
                    .content();

            if (response == null || response.isBlank()) {
                log.warn("MiMO 返回空结果, imageUrl={}", imageUrl);
                return result;
            }

            // 去除可能的 Markdown 代码块包裹
            String jsonStr = response.strip();
            if (jsonStr.startsWith("```")) {
                jsonStr = jsonStr.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("\\n?```$", "");
            }

            JSONObject json = JSONUtil.parseObj(jsonStr);

            // 图像描述相关 → aiAnalysis
            JSONObject aiAnalysis = new JSONObject();
            aiAnalysis.set("description", json.getStr("description"));
            aiAnalysis.set("tags", json.get("tags"));
            aiAnalysis.set("style", json.getStr("style"));
            aiAnalysis.set("scene", json.getStr("scene"));
            result.put("aiAnalysis", aiAnalysis.toString());

            // 颜色相关
            result.put("dominantColor", json.getStr("dominantColor"));
            result.put("dominantHex", json.getStr("dominantHex"));

            // 完整调色板 → colorInfo
            result.put("colorInfo", json.get("colors") != null
                    ? JSONUtil.toJsonStr(json.get("colors"))
                    : "[]");

        } catch (Exception e) {
            log.error("AI 图像分析失败, imageUrl={}", imageUrl, e);
        }
        return result;
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
