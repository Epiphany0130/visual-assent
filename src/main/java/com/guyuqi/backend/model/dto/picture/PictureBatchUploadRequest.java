package com.guyuqi.backend.model.dto.picture;

import lombok.Data;

import java.io.Serializable;

/**
 * 图片批量上传请求
 */
@Data
public class PictureBatchUploadRequest implements Serializable {

    /**
     * 项目名称（用于自动命名前缀）
     */
    private String projectName;

    /**
     * 空间 id
     */
    private Long spaceId;


    private static final long serialVersionUID = 1L;
}
