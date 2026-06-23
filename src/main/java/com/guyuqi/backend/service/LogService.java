package com.guyuqi.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guyuqi.backend.model.dto.log.LogAddRequest;
import com.guyuqi.backend.model.dto.log.LogQueryRequest;
import com.guyuqi.backend.model.entity.Log;
import com.guyuqi.backend.model.vo.log.LogVO;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 针对表【operation_log(操作日志与审计)】的数据库操作 Service
 */
public interface LogService extends IService<Log> {

    /**
     * 构造查询条件
     */
    QueryWrapper<Log> getQueryWrapper(LogQueryRequest logQueryRequest);

    /**
     * 获取日志视图对象
     */
    LogVO getLogVO(Log log, HttpServletRequest request);

    /**
     * 分页获取日志视图对象
     */
    Page<LogVO> getLogVOPage(Page<Log> logPage, HttpServletRequest request);

    /**
     * 记录操作日志
     */
    void addLog(LogAddRequest logAddRequest);
}
