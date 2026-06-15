package com.guyuqi.backend.model.dto.ai;

import lombok.Data;

import java.io.Serializable;

/**
 * Prompt 优化响应
 */
@Data
public class PromptOptimizeResponse implements Serializable {

    /**
     * 优化后的 prompt
     */
    private String optimizedPrompt;

    private static final long serialVersionUID = 1L;
}
