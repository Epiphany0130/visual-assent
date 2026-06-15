package com.guyuqi.backend.model.dto.ai;

import lombok.Data;

import java.io.Serializable;

/**
 * 生图任务状态响应
 */
@Data
public class TaskStatusResponse implements Serializable {

    /**
     * 任务 id
     */
    private Long taskId;

    /**
     * 任务状态：0-待生成; 1-生成中; 2-成功; 3-失败
     */
    private Integer status;

    /**
     * 生成的图片 url（成功时返回）
     */
    private String imageUrl;

    /**
     * 入库后的素材 id（成功且入库后返回）
     */
    private Long pictureId;

    /**
     * 失败原因
     */
    private String errorMsg;

    private static final long serialVersionUID = 1L;
}
