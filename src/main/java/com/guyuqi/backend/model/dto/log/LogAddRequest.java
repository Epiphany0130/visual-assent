package com.guyuqi.backend.model.dto.log;

import lombok.Data;

import java.io.Serializable;

/**
 * 日志记录请求
 */
@Data
public class LogAddRequest implements Serializable {

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
     * 操作对象 id
     */
    private Long targetId;

    /**
     * 操作对象名称
     */
    private String targetName;

    /**
     * 关联空间 id
     */
    private Long spaceId;

    /**
     * 操作 IP 地址
     */
    private String ipAddress;

    /**
     * 操作状态：0-失败；1-成功；2-待审批
     */
    private Integer status;

    /**
     * 操作详情（JSON）
     */
    private String operationDetail;

    private static final long serialVersionUID = 1L;
}
