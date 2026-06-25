package com.guyuqi.backend.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guyuqi.backend.model.dto.picture.PictureTransferQueryRequest;
import com.guyuqi.backend.model.dto.picture.PictureTransferRequest;
import com.guyuqi.backend.model.dto.picture.PictureTransferReviewRequest;
import com.guyuqi.backend.model.entity.PictureTransfer;
import com.guyuqi.backend.model.entity.User;
import com.guyuqi.backend.model.vo.picture.PictureTransferVO;

/**
 * 图片流转记录 Service 接口
 */
public interface PictureTransferService extends IService<PictureTransfer> {

    /**
     * 发起图片流转
     *
     * @param pictureTransferRequest 流转请求
     * @param loginUser              登录用户
     * @return 流转记录 id
     */
    Long transferPicture(PictureTransferRequest pictureTransferRequest, User loginUser);

    /**
     * 审核图片流转
     *
     * @param pictureTransferReviewRequest 审核请求
     * @param loginUser                    登录用户
     * @return 是否审核成功
     */
    boolean reviewTransfer(PictureTransferReviewRequest pictureTransferReviewRequest, User loginUser);

    /**
     * 执行流转操作（审核通过后调用）
     *
     * @param transferId 流转记录 id
     */
    void executeTransfer(Long transferId);

    /**
     * 获取流转记录 VO
     *
     * @param pictureTransfer 流转记录
     * @return 流转记录 VO
     */
    PictureTransferVO getPictureTransferVO(PictureTransfer pictureTransfer);

    /**
     * 分页获取流转记录列表
     *
     * @param pictureTransferQueryRequest 查询请求
     * @return 流转记录分页
     */
    Page<PictureTransfer> getPictureTransferPage(PictureTransferQueryRequest pictureTransferQueryRequest);

    /**
     * 分页获取流转记录 VO 列表
     *
     * @param pictureTransferQueryRequest 查询请求
     * @return 流转记录 VO 分页
     */
    Page<PictureTransferVO> getPictureTransferVOPage(PictureTransferQueryRequest pictureTransferQueryRequest);

    /**
     * 根据查询条件获取 QueryWrapper
     *
     * @param pictureTransferQueryRequest 查询请求
     * @return QueryWrapper
     */
    com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<PictureTransfer> getQueryWrapper(PictureTransferQueryRequest pictureTransferQueryRequest);
}
