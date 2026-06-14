package com.guyuqi.backend.service;

import java.util.Map;

/**
 * AI 服务接口（基于 Spring AI / MiMO 模型）
 */
public interface AiService {

    /**
     * 分析图片，返回 AI 洞察数据
     *
     * @param imageUrl 图片公网 URL
     * @return 分析结果 Map，包含 description、tags、style、scene、
     *         dominantColor、dominantHex、colorInfo、aiAnalysis
     */
    Map<String, String> analyzeImage(String imageUrl);
}
