package com.guyuqi.backend.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import com.guyuqi.backend.exception.BusinessException;
import com.guyuqi.backend.exception.ErrorCode;
import com.guyuqi.backend.manager.ai.TencentImageGenerateManager;
import com.guyuqi.backend.manager.upload.AiPictureUpload;
import com.guyuqi.backend.mapper.AiGenerateTaskMapper;
import com.guyuqi.backend.model.dto.ai.PromptOptimizeResponse;
import com.guyuqi.backend.model.dto.ai.TaskStatusResponse;
import com.guyuqi.backend.model.dto.file.UploadPictureResult;
import com.guyuqi.backend.model.entity.AiGenerateTask;
import com.guyuqi.backend.model.entity.Picture;
import com.guyuqi.backend.model.entity.User;
import com.guyuqi.backend.service.AiGenerateService;
import com.guyuqi.backend.service.PictureService;
import com.guyuqi.backend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

/**
 * AI 绘图服务实现类
 */
@Slf4j
@Service
public class AiGenerateServiceImpl implements AiGenerateService {

    @Resource
    private ChatClient.Builder chatClientBuilder;

    @Resource
    private TencentImageGenerateManager tencentImageGenerateManager;

    @Resource
    private AiGenerateTaskMapper aiGenerateTaskMapper;

    @Resource
    private AiPictureUpload aiPictureUpload;

    @Resource
    private PictureService pictureService;

    @Resource
    private UserService userService;

    private static final String SYSTEM_PROMPT = """
            你是一个专业的 AI 绘图 prompt 优化助手。你的任务是将用户简单的图片描述，优化为一段更详细、更专业的 AI 绘图 prompt。

            优化规则：
            1. 保留用户描述的核心意图和主体内容
            2. 补充画面风格（如：写实摄影、插画、3D 渲染、水彩画等）
            3. 补充光影效果和氛围（如：柔光、逆光、暖色调、冷色调等）
            4. 补充构图方式（如：特写、全景、俯瞰、对称构图等）
            5. 补充细节描述（如：材质、纹理、背景元素等）
            6. 输出纯文本，不要返回 JSON，不要返回 Markdown，不要返回解释文字
            7. 优化后的 prompt 应控制在 100-200 字以内

            示例：
            输入：科技感蓝色背景
            输出：一张具有未来科技感的蓝色抽象背景图，深蓝色渐变至亮蓝色，带有发光的几何线条和数据流光效，画面中心有微弱的光晕扩散效果，整体风格为科技感数字艺术，构图对称均衡，高清画质，适合用作宣传海报背景
            """;

    /**
     * 优化用户输入的 prompt，提升生图质量
     *
     * @param prompt 用户原始描述
     * @return 优化后的 prompt
     */
    @Override
    public PromptOptimizeResponse optimizePrompt(String prompt) {
        PromptOptimizeResponse response = new PromptOptimizeResponse();
        try {
            ChatClient chatClient = chatClientBuilder.build();

            String result = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user("请优化以下绘图描述：" + prompt)
                    .call()
                    .content();

            if (result == null || result.isBlank()) {
                log.warn("MiMO 返回空结果, prompt={}", prompt);
                response.setOptimizedPrompt(prompt);
                return response;
            }

            // 去除可能的 Markdown 代码块包裹
            String optimized = result.strip();
            if (optimized.startsWith("```")) {
                optimized = optimized.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("\\n?```$", "").strip();
            }

            response.setOptimizedPrompt(optimized);
        } catch (Exception e) {
            log.error("Prompt 优化失败, prompt={}", prompt, e);
            response.setOptimizedPrompt(prompt);
        }
        return response;
    }

    /**
     * 提交图片生成任务（异步执行）
     *
     * @param userId  用户 id
     * @param prompt  生图 prompt
     * @param width   图片宽度
     * @param height  图片高度
     * @return 任务 id
     */
    @Override
    public Long submitImageGenerateTask(Long userId, String prompt, Integer width, Integer height) {
        // 创建任务记录
        AiGenerateTask task = new AiGenerateTask();
        task.setUserId(userId);
        task.setOriginalPrompt(prompt);
        task.setOptimizedPrompt(prompt);
        task.setStatus(0); // 待生成
        task.setModelParams("{\"width\":" + width + ",\"height\":" + height + "}");
        aiGenerateTaskMapper.insert(task);

        // 异步执行生图
        CompletableFuture.runAsync(() -> doGenerateImage(task.getId()));

        return task.getId();
    }

    /**
     * 异步执行图片生成
     */
    public void doGenerateImage(Long taskId) {
        AiGenerateTask task = aiGenerateTaskMapper.selectById(taskId);
        if (task == null) {
            return;
        }

        long startTime = System.currentTimeMillis();
        try {
            // 更新为生成中
            task.setStatus(1);
            aiGenerateTaskMapper.updateById(task);

            // 提交到腾讯混元
            JSONObject modelParams = cn.hutool.json.JSONUtil.parseObj(task.getModelParams());
            int width = modelParams.getInt("width", 1024);
            int height = modelParams.getInt("height", 1024);
            String remoteId = tencentImageGenerateManager.submitTask(task.getOptimizedPrompt(), width, height);
            log.info("生图任务已提交, taskId={}, remoteId={}", taskId, remoteId);

            // 轮询等待结果（最多等待 5 分钟）
            int maxAttempts = 60;
            for (int i = 0; i < maxAttempts; i++) {
                Thread.sleep(5000); // 每 5 秒查一次

                JSONObject queryResult = tencentImageGenerateManager.queryTask(remoteId);

                String status = queryResult.getStr("status");
                log.info("生图任务轮询, remoteId={}, status={}", remoteId, status);

                if ("completed".equals(status) || "done".equals(status) || "succeed".equals(status)) {
                    // 生成成功，从 data 数组中提取图片 URL
                    String imageUrl = null;
                    Object dataObj = queryResult.get("data");
                    if (dataObj instanceof cn.hutool.json.JSONArray dataArray && !dataArray.isEmpty()) {
                        JSONObject firstItem = dataArray.getJSONObject(0);
                        imageUrl = firstItem.getStr("url");
                    } else if (dataObj instanceof JSONObject dataJson) {
                        imageUrl = dataJson.getStr("url");
                        if (imageUrl == null) imageUrl = dataJson.getStr("image_url");
                    }

                    // 下载图片到临时文件
                    File tempFile = File.createTempFile("ai_gen_", ".png");
                    try {
                        HttpUtil.downloadFile(imageUrl, tempFile);
                        // 复用上传模板：上传 COS + 图片处理 + 标签 + OCR + 颜色分析 + AI 分析
                        String uploadPathPrefix = String.format("ai_generate/%d", task.getUserId());
                        UploadPictureResult result = aiPictureUpload.uploadPicture(tempFile, uploadPathPrefix, String.valueOf(task.getUserId()));
                        // 创建 Picture 记录
                        Long pictureId = createPictureFromUploadResult(result, task.getUserId(), task.getOptimizedPrompt());
                        task.setImageUrl(result.getUrl());
                        task.setPictureId(pictureId);
                    } finally {
                        tempFile.delete();
                    }
                    task.setStatus(2); // 成功
                    task.setCostTime(System.currentTimeMillis() - startTime);
                    aiGenerateTaskMapper.updateById(task);
                    log.info("生图成功, taskId={}, 耗时={}ms, pictureId={}", taskId, task.getCostTime(), task.getPictureId());
                    return;
                } else if ("failed".equals(status) || "error".equals(status)) {
                    task.setStatus(3); // 失败
                    task.setErrorMsg(queryResult.getStr("message", queryResult.getStr("error")));
                    task.setCostTime(System.currentTimeMillis() - startTime);
                    aiGenerateTaskMapper.updateById(task);
                    log.error("生图失败, taskId={}, error={}", taskId, task.getErrorMsg());
                    return;
                }
                // 其他状态继续轮询
            }

            // 超时
            task.setStatus(3);
            task.setErrorMsg("生图超时");
            task.setCostTime(System.currentTimeMillis() - startTime);
            aiGenerateTaskMapper.updateById(task);
            log.error("生图超时, taskId={}", taskId);

        } catch (Exception e) {
            log.error("生图异常, taskId={}", taskId, e);
            task.setStatus(3);
            task.setErrorMsg(e.getMessage());
            task.setCostTime(System.currentTimeMillis() - startTime);
            aiGenerateTaskMapper.updateById(task);
        }
    }

    /**
     * 根据上传结果创建 Picture 记录
     *
     * @param result  上传结果（包含 COS URL、图片信息、标签、OCR、颜色分析等）
     * @param userId  用户 id
     * @param prompt  生图 prompt
     * @return Picture id
     */
    private Long createPictureFromUploadResult(UploadPictureResult result, Long userId, String prompt) {
        User loginUser = userService.getById(userId);
        Picture picture = new Picture();
        picture.setUrl(result.getUrl());
        picture.setThumbnailUrl(result.getThumbnailUrl());
        String userName = loginUser != null ? loginUser.getUserName() : "unknown";
        picture.setName("AiGenerate_" + userName + "_" + System.currentTimeMillis());
        picture.setPicSize(result.getPicSize());
        picture.setPicWidth(result.getPicWidth());
        picture.setPicHeight(result.getPicHeight());
        picture.setPicScale(result.getPicScale());
        picture.setPicFormat(result.getPicFormat());
        picture.setTags(result.getTags());
        picture.setOcrResult(result.getOcrResult());
        picture.setDominantColor(result.getDominantColor());
        picture.setDominantColorHex(result.getDominantColorHex());
        picture.setColorInfo(result.getColorInfo());
        picture.setAiAnalysis(result.getAiAnalysis());
        picture.setUserId(userId);
        picture.setSource("ai_generate");
        if (loginUser != null) {
            pictureService.fillReviewParams(picture, loginUser);
        } else {
            picture.setReviewStatus(1);
        }
        pictureService.save(picture);
        return picture.getId();
    }

    /**
     * 查询生图任务状态
     *
     * @param taskId 任务 id
     * @return 任务状态
     */
    @Override
    public TaskStatusResponse queryTaskStatus(Long taskId) {
        AiGenerateTask task = aiGenerateTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        }

        TaskStatusResponse response = new TaskStatusResponse();
        response.setTaskId(task.getId());
        response.setStatus(task.getStatus());
        response.setImageUrl(task.getImageUrl());
        response.setPictureId(task.getPictureId());
        response.setErrorMsg(task.getErrorMsg());
        return response;
    }
}
