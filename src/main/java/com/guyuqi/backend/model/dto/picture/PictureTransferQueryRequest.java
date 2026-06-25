package com.guyuqi.backend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片流转查询请求
 */
@Data
public class PictureTransferQueryRequest implements Serializable {
    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 源空间 id
     */
    private Long sourceSpaceId;

    /**
     * 目标空间 id
     */
    private Long targetSpaceId;

    /**
     * 操作用户 id
     */
    private Long userId;

    /**
     * 审核状态：0-待审核；1-通过；2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 当前页
     */
    private long current = 1;

    /**
     * 页面大小
     */
    private long pageSize = 10;
}
