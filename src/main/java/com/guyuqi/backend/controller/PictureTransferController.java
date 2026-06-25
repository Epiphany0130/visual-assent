package com.guyuqi.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guyuqi.backend.annotation.AuthCheck;
import com.guyuqi.backend.common.BaseResponse;
import com.guyuqi.backend.common.ResultUtils;
import com.guyuqi.backend.constant.UserConstant;
import com.guyuqi.backend.exception.ErrorCode;
import com.guyuqi.backend.exception.ThrowUtils;
import com.guyuqi.backend.manager.auth.annotation.SaSpaceCheckPermission;
import com.guyuqi.backend.manager.auth.model.SpaceUserPermissionConstant;
import com.guyuqi.backend.model.dto.picture.PictureTransferQueryRequest;
import com.guyuqi.backend.model.dto.picture.PictureTransferRequest;
import com.guyuqi.backend.model.dto.picture.PictureTransferReviewRequest;
import com.guyuqi.backend.model.entity.PictureTransfer;
import com.guyuqi.backend.model.entity.User;
import com.guyuqi.backend.model.vo.picture.PictureTransferVO;
import com.guyuqi.backend.service.PictureTransferService;
import com.guyuqi.backend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 图片流转接口
 */
@Slf4j
@RestController
@RequestMapping("/picture/transfer")
public class PictureTransferController {

    @Resource
    private PictureTransferService pictureTransferService;

    @Resource
    private UserService userService;

    /**
     * 发起图片流转
     *
     * @param pictureTransferRequest 流转请求
     * @param request                HTTP 请求
     * @return 流转记录 id
     */
    @PostMapping("/add")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.PICTURE_TRANSFER)
    public BaseResponse<Long> transferPicture(@RequestBody PictureTransferRequest pictureTransferRequest,
                                              HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        Long transferId = pictureTransferService.transferPicture(pictureTransferRequest, loginUser);
        return ResultUtils.success(transferId);
    }

    /**
     * 审核图片流转
     *
     * @param pictureTransferReviewRequest 审核请求
     * @param request                      HTTP 请求
     * @return 是否审核成功
     */
    @PostMapping("/review")
    public BaseResponse<Boolean> reviewTransfer(@RequestBody PictureTransferReviewRequest pictureTransferReviewRequest,
                                                HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        boolean result = pictureTransferService.reviewTransfer(pictureTransferReviewRequest, loginUser);
        return ResultUtils.success(result);
    }

    /**
     * 根据 id 获取流转记录（封装类）
     *
     * @param id 流转记录 id
     * @return 流转记录 VO
     */
    @GetMapping("/get/vo")
    public BaseResponse<PictureTransferVO> getPictureTransferVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        PictureTransfer pictureTransfer = pictureTransferService.getById(id);
        ThrowUtils.throwIf(pictureTransfer == null, ErrorCode.NOT_FOUND_ERROR);
        PictureTransferVO pictureTransferVO = pictureTransferService.getPictureTransferVO(pictureTransfer);
        return ResultUtils.success(pictureTransferVO);
    }

    /**
     * 分页获取流转记录列表（仅管理员可用）
     *
     * @param pictureTransferQueryRequest 查询请求
     * @return 流转记录分页
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<PictureTransfer>> listPictureTransferByPage(
            @RequestBody PictureTransferQueryRequest pictureTransferQueryRequest) {
        long current = pictureTransferQueryRequest.getCurrent();
        long size = pictureTransferQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<PictureTransfer> pictureTransferPage = pictureTransferService.getPictureTransferPage(pictureTransferQueryRequest);
        return ResultUtils.success(pictureTransferPage);
    }

    /**
     * 分页获取流转记录列表（封装类，仅管理员可用）
     *
     * @param pictureTransferQueryRequest 查询请求
     * @return 流转记录 VO 分页
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<PictureTransferVO>> listPictureTransferVOByPage(
            @RequestBody PictureTransferQueryRequest pictureTransferQueryRequest) {
        long current = pictureTransferQueryRequest.getCurrent();
        long size = pictureTransferQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<PictureTransferVO> pictureTransferVOPage = pictureTransferService.getPictureTransferVOPage(pictureTransferQueryRequest);
        return ResultUtils.success(pictureTransferVOPage);
    }

    /**
     * 分页获取待审核的流转记录列表（仅管理员可用）
     *
     * @param pictureTransferQueryRequest 查询请求
     * @return 流转记录 VO 分页
     */
    @PostMapping("/review/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<PictureTransferVO>> listPendingReviewTransferVOByPage(
            @RequestBody PictureTransferQueryRequest pictureTransferQueryRequest) {
        long current = pictureTransferQueryRequest.getCurrent();
        long size = pictureTransferQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 强制设置为待审核状态
        pictureTransferQueryRequest.setReviewStatus(0);
        Page<PictureTransferVO> pictureTransferVOPage = pictureTransferService.getPictureTransferVOPage(pictureTransferQueryRequest);
        return ResultUtils.success(pictureTransferVOPage);
    }
}
