package com.guyuqi.backend.model.vo.picture;

import com.guyuqi.backend.model.entity.PictureTransfer;
import com.guyuqi.backend.model.vo.space.SpaceVO;
import com.guyuqi.backend.model.vo.user.UserVO;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 图片流转记录 VO
 */
@Data
public class PictureTransferVO implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 图片 id
     */
    private Long pictureId;

    /**
     * 图片信息
     */
    private PictureVO picture;

    /**
     * 源空间 id
     */
    private Long sourceSpaceId;

    /**
     * 源空间信息
     */
    private SpaceVO sourceSpace;

    /**
     * 目标空间 id
     */
    private Long targetSpaceId;

    /**
     * 目标空间信息
     */
    private SpaceVO targetSpace;

    /**
     * 操作用户 id
     */
    private Long userId;

    /**
     * 操作用户信息
     */
    private UserVO user;

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

    /**
     * 实体类转 VO
     */
    public static PictureTransferVO objToVo(PictureTransfer pictureTransfer) {
        if (pictureTransfer == null) {
            return null;
        }
        PictureTransferVO pictureTransferVO = new PictureTransferVO();
        BeanUtils.copyProperties(pictureTransfer, pictureTransferVO);
        return pictureTransferVO;
    }
}
