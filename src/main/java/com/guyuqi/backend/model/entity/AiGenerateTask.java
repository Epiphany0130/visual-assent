package com.guyuqi.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * AI 绘图任务
 * @TableName ai_generate_task
 */
@TableName(value = "ai_generate_task")
@Data
public class AiGenerateTask {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 发起用户 id
     */
    private Long userId;

    /**
     * 用户原始输入
     */
    private String originalPrompt;

    /**
     * 优化后的 prompt
     */
    private String optimizedPrompt;

    /**
     * 生图参数 JSON（尺寸、风格等）
     */
    private String modelParams;

    /**
     * 任务状态：0-待生成; 1-生成中; 2-成功; 3-失败
     */
    private Integer status;

    /**
     * 生成的图片 url
     */
    private String imageUrl;

    /**
     * 入库后的素材 id
     */
    private Long pictureId;

    /**
     * 失败原因
     */
    private String errorMsg;

    /**
     * 生成耗时（ms）
     */
    private Long costTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;
}
