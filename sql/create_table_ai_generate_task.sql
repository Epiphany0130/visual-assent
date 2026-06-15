-- AI 绘图任务表
create table if not exists ai_generate_task
(
    id               bigint auto_increment comment 'id' primary key,
    userId           bigint                             not null comment '发起用户 id',
    originalPrompt   text                               null comment '用户原始输入',
    optimizedPrompt  text                               null comment '优化后的 prompt',
    modelParams      varchar(512)                       null comment '生图参数 JSON（尺寸、风格等）',
    status           int      default 0                 not null comment '任务状态：0-待生成; 1-生成中; 2-成功; 3-失败',
    imageUrl         varchar(512)                       null comment '生成的图片 url',
    pictureId        bigint                             null comment '入库后的素材 id',
    errorMsg         varchar(512)                       null comment '失败原因',
    costTime         bigint                             null comment '生成耗时（ms）',
    createTime       datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime       datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete         tinyint  default 0                 not null comment '是否删除',
    INDEX idx_userId (userId),           -- 提升基于用户 ID 的查询性能
    INDEX idx_status (status)            -- 提升基于任务状态的查询性能
) comment 'AI 绘图任务' collate = utf8mb4_unicode_ci;
