package com.guyuqi.backend.model.dto.space;

import lombok.Data;

import java.io.Serializable;

/**
 * 空间创建请求
 */
@Data
public class SpaceAddRequest implements Serializable {

    /**
     * 空间名称
     */
    private String spaceName;

    /**
     * 空间描述
     */
    private String spaceDesc;

    /**
     * 空间级别：0-免费版 1-基础版 2-专业版
     */
    private Integer spaceLevel;

    /**
     * 空间类型：0-私有 1-团队
     */
    private Integer spaceType;

    private static final long serialVersionUID = 1L;
}
