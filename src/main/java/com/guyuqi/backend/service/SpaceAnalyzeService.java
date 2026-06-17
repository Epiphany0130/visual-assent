package com.guyuqi.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guyuqi.backend.exception.BusinessException;
import com.guyuqi.backend.exception.ErrorCode;
import com.guyuqi.backend.exception.ThrowUtils;
import com.guyuqi.backend.model.dto.space.SpaceAddRequest;
import com.guyuqi.backend.model.dto.space.SpaceQueryRequest;
import com.guyuqi.backend.model.dto.space.analyze.*;
import com.guyuqi.backend.model.entity.Picture;
import com.guyuqi.backend.model.entity.Space;
import com.guyuqi.backend.model.entity.User;
import com.guyuqi.backend.model.vo.space.SpaceVO;
import com.guyuqi.backend.model.vo.space.analyze.*;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author guyuqi
* @description 针对表【space(空间)】的分析操作Service
* @createDate 2026-06-16 19:14:35
*/
public interface SpaceAnalyzeService extends IService<Space> {

    /**
     * 获取空间使用分析数据
     *
     * @param spaceUsageAnalyzeRequest SpaceUsageAnalyzeRequest 请求参数
     * @param loginUser                当前登录用户
     * @return SpaceUsageAnalyzeResponse 分析结果
     */
    SpaceUsageAnalyzeResponse getSpaceUsageAnalyze(SpaceUsageAnalyzeRequest spaceUsageAnalyzeRequest, User loginUser);

    /**
     * 空间图片分类分析
     * @param spaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest 请求参数
     * @param loginUser 当前登录用户
     * @return 分析结果
     */
    List<SpaceCategoryAnalyzeResponse> getSpaceCategoryAnalyze(SpaceCategoryAnalyzeRequest spaceCategoryAnalyzeRequest, User loginUser);

    /**
     * 空间图片标签分析
     *
     * @param spaceTagAnalyzeRequest spaceTagAnalyzeRequest 请求对象
     * @param loginUser 当前登录用户
     * @return 分析结果
     */
    List<SpaceTagAnalyzeResponse> getSpaceTagAnalyze(SpaceTagAnalyzeRequest spaceTagAnalyzeRequest, User loginUser);

    /**
     * 空间图片大小分析
     *
     * @param spaceSizeAnalyzeRequest spaceSizeAnalyzeRequest 请求对象
     * @param loginUser 当前登录用户
     * @return 分析结果
     */
    List<SpaceSizeAnalyzeResponse> getSpaceSizeAnalyze(SpaceSizeAnalyzeRequest spaceSizeAnalyzeRequest, User loginUser);

    /**
     * 用户上传行为分析
     *
     * @param spaceUserAnalyzeRequest spaceUserAnalyzeRequest 请求对象
     * @param loginUser 当前登录用户
     * @return 分析结果
     */
    List<SpaceUserAnalyzeResponse> getSpaceUserAnalyze(SpaceUserAnalyzeRequest spaceUserAnalyzeRequest, User loginUser);

    List<Space> getSpaceRankAnalyze(SpaceRankAnalyzeRequest spaceRankAnalyzeRequest, User loginUser);

    /**
     * 空间权限校验
     *
     * @param loginUser 当前登录用户
     * @param space 空间
     */
    void checkSpaceAuth(User loginUser, Space space);
}
