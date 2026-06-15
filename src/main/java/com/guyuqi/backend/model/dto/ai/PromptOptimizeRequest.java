package com.guyuqi.backend.model.dto.ai;

import lombok.Data;

import java.io.Serializable;

/**
 * Prompt 优化请求
 */
@Data
public class PromptOptimizeRequest implements Serializable {

    /**
     * 用户原始输入的描述
     */
    private String prompt;

    private static final long serialVersionUID = 1L;
}
