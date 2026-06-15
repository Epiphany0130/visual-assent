package com.guyuqi.backend.model.dto.ai;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片生成请求
 */
@Data
public class ImageGenerateRequest implements Serializable {

    /**
     * 最终确认的 prompt（可为优化后的）
     */
    private String prompt;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    private static final long serialVersionUID = 1L;
}
