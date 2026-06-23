package com.guyuqi.backend.model.vo.log;

import lombok.Data;

import java.util.Date;

/**
 * 操作日志与审计视图
 */
@Data
public class LogVO {
    /**
     * id
     */
    private Long id;

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
     * 操作对象名称（冗余，便于展示）
     */
    private String targetName;

    /**
     * 操作 IP 地址
     */
    private String ipAddress;

    /**
     * 操作详情（JSON）：记录变更前后的内容
     */
    private String operationDetail;

    /**
     * 操作状态：0-失败；1-成功；2-待审批
     */
    private Integer status;

    /**
     * 操作时间
     */
    private Date operationTime;
}