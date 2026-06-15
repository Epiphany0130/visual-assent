package com.guyuqi.backend.manager.ai;

import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 腾讯混元生图 API 调用管理
 */
@Slf4j
@Component
public class TencentImageGenerateManager {

    @Value("${tencent.image.submit-url}")
    private String submitUrl;

    @Value("${tencent.image.query-url}")
    private String queryUrl;

    @Value("${tencent.image.api-key}")
    private String apiKey;

    @Value("${tencent.image.model}")
    private String model;

    /**
     * 腾讯混元支持的预设分辨率
     */
    private static final Map<String, int[]> SUPPORTED_RESOLUTIONS = Map.of(
            "768:768", new int[]{768, 768},
            "768:1024", new int[]{768, 1024},
            "1024:768", new int[]{1024, 768},
            "1024:1024", new int[]{1024, 1024},
            "720:1280", new int[]{720, 1280},
            "1280:720", new int[]{1280, 720},
            "768:1280", new int[]{768, 1280},
            "1280:768", new int[]{1280, 768}
    );

    /**
     * 根据用户请求的宽高，匹配最接近的预设分辨率
     */
    private String matchResolution(int width, int height) {
        String bestKey = "1024:1024";
        double bestDiff = Double.MAX_VALUE;
        double targetRatio = (double) width / height;
        for (Map.Entry<String, int[]> entry : SUPPORTED_RESOLUTIONS.entrySet()) {
            int[] size = entry.getValue();
            double ratio = (double) size[0] / size[1];
            double diff = Math.abs(ratio - targetRatio);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestKey = entry.getKey();
            }
        }
        return bestKey;
    }

    /**
     * 提交生图任务
     *
     * @param prompt 生图 prompt
     * @param width  图片宽度
     * @param height 图片高度
     * @return 腾讯平台返回的任务 id
     */
    public String submitTask(String prompt, int width, int height) {
        String resolution = matchResolution(width, height);
        log.info("生图尺寸映射: {}x{} -> {}", width, height, resolution);

        JSONObject body = new JSONObject();
        body.set("model", model);
        body.set("prompt", prompt);
        body.set("resolution", resolution);

        HttpResponse response = HttpRequest.post(submitUrl)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .header(Header.CONTENT_TYPE, "application/json")
                .body(body.toString())
                .timeout(10000)
                .execute();

        JSONObject result = JSONUtil.parseObj(response.body());
        log.info("生图任务提交响应: {}", result);

        // 腾讯混元 API 无 code 字段，直接返回 {id, status, ...}
        // 有 id 字段即表示提交成功
        String taskId = result.getStr("id");

        // 兼容有 code 字段的响应格式
        if (taskId == null) {
            Integer code = result.getInt("code");
            if (code != null && code != 0) {
                throw new RuntimeException("生图任务提交失败: " + result.getStr("message"));
            }
            JSONObject data = result.getJSONObject("data");
            if (data != null) {
                taskId = data.getStr("id");
            }
        }

        if (taskId == null) {
            throw new RuntimeException("生图任务提交失败: 返回数据中未找到任务 id, 响应: " + result);
        }

        return taskId;
    }

    /**
     * 查询生图任务状态
     *
     * @param taskId 腾讯平台任务 id
     * @return 查询结果 JSON，包含 status 和 url 等字段
     */
    public JSONObject queryTask(String taskId) {
        JSONObject body = new JSONObject();
        body.set("model", model);
        body.set("id", taskId);

        HttpResponse response = HttpRequest.post(queryUrl)
                .header(Header.AUTHORIZATION, "Bearer " + apiKey)
                .header(Header.CONTENT_TYPE, "application/json")
                .body(body.toString())
                .timeout(10000)
                .execute();

        JSONObject result = JSONUtil.parseObj(response.body());
        log.info("生图任务查询响应: {}", result);
        return result;
    }
}
