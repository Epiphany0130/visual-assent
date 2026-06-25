package com.guyuqi.backend.service.impl;

import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guyuqi.backend.exception.BusinessException;
import com.guyuqi.backend.exception.ErrorCode;
import com.guyuqi.backend.exception.ThrowUtils;
import com.guyuqi.backend.mapper.PictureTransferMapper;
import com.guyuqi.backend.model.dto.picture.PictureTransferQueryRequest;
import com.guyuqi.backend.model.dto.picture.PictureTransferRequest;
import com.guyuqi.backend.model.dto.picture.PictureTransferReviewRequest;
import com.guyuqi.backend.model.entity.Picture;
import com.guyuqi.backend.model.entity.PictureTransfer;
import com.guyuqi.backend.model.entity.Space;
import com.guyuqi.backend.model.entity.User;
import com.guyuqi.backend.model.enums.PictureReviewStatusEnum;
import com.guyuqi.backend.model.vo.picture.PictureTransferVO;
import com.guyuqi.backend.model.vo.space.SpaceVO;
import com.guyuqi.backend.model.dto.log.LogAddRequest;
import com.guyuqi.backend.service.LogService;
import com.guyuqi.backend.service.PictureService;
import com.guyuqi.backend.service.PictureTransferService;
import com.guyuqi.backend.service.SpaceService;
import com.guyuqi.backend.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 图片流转记录 Service 实现类
 */
@Slf4j
@Service
public class PictureTransferServiceImpl extends ServiceImpl<PictureTransferMapper, PictureTransfer>
        implements PictureTransferService {

    @Resource
    private PictureService pictureService;

    @Lazy
    @Resource
    private SpaceService spaceService;

    @Resource
    private UserService userService;

    @Resource
    private LogService logService;

    @Resource
    private TransactionTemplate transactionTemplate;

    /**
     * 发起图片流转
     */
    @Override
    public Long transferPicture(PictureTransferRequest pictureTransferRequest, User loginUser) {
        ThrowUtils.throwIf(pictureTransferRequest == null, ErrorCode.PARAMS_ERROR);
        Long pictureId = pictureTransferRequest.getPictureId();
        Long targetSpaceId = pictureTransferRequest.getTargetSpaceId();
        ThrowUtils.throwIf(pictureId == null || pictureId <= 0, ErrorCode.PARAMS_ERROR, "图片 id 不能为空");

        // 查询图片
        Picture picture = pictureService.getById(pictureId);
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        // 校验用户权限：图片所有者 或 源空间管理员
        Long sourceSpaceId = picture.getSpaceId();
        if (sourceSpaceId != null) {
            // 图片在空间中，需要校验空间权限
            Space sourceSpace = spaceService.getById(sourceSpaceId);
            ThrowUtils.throwIf(sourceSpace == null, ErrorCode.NOT_FOUND_ERROR, "源空间不存在");
            // 校验是否是空间创建者或管理员
            if (!loginUser.getId().equals(sourceSpace.getUserId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有源空间的权限");
            }
        } else {
            // 图片在公共图库，只有图片所有者或管理员可以操作
            if (!picture.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有操作权限");
            }
        }

        // 校验目标空间
        if (targetSpaceId != null) {
            Space targetSpace = spaceService.getById(targetSpaceId);
            ThrowUtils.throwIf(targetSpace == null, ErrorCode.NOT_FOUND_ERROR, "目标空间不存在");

            // 校验目标空间额度
            ThrowUtils.throwIf(targetSpace.getTotalCount() >= targetSpace.getMaxCount(),
                    ErrorCode.OPERATION_ERROR, "目标空间图片数量已达上限");
            ThrowUtils.throwIf(targetSpace.getTotalSize() + picture.getPicSize() > targetSpace.getMaxSize(),
                    ErrorCode.OPERATION_ERROR, "目标空间存储空间不足");
        }

        // 检查是否已有待审核的流转记录
        long pendingCount = this.lambdaQuery()
                .eq(PictureTransfer::getPictureId, pictureId)
                .eq(PictureTransfer::getReviewStatus, PictureReviewStatusEnum.REVIEWING.getValue())
                .count();
        ThrowUtils.throwIf(pendingCount > 0, ErrorCode.OPERATION_ERROR, "该图片已有待审核的流转记录");

        // 创建流转记录
        PictureTransfer pictureTransfer = new PictureTransfer();
        pictureTransfer.setPictureId(pictureId);
        pictureTransfer.setSourceSpaceId(sourceSpaceId);
        pictureTransfer.setTargetSpaceId(targetSpaceId);
        pictureTransfer.setUserId(loginUser.getId());
        pictureTransfer.setUserName(loginUser.getUserName());
        pictureTransfer.setTransferType("move");
        pictureTransfer.setReason(pictureTransferRequest.getReason());
        pictureTransfer.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        pictureTransfer.setStatus(1);

        boolean result = this.save(pictureTransfer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "创建流转记录失败");

        // 记录日志
        LogAddRequest logAddRequest = new LogAddRequest();
        logAddRequest.setUserId(loginUser.getId());
        logAddRequest.setUserName(loginUser.getUserName());
        logAddRequest.setOperationType("transfer");
        logAddRequest.setTargetType("picture");
        logAddRequest.setTargetId(pictureId);
        logAddRequest.setTargetName(picture.getName());
        logAddRequest.setSpaceId(sourceSpaceId);
        logAddRequest.setStatus(1);
        logService.addLog(logAddRequest);

        return pictureTransfer.getId();
    }

    /**
     * 审核图片流转
     */
    @Override
    public boolean reviewTransfer(PictureTransferReviewRequest pictureTransferReviewRequest, User loginUser) {
        ThrowUtils.throwIf(pictureTransferReviewRequest == null, ErrorCode.PARAMS_ERROR);
        Long transferId = pictureTransferReviewRequest.getTransferId();
        Integer reviewStatus = pictureTransferReviewRequest.getReviewStatus();
        ThrowUtils.throwIf(transferId == null || transferId <= 0, ErrorCode.PARAMS_ERROR, "流转记录 id 不能为空");
        ThrowUtils.throwIf(reviewStatus == null || (reviewStatus != 1 && reviewStatus != 2),
                ErrorCode.PARAMS_ERROR, "审核状态无效");

        // 查询流转记录
        PictureTransfer pictureTransfer = this.getById(transferId);
        ThrowUtils.throwIf(pictureTransfer == null, ErrorCode.NOT_FOUND_ERROR, "流转记录不存在");

        // 校验是否已审核
        ThrowUtils.throwIf(pictureTransfer.getReviewStatus() != PictureReviewStatusEnum.REVIEWING.getValue(),
                ErrorCode.OPERATION_ERROR, "该记录已审核");

        // 校验审核人权限：系统管理员 或 目标空间管理员
        Long targetSpaceId = pictureTransfer.getTargetSpaceId();
        if (targetSpaceId != null) {
            Space targetSpace = spaceService.getById(targetSpaceId);
            ThrowUtils.throwIf(targetSpace == null, ErrorCode.NOT_FOUND_ERROR, "目标空间不存在");
            // 校验是否是目标空间创建者或管理员
            if (!loginUser.getId().equals(targetSpace.getUserId()) && !userService.isAdmin(loginUser)) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "没有目标空间的审核权限");
            }
        } else {
            // 目标是公共图库，只有管理员可以审核
            ThrowUtils.throwIf(!userService.isAdmin(loginUser), ErrorCode.NO_AUTH_ERROR, "没有审核权限");
        }

        // 更新审核状态
        PictureTransfer updateTransfer = new PictureTransfer();
        updateTransfer.setId(transferId);
        updateTransfer.setReviewStatus(reviewStatus);
        updateTransfer.setReviewMessage(pictureTransferReviewRequest.getReviewMessage());
        updateTransfer.setReviewerId(loginUser.getId());
        updateTransfer.setReviewTime(new Date());

        boolean result = this.updateById(updateTransfer);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "审核失败");

        // 如果审核通过，执行流转操作
        if (reviewStatus == PictureReviewStatusEnum.PASS.getValue()) {
            executeTransfer(transferId);
        }

        // 记录日志
        LogAddRequest logAddRequest = new LogAddRequest();
        logAddRequest.setUserId(loginUser.getId());
        logAddRequest.setUserName(loginUser.getUserName());
        logAddRequest.setOperationType("transfer_review");
        logAddRequest.setTargetType("picture_transfer");
        logAddRequest.setTargetId(transferId);
        logAddRequest.setTargetName("流转审核");
        logAddRequest.setStatus(1);
        logService.addLog(logAddRequest);

        return true;
    }

    /**
     * 执行流转操作
     */
    @Override
    public void executeTransfer(Long transferId) {
        PictureTransfer pictureTransfer = this.getById(transferId);
        ThrowUtils.throwIf(pictureTransfer == null, ErrorCode.NOT_FOUND_ERROR, "流转记录不存在");

        // 查询图片
        Picture picture = pictureService.getById(pictureTransfer.getPictureId());
        ThrowUtils.throwIf(picture == null, ErrorCode.NOT_FOUND_ERROR, "图片不存在");

        Long sourceSpaceId = pictureTransfer.getSourceSpaceId();
        Long targetSpaceId = pictureTransfer.getTargetSpaceId();
        Long picSize = picture.getPicSize();

        // 事务操作
        transactionTemplate.execute(status -> {
            // 1. 更新图片的空间 id（使用 lambdaUpdate 确保 null 值也能更新）
            pictureService.lambdaUpdate()
                    .eq(Picture::getId, picture.getId())
                    .set(Picture::getSpaceId, targetSpaceId)
                    .update();

            // 2. 更新源空间额度
            if (sourceSpaceId != null) {
                spaceService.lambdaUpdate()
                        .eq(Space::getId, sourceSpaceId)
                        .setSql("totalSize = totalSize - " + picSize)
                        .setSql("totalCount = totalCount - 1")
                        .update();
            }

            // 3. 更新目标空间额度
            if (targetSpaceId != null) {
                spaceService.lambdaUpdate()
                        .eq(Space::getId, targetSpaceId)
                        .setSql("totalSize = totalSize + " + picSize)
                        .setSql("totalCount = totalCount + 1")
                        .update();
            }

            // 4. 更新流转记录状态
            PictureTransfer updateTransfer = new PictureTransfer();
            updateTransfer.setId(transferId);
            updateTransfer.setStatus(1);
            this.updateById(updateTransfer);

            return true;
        });

        log.info("图片流转执行成功, transferId={}, pictureId={}, from={}, to={}",
                transferId, picture.getId(), sourceSpaceId, targetSpaceId);
    }

    /**
     * 获取流转记录 VO
     */
    @Override
    public PictureTransferVO getPictureTransferVO(PictureTransfer pictureTransfer) {
        if (pictureTransfer == null) {
            return null;
        }
        PictureTransferVO pictureTransferVO = PictureTransferVO.objToVo(pictureTransfer);

        // 填充图片信息
        Picture picture = pictureService.getById(pictureTransfer.getPictureId());
        if (picture != null) {
            pictureTransferVO.setPicture(pictureService.getPictureVO(picture, null));
        }

        // 填充源空间信息
        if (pictureTransfer.getSourceSpaceId() != null) {
            Space sourceSpace = spaceService.getById(pictureTransfer.getSourceSpaceId());
            if (sourceSpace != null) {
                pictureTransferVO.setSourceSpace(SpaceVO.objToVo(sourceSpace));
            }
        }

        // 填充目标空间信息
        if (pictureTransfer.getTargetSpaceId() != null) {
            Space targetSpace = spaceService.getById(pictureTransfer.getTargetSpaceId());
            if (targetSpace != null) {
                pictureTransferVO.setTargetSpace(SpaceVO.objToVo(targetSpace));
            }
        }

        // 填充用户信息
        User user = userService.getById(pictureTransfer.getUserId());
        if (user != null) {
            pictureTransferVO.setUser(userService.getUserVO(user));
        }

        return pictureTransferVO;
    }

    /**
     * 分页获取流转记录列表
     */
    @Override
    public Page<PictureTransfer> getPictureTransferPage(PictureTransferQueryRequest pictureTransferQueryRequest) {
        long current = pictureTransferQueryRequest.getCurrent();
        long size = pictureTransferQueryRequest.getPageSize();
        return this.page(new Page<>(current, size), getQueryWrapper(pictureTransferQueryRequest));
    }

    /**
     * 分页获取流转记录 VO 列表
     */
    @Override
    public Page<PictureTransferVO> getPictureTransferVOPage(PictureTransferQueryRequest pictureTransferQueryRequest) {
        Page<PictureTransfer> pictureTransferPage = getPictureTransferPage(pictureTransferQueryRequest);
        Page<PictureTransferVO> pictureTransferVOPage = new Page<>(pictureTransferPage.getCurrent(), pictureTransferPage.getSize(), pictureTransferPage.getTotal());
        List<PictureTransferVO> pictureTransferVOList = pictureTransferPage.getRecords().stream()
                .map(this::getPictureTransferVO)
                .collect(Collectors.toList());
        pictureTransferVOPage.setRecords(pictureTransferVOList);
        return pictureTransferVOPage;
    }

    /**
     * 根据查询条件获取 QueryWrapper
     */
    @Override
    public QueryWrapper<PictureTransfer> getQueryWrapper(PictureTransferQueryRequest pictureTransferQueryRequest) {
        QueryWrapper<PictureTransfer> queryWrapper = new QueryWrapper<>();
        if (pictureTransferQueryRequest == null) {
            return queryWrapper;
        }
        Long pictureId = pictureTransferQueryRequest.getPictureId();
        Long sourceSpaceId = pictureTransferQueryRequest.getSourceSpaceId();
        Long targetSpaceId = pictureTransferQueryRequest.getTargetSpaceId();
        Long userId = pictureTransferQueryRequest.getUserId();
        Integer reviewStatus = pictureTransferQueryRequest.getReviewStatus();

        queryWrapper.eq(ObjUtil.isNotEmpty(pictureId), "pictureId", pictureId);
        queryWrapper.eq(ObjUtil.isNotEmpty(sourceSpaceId), "sourceSpaceId", sourceSpaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(targetSpaceId), "targetSpaceId", targetSpaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(reviewStatus), "reviewStatus", reviewStatus);
        queryWrapper.orderByDesc("createTime");
        return queryWrapper;
    }
}
