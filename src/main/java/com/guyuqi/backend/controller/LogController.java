package com.guyuqi.backend.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.guyuqi.backend.annotation.AuthCheck;
import com.guyuqi.backend.common.BaseResponse;
import com.guyuqi.backend.common.ResultUtils;
import com.guyuqi.backend.constant.UserConstant;
import com.guyuqi.backend.exception.ErrorCode;
import com.guyuqi.backend.exception.ThrowUtils;
import com.guyuqi.backend.model.dto.log.LogAddRequest;
import com.guyuqi.backend.model.dto.log.LogQueryRequest;
import com.guyuqi.backend.model.entity.Log;
import com.guyuqi.backend.model.entity.User;
import com.guyuqi.backend.model.vo.log.LogVO;
import com.guyuqi.backend.service.LogService;
import com.guyuqi.backend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 操作日志与审计接口
 */
@Slf4j
@RestController
@RequestMapping("/log")
public class LogController {

    @Resource
    private LogService logService;

    @Resource
    private UserService userService;

    /**
     * 根据 id 获取日志（仅管理员可用）
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Log> getLogById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Log logEntry = logService.getById(id);
        ThrowUtils.throwIf(logEntry == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(logEntry);
    }

    /**
     * 根据 id 获取日志（封装类）
     */
    @GetMapping("/get/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<LogVO> getLogVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Log logEntry = logService.getById(id);
        ThrowUtils.throwIf(logEntry == null, ErrorCode.NOT_FOUND_ERROR);
        LogVO logVO = logService.getLogVO(logEntry, request);
        return ResultUtils.success(logVO);
    }

    /**
     * 分页获取日志列表（仅管理员可用）
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Log>> listLogByPage(@RequestBody LogQueryRequest logQueryRequest) {
        long current = logQueryRequest.getCurrent();
        long size = logQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Log> logPage = logService.page(new Page<>(current, size),
                logService.getQueryWrapper(logQueryRequest));
        return ResultUtils.success(logPage);
    }

    /**
     * 分页获取日志列表（封装类）
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<LogVO>> listLogVOByPage(@RequestBody LogQueryRequest logQueryRequest,
                                                     HttpServletRequest request) {
        long current = logQueryRequest.getCurrent();
        long size = logQueryRequest.getPageSize();
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Log> logPage = logService.page(new Page<>(current, size),
                logService.getQueryWrapper(logQueryRequest));
        return ResultUtils.success(logService.getLogVOPage(logPage, request));
    }

    /**
     * 记录分享日志（前端分享时调用）
     */
    @PostMapping("/add/share")
    public BaseResponse<Boolean> addShareLog(@RequestBody LogAddRequest logAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(logAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser != null) {
            logAddRequest.setUserId(loginUser.getId());
            logAddRequest.setUserName(loginUser.getUserName());
        }
        // 设置分享操作类型
        logAddRequest.setOperationType("share");
        logAddRequest.setIpAddress(request.getRemoteAddr());
        logAddRequest.setStatus(1);
        logService.addLog(logAddRequest);
        return ResultUtils.success(true);
    }

    /**
     * 记录下载日志（前端下载时调用）
     */
    @PostMapping("/add/download")
    public BaseResponse<Boolean> addDownloadLog(@RequestBody LogAddRequest logAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(logAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (loginUser != null) {
            logAddRequest.setUserId(loginUser.getId());
            logAddRequest.setUserName(loginUser.getUserName());
        }
        // 设置下载操作类型
        logAddRequest.setOperationType("download");
        logAddRequest.setIpAddress(request.getRemoteAddr());
        logAddRequest.setStatus(1);
        logService.addLog(logAddRequest);
        return ResultUtils.success(true);
    }
}
