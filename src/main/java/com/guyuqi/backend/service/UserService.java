package com.guyuqi.backend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.guyuqi.backend.model.dto.user.UserQueryRequest;
import com.guyuqi.backend.model.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;
import com.guyuqi.backend.model.vo.user.LoginUserVO;
import com.guyuqi.backend.model.vo.user.UserVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author guyuqi
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2026-05-30 14:31:07
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 用户登录
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request      请求对象
     * @return 登录用户信息
     */
    LoginUserVO userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 加密密码
     * @param userPassword 用户密码
     * @return 加密后的密码
     */
    String getEncryptPassword(String userPassword);

    /**
     * 获得脱敏后的登录用户信息
     * @param user 用户信息
     * @return 脱敏后的登录用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取当前登录用户
     *
     * @param request 请求对象
     * @return 当前登录用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request 请求对象
     * @return 注销结果
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取脱敏后的用户信息
     * @param user 用户信息
     * @return 脱敏后的用户信息
     */
    UserVO getUserVO(User user);

    /**
     * 获取脱敏后的用户信息列表
     * @param userList 用户信息列表
     * @return 脱敏后的用户信息列表
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 构建查询条件
     * @param userQueryRequest 用户查询请求对象
     * @return 查询条件
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);
}
