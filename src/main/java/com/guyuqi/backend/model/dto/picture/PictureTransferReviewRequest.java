package com.guyuqi.backend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片流转审核请求
 */
@Data
public class PictureTransferReviewRequest implements Serializable {
    /**
     * 流转记录 id
     */
    private Long transferId;

    /**
     * 审核状态：1-通过；2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;
}
