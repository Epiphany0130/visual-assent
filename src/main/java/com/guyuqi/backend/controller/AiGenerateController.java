package com.guyuqi.backend.controller;

import com.guyuqi.backend.common.BaseResponse;
import com.guyuqi.backend.common.ResultUtils;
import com.guyuqi.backend.exception.ErrorCode;
import com.guyuqi.backend.exception.ThrowUtils;
import com.guyuqi.backend.model.dto.ai.ImageGenerateRequest;
import com.guyuqi.backend.model.dto.ai.PromptOptimizeRequest;
import com.guyuqi.backend.model.dto.ai.PromptOptimizeResponse;
import com.guyuqi.backend.model.dto.ai.TaskStatusResponse;
import com.guyuqi.backend.model.entity.User;
import com.guyuqi.backend.service.AiGenerateService;
import com.guyuqi.backend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI 绘图接口
 */
@Slf4j
@RestController
@RequestMapping("/ai/generate")
public class AiGenerateController {

    @Resource
    private AiGenerateService aiGenerateService;

    @Resource
    private UserService userService;

    /**
     * 优化 prompt
     *
     * @param promptOptimizeRequest prompt 优化请求
     * @return 优化后的 prompt
     */
    @PostMapping("/prompt/optimize")
    public BaseResponse<PromptOptimizeResponse> optimizePrompt(
            @RequestBody PromptOptimizeRequest promptOptimizeRequest) {
        String prompt = promptOptimizeRequest.getPrompt();
        ThrowUtils.throwIf(prompt == null || prompt.isBlank(), ErrorCode.PARAMS_ERROR, "prompt 不能为空");
        ThrowUtils.throwIf(prompt.length() > 500, ErrorCode.PARAMS_ERROR, "prompt 过长");
        PromptOptimizeResponse result = aiGenerateService.optimizePrompt(prompt);
        return ResultUtils.success(result);
    }

    /**
     * 提交图片生成任务
     *
     * @param imageGenerateRequest 生图请求
     * @param request              HTTP 请求
     * @return 任务 id
     */
    @PostMapping("/image")
    public BaseResponse<Long> generateImage(
            @RequestBody ImageGenerateRequest imageGenerateRequest,
            HttpServletRequest request) {
        String prompt = imageGenerateRequest.getPrompt();
        ThrowUtils.throwIf(prompt == null || prompt.isBlank(), ErrorCode.PARAMS_ERROR, "prompt 不能为空");

        Integer width = imageGenerateRequest.getWidth();
        Integer height = imageGenerateRequest.getHeight();
        // 默认 1024x1024
        if (width == null || width <= 0) width = 1024;
        if (height == null || height <= 0) height = 1024;

        User loginUser = userService.getLoginUser(request);
        Long taskId = aiGenerateService.submitImageGenerateTask(loginUser.getId(), prompt, width, height);
        return ResultUtils.success(taskId);
    }

    /**
     * 查询生图任务状态
     *
     * @param taskId 任务 id
     * @return 任务状态
     */
    @GetMapping("/task/{taskId}")
    public BaseResponse<TaskStatusResponse> queryTaskStatus(@PathVariable Long taskId) {
        ThrowUtils.throwIf(taskId == null || taskId <= 0, ErrorCode.PARAMS_ERROR, "taskId 无效");
        TaskStatusResponse result = aiGenerateService.queryTaskStatus(taskId);
        return ResultUtils.success(result);
    }
}
