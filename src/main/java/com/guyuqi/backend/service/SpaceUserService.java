package com.guyuqi.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guyuqi.backend.model.dto.spaceuser.SpaceUserAddRequest;
import com.guyuqi.backend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.guyuqi.backend.model.entity.SpaceUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guyuqi.backend.model.vo.space.SpaceUserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author guyuqi
* @description 针对表【space_user(空间用户关联)】的数据库操作Service
* @createDate 2026-06-17 12:29:03
*/
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间成员
     * @param spaceUserAddRequest spaceUserAddRequest 请求对象
     * @return 成员 id
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 校验空间成员对象
     * @param spaceUser 空间成员
     * @param add 是否为添加
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 拼接查询请求
     * @param spaceUserQueryRequest spaceUserQueryRequest 请求对象
     * @return 查询请求
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间成员封装类
     * @param spaceUser 空间用户
     * @param request HTTP 响应
     * @return 空间成员 VO
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间成员封装类
     * @param spaceUserList 空间用户列表
     * @return 空间成员 VO 列表
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
