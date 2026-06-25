package com.guyuqi.backend.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片流转记录
 * @TableName picture_transfer
 */
@TableName(value = "picture_transfer")
@Data
public class PictureTransfer implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 源空间 id（null 表示公共图库）
     */
    private Long sourceSpaceId;

    /**
     * 目标空间 id（null 表示公共图库）
     */
    private Long targetSpaceId;

    /**
     * 操作用户 id
     */
    private Long userId;

    /**
     * 操作用户名称
     */
    private String userName;

    /**
     * 流转类型：move-移动
     */
    private String transferType;

    /**
     * 流转原因
     */
    private String reason;

    /**
     * 审核状态：0-待审核；1-通过；2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 审核信息
     */
    private String reviewMessage;

    /**
     * 审核人 id
     */
    private Long reviewerId;

    /**
     * 审核时间
     */
    private Date reviewTime;

    /**
     * 状态：0-失败；1-成功
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    @TableLogic
    private Integer isDelete;

    @TableField(fill = FieldFill.INSERT)
    private Date editTime;
}
