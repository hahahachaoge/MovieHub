package com.movie.service;

import com.movie.dto.LoginDTO;
import com.movie.dto.RegisterDTO;
import com.movie.dto.UserVO;
import com.movie.entity.User;

public interface UserService {

    /**
     * 用户注册
     * @param registerDTO 注册信息
     * @return JWT token
     */
    String register(RegisterDTO registerDTO);

    /**
     * 用户登录
     * @param loginDTO 登录信息
     * @return JWT token
     */
    String login(LoginDTO loginDTO);

    /**
     * 用户退出
     * @param token 当前令牌
     */
    void logout(String token);

    /**
     * 根据ID获取用户
     */
    User getUserById(Long id);

    /**
     * 根据Token获取当前用户信息
     */
    UserVO getCurrentUser(String token);

    /**
     * 更新用户信息
     */
    void updateUser(Long userId, User user);
}
