-- 图片表
create table if not exists picture
(
    id           bigint auto_increment comment 'id' primary key,
    url          varchar(512)                       not null comment '图片 url',
    name         varchar(128)                       not null comment '图片名称',
    introduction varchar(512)                       null comment '简介',
    category     varchar(64)                        null comment '分类',
    tags         varchar(512)                      null comment '标签（JSON 数组）',
    picSize      bigint                             null comment '图片体积',
    picWidth     int                                null comment '图片宽度',
    picHeight    int                                null comment '图片高度',
    picScale     double                             null comment '图片宽高比例',
    picFormat    varchar(32)                        null comment '图片格式',
    userId       bigint                             not null comment '创建用户 id',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    editTime     datetime default CURRENT_TIMESTAMP not null comment '编辑时间',
    updateTime   datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 not null comment '是否删除',
    INDEX idx_name (name),                 -- 提升基于图片名称的查询性能
    INDEX idx_introduction (introduction), -- 用于模糊搜索图片简介
    INDEX idx_category (category),         -- 提升基于分类的查询性能
    INDEX idx_tags (tags),                 -- 提升基于标签的查询性能
    INDEX idx_userId (userId)              -- 提升基于用户 ID 的查询性能
) comment '图片' collate = utf8mb4_unicode_ci;

-- 添加审核相关字段
ALTER TABLE picture
    ADD COLUMN reviewStatus  INT DEFAULT 0 NOT NULL COMMENT '审核状态：0-待审核; 1-通过; 2-拒绝' AFTER userId,
    ADD COLUMN reviewMessage VARCHAR(512)  NULL COMMENT '审核信息' AFTER reviewStatus,
    ADD COLUMN reviewerId    BIGINT        NULL COMMENT '审核人 ID' AFTER reviewMessage,
    ADD COLUMN reviewTime    DATETIME      NULL COMMENT '审核时间' AFTER reviewerId;

-- 创建基于 reviewStatus 列的索引
CREATE INDEX idx_reviewStatus ON picture (reviewStatus);

-- 添加缩略图字段
ALTER TABLE picture
    -- 添加新列
    ADD COLUMN thumbnailUrl varchar(512) NULL COMMENT '缩略图 url' AFTER userId;

-- 添加 OCR 识别字段
ALTER TABLE picture
    ADD COLUMN ocrResult TEXT COMMENT 'OCR识别结果（JSON）' AFTER thumbnailUrl;

-- 添加 AI 分析字段
ALTER TABLE picture
    ADD COLUMN dominantColor varchar(32) NULL COMMENT '主色调名称' AFTER ocrResult,
    ADD COLUMN dominantColorHex varchar(16) NULL COMMENT '主色调 Hex' AFTER dominantColor,
    ADD COLUMN colorInfo TEXT NULL COMMENT '颜色识别结果（JSON）' AFTER dominantColorHex,
    ADD COLUMN aiAnalysis TEXT NULL COMMENT 'AI 分析结果（JSON）' AFTER colorInfo;

-- 主色调查询索引
CREATE INDEX idx_dominantColor ON picture (dominantColor);