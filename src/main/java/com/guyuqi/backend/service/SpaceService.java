package com.guyuqi.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guyuqi.backend.model.dto.space.SpaceAddRequest;
import com.guyuqi.backend.model.dto.space.SpaceQueryRequest;
import com.guyuqi.backend.model.entity.Space;
import com.guyuqi.backend.model.entity.User;
import com.guyuqi.backend.model.vo.space.SpaceVO;
import jakarta.servlet.http.HttpServletRequest;

/**
* @author guyuqi
* @description 针对表【space(空间)】的数据库操作Service
* @createDate 2026-06-16 19:14:35
*/
public interface SpaceService extends IService<Space> {

    /**
     * 创建空间
     *
     * @param spaceAddRequest 空间创建请求
     * @param loginUser 当前登录用户
     * @return 空间 id
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 校验空间是否合法
     * @param space 空间对象
     * @param add 是否为添加操作
     */
    void validSpace(Space space, boolean add);

    /**
     * 根据空间级别，自动填充限额
     *
     * @param space 空间对象
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 获取空间包装类
     *
     * @param space 空间对象
     * @param request HTTP 请求对象
     * @return 空间包装类
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间包装类分页
     *
     * @param spacePage 空间分页对象
     * @param request HTTP 请求对象
     * @return 空间包装类分页对象
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 拼接空间查询条件
     *
     * @param spaceQueryRequest 空间查询请求对象
     * @return 查询条件
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 校验空间权限
     *
     * @param loginUser 当前登录用户
     * @param space 空间对象
     */
    void checkSpaceAuth(User loginUser, Space space);

    /**
     * 删除空间（关联删除空间内的图片）
     *
     * @param spaceId 空间 id
     * @param loginUser 当前登录用户
     */
    void deleteSpace(long spaceId, User loginUser);
}
