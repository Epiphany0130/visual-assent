-- 图片流转记录表
create table if not exists picture_transfer
(
    id              bigint auto_increment comment 'id' primary key,
    pictureId       bigint                                 not null comment '图片 id',
    sourceSpaceId   bigint                                 null comment '源空间 id（null 表示公共图库）',
    targetSpaceId   bigint                                 null comment '目标空间 id（null 表示公共图库）',
    userId          bigint                                 not null comment '操作用户 id',
    userName        varchar(64)                            null comment '操作用户名称',
    transferType    varchar(32)                            not null comment '流转类型：move-移动',
    reason          varchar(512)                           null comment '流转原因',
    reviewStatus    int       default 0                    not null comment '审核状态：0-待审核；1-通过；2-拒绝',
    reviewMessage   varchar(512)                           null comment '审核信息',
    reviewerId      bigint                                 null comment '审核人 id',
    reviewTime      datetime                               null comment '审核时间',
    status          int       default 1                    not null comment '状态：0-失败；1-成功',
    createTime      datetime default CURRENT_TIMESTAMP     not null comment '创建时间',
    updateTime      datetime default CURRENT_TIMESTAMP     not null on update CURRENT_TIMESTAMP comment '更新时间',
    -- 索引设计
    INDEX idx_pictureId (pictureId),
    INDEX idx_sourceSpaceId (sourceSpaceId),
    INDEX idx_targetSpaceId (targetSpaceId),
    INDEX idx_userId (userId),
    INDEX idx_reviewStatus (reviewStatus)
) comment '图片流转记录' collate = utf8mb4_unicode_ci;

ALTER TABLE picture_transfer
    ADD COLUMN isDelete int default 0 not null comment '是否删除（0-未删除；1-已删除）',
    ADD COLUMN editTime  datetime null comment '编辑时间';