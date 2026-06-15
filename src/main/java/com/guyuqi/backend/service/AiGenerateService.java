package com.guyuqi.backend.service;

import com.guyuqi.backend.model.dto.ai.PromptOptimizeResponse;
import com.guyuqi.backend.model.dto.ai.TaskStatusResponse;

/**
 * AI 绘图服务接口
 */
public interface AiGenerateService {

    /**
     * 优化用户输入的 prompt，提升生图质量
     *
     * @param prompt 用户原始描述
     * @return 优化后的 prompt
     */
    PromptOptimizeResponse optimizePrompt(String prompt);

    /**
     * 提交图片生成任务（异步执行）
     *
     * @param userId  用户 id
     * @param prompt  生图 prompt
     * @param width   图片宽度
     * @param height  图片高度
     * @return 任务 id
     */
    Long submitImageGenerateTask(Long userId, String prompt, Integer width, Integer height);

    /**
     * 查询生图任务状态
     *
     * @param taskId 任务 id
     * @return 任务状态
     */
    TaskStatusResponse queryTaskStatus(Long taskId);
}
