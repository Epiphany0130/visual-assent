-- 操作日志与审计表
create table if not exists operation_log
(
    id            bigint auto_increment comment 'id' primary key,
    userId        bigint                             not null comment '操作用户 id',
    userName      varchar(64)                        null comment '操作用户名称',
    operationType varchar(32)                        not null comment '操作类型：upload/download/delete/edit/share/permission_change/approve/login/logout',
    targetType    varchar(32)                        not null comment '操作对象类型：picture/space/space_user/user',
    targetId      bigint                             null comment '操作对象 id',
    targetName    varchar(128)                       null comment '操作对象名称（冗余，便于展示）',
    spaceId       bigint                             null comment '关联空间 id（为空表示公共空间操作）',
    ipAddress       varchar(64)                        null comment '操作 IP 地址',
    operationDetail text                               null comment '操作详情（JSON）：记录变更前后的内容',
    status          tinyint                            not null comment '操作状态：0-失败；1-成功；2-待审批',
    operationTime datetime default CURRENT_TIMESTAMP not null comment '操作时间',
    createTime    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    -- 索引设计
    INDEX idx_userId (userId),                            -- 按用户查询
    INDEX idx_operationType (operationType),              -- 按操作类型筛选
    INDEX idx_targetType_targetId (targetType, targetId), -- 按操作对象查询
    INDEX idx_spaceId (spaceId),                          -- 按空间筛选
    INDEX idx_operationTime (operationTime)               -- 按时间范围查询
) comment '操作日志与审计' collate = utf8mb4_unicode_ci;