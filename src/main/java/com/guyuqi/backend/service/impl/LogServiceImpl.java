package com.guyuqi.backend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.guyuqi.backend.model.dto.log.LogAddRequest;
import com.guyuqi.backend.model.dto.log.LogQueryRequest;
import com.guyuqi.backend.model.entity.Log;
import com.guyuqi.backend.model.vo.log.LogVO;
import com.guyuqi.backend.service.LogService;
import com.guyuqi.backend.mapper.LogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 针对表【operation_log(操作日志与审计)】的数据库操作 Service 实现
 */
@Service
public class LogServiceImpl extends ServiceImpl<LogMapper, Log>
        implements LogService {

    /**
     * 构造查询条件
     */
    @Override
    public QueryWrapper<Log> getQueryWrapper(LogQueryRequest logQueryRequest) {
        QueryWrapper<Log> queryWrapper = new QueryWrapper<>();
        if (logQueryRequest == null) {
            return queryWrapper;
        }

        // 从对象中取值
        Long userId = logQueryRequest.getUserId();
        String userName = logQueryRequest.getUserName();
        String operationType = logQueryRequest.getOperationType();
        String targetType = logQueryRequest.getTargetType();
        String targetName = logQueryRequest.getTargetName();
        Long spaceId = logQueryRequest.getSpaceId();
        Integer status = logQueryRequest.getStatus();
        String searchText = logQueryRequest.getSearchText();
        String sortField = logQueryRequest.getSortField();
        String sortOrder = logQueryRequest.getSortOrder();

        // 从多字段中搜索
        if (StrUtil.isNotBlank(searchText)) {
            queryWrapper.and(qw -> qw.like("userName", searchText)
                    .or().like("operationType", searchText)
                    .or().like("targetType", searchText)
                    .or().like("targetName", searchText)
                    .or().like("status", searchText)
            );
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.eq(StrUtil.isNotBlank(operationType), "operationType", operationType);
        queryWrapper.eq(StrUtil.isNotBlank(targetType), "targetType", targetType);
        queryWrapper.like(StrUtil.isNotBlank(targetName), "targetName", targetName);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceId), "spaceId", spaceId);
        queryWrapper.eq(ObjUtil.isNotEmpty(status), "status", status);
        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    /**
     * 获取日志视图对象
     */
    @Override
    public LogVO getLogVO(Log log, HttpServletRequest request) {
        LogVO logVO = new LogVO();
        BeanUtils.copyProperties(log, logVO);
        return logVO;
    }

    /**
     * 分页获取日志视图对象
     */
    @Override
    public Page<LogVO> getLogVOPage(Page<Log> logPage, HttpServletRequest request) {
        List<Log> logList = logPage.getRecords();
        Page<LogVO> logVOPage = new Page<>(logPage.getCurrent(), logPage.getSize(), logPage.getTotal());
        if (CollUtil.isEmpty(logList)) {
            return logVOPage;
        }
        List<LogVO> logVOList = logList.stream()
                .map(log -> {
                    LogVO logVO = new LogVO();
                    BeanUtils.copyProperties(log, logVO);
                    return logVO;
                })
                .collect(Collectors.toList());
        logVOPage.setRecords(logVOList);
        return logVOPage;
    }

    /**
     * 记录操作日志
     */
    @Override
    public void addLog(LogAddRequest logAddRequest) {
        Log log = new Log();
        BeanUtils.copyProperties(logAddRequest, log);
        log.setOperationTime(new Date());
        this.save(log);
    }
}
