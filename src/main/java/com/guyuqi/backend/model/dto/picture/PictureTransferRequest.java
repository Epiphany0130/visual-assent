package com.guyuqi.backend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片流转请求
 */
@Data
public class PictureTransferRequest implements Serializable {
    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 目标空间 id（null 表示公共图库）
     */
    private Long targetSpaceId;

    /**
     * 流转原因
     */
    private String reason;
}
