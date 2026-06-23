package com.guyuqi.backend.model.dto.log;

import com.guyuqi.backend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 日志查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LogQueryRequest extends PageRequest implements Serializable {

    /**
     * 操作用户 id
     */
    private Long userId;

    /**
     * 操作用户名称
     */
    private String userName;

    /**
     * 操作类型：upload/download/delete/edit/share/permission_change/approve/login/logout
     */
    private String operationType;

    /**
     * 操作对象类型：picture/space/space_user/user
     */
    private String targetType;

    /**
     * 操作对象名称（冗余，便于展示）
     */
    private String targetName;

    /**
     * 关联空间 id（为空表示公共空间操作）
     */
    private Long spaceId;

    /**
     * 操作状态：0-失败；1-成功；2-待审批
     */
    private Integer status;

    /**
     * 搜索词（同时搜名称、简介等）
     */
    private String searchText;

    private static final long serialVersionUID = 1L;
}