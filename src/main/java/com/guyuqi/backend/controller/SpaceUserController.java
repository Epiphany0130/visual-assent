package com.guyuqi.backend.controller;

import cn.hutool.core.util.ObjectUtil;
import com.guyuqi.backend.common.BaseResponse;
import com.guyuqi.backend.common.DeleteRequest;
import com.guyuqi.backend.common.ResultUtils;
import com.guyuqi.backend.exception.BusinessException;
import com.guyuqi.backend.exception.ErrorCode;
import com.guyuqi.backend.exception.ThrowUtils;
import com.guyuqi.backend.manager.auth.annotation.SaSpaceCheckPermission;
import com.guyuqi.backend.manager.auth.model.SpaceUserPermissionConstant;
import com.guyuqi.backend.model.dto.spaceuser.SpaceUserAddRequest;
import com.guyuqi.backend.model.dto.spaceuser.SpaceUserEditRequest;
import com.guyuqi.backend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.guyuqi.backend.model.entity.SpaceUser;
import com.guyuqi.backend.model.entity.User;
import com.guyuqi.backend.model.vo.space.SpaceUserVO;
import com.guyuqi.backend.model.dto.log.LogAddRequest;
import com.guyuqi.backend.service.LogService;
import com.guyuqi.backend.service.SpaceUserService;
import com.guyuqi.backend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 空间成员接口
 */
@RestController
@RequestMapping("/spaceUser")
@Slf4j
public class SpaceUserController {

    @Resource
    private SpaceUserService spaceUserService;

    @Resource
    private UserService userService;

    @Resource
    private LogService logService;

    /**
     * 添加成员到空间
     */
    @PostMapping("/add")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Long> addSpaceUser(@RequestBody SpaceUserAddRequest spaceUserAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserAddRequest == null, ErrorCode.PARAMS_ERROR);
        long id = spaceUserService.addSpaceUser(spaceUserAddRequest);
        // 记录添加成员日志
        User loginUser = userService.getLoginUser(request);
        LogAddRequest logAddRequest = new LogAddRequest();
        logAddRequest.setUserId(loginUser.getId());
        logAddRequest.setUserName(loginUser.getUserName());
        logAddRequest.setOperationType("permission_change");
        logAddRequest.setTargetType("space_user");
        logAddRequest.setTargetId(id);
        logAddRequest.setSpaceId(spaceUserAddRequest.getSpaceId());
        logAddRequest.setStatus(1);
        logService.addLog(logAddRequest);
        return ResultUtils.success(id);
    }

    /**
     * 从空间移除成员
     */
    @PostMapping("/delete")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> deleteSpaceUser(@RequestBody DeleteRequest deleteRequest,
                                                 HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        // 判断是否存在
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceUserService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 记录移除成员日志
        User loginUser = userService.getLoginUser(request);
        LogAddRequest logAddRequest = new LogAddRequest();
        logAddRequest.setUserId(loginUser.getId());
        logAddRequest.setUserName(loginUser.getUserName());
        logAddRequest.setOperationType("permission_change");
        logAddRequest.setTargetType("space_user");
        logAddRequest.setTargetId(id);
        logAddRequest.setSpaceId(oldSpaceUser.getSpaceId());
        logAddRequest.setStatus(1);
        logService.addLog(logAddRequest);
        return ResultUtils.success(true);
    }

    /**
     * 查询某个成员在某个空间的信息
     */
    @PostMapping("/get")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<SpaceUser> getSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest) {
        // 参数校验
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        Long spaceId = spaceUserQueryRequest.getSpaceId();
        Long userId = spaceUserQueryRequest.getUserId();
        ThrowUtils.throwIf(ObjectUtil.hasEmpty(spaceId, userId), ErrorCode.PARAMS_ERROR);
        // 查询数据库
        SpaceUser spaceUser = spaceUserService.getOne(spaceUserService.getQueryWrapper(spaceUserQueryRequest));
        ThrowUtils.throwIf(spaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(spaceUser);
    }

    /**
     * 查询成员信息列表
     */
    @PostMapping("/list")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<List<SpaceUserVO>> listSpaceUser(@RequestBody SpaceUserQueryRequest spaceUserQueryRequest,
                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(spaceUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest)
        );
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }

    /**
     * 编辑成员信息（设置权限）
     */
    @PostMapping("/edit")
    @SaSpaceCheckPermission(value = SpaceUserPermissionConstant.SPACE_USER_MANAGE)
    public BaseResponse<Boolean> editSpaceUser(@RequestBody SpaceUserEditRequest spaceUserEditRequest,
                                               HttpServletRequest request) {
        if (spaceUserEditRequest == null || spaceUserEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 将实体类和 DTO 进行转换
        SpaceUser spaceUser = new SpaceUser();
        BeanUtils.copyProperties(spaceUserEditRequest, spaceUser);
        // 数据校验
        spaceUserService.validSpaceUser(spaceUser, false);
        // 判断是否存在
        long id = spaceUserEditRequest.getId();
        SpaceUser oldSpaceUser = spaceUserService.getById(id);
        ThrowUtils.throwIf(oldSpaceUser == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = spaceUserService.updateById(spaceUser);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 记录编辑成员权限日志
        User loginUser = userService.getLoginUser(request);
        LogAddRequest logAddRequest = new LogAddRequest();
        logAddRequest.setUserId(loginUser.getId());
        logAddRequest.setUserName(loginUser.getUserName());
        logAddRequest.setOperationType("permission_change");
        logAddRequest.setTargetType("space_user");
        logAddRequest.setTargetId(id);
        logAddRequest.setSpaceId(oldSpaceUser.getSpaceId());
        logAddRequest.setStatus(1);
        logService.addLog(logAddRequest);
        return ResultUtils.success(true);
    }

    /**
     * 查询我加入的团队空间列表
     */
    @PostMapping("/list/my")
    public BaseResponse<List<SpaceUserVO>> listMyTeamSpace(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        SpaceUserQueryRequest spaceUserQueryRequest = new SpaceUserQueryRequest();
        spaceUserQueryRequest.setUserId(loginUser.getId());
        List<SpaceUser> spaceUserList = spaceUserService.list(
                spaceUserService.getQueryWrapper(spaceUserQueryRequest)
        );
        return ResultUtils.success(spaceUserService.getSpaceUserVOList(spaceUserList));
    }
}
