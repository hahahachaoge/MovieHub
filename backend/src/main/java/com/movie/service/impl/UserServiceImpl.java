package com.movie.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.movie.common.BusinessException;
import com.movie.common.JwtUtil;
import com.movie.dto.LoginDTO;
import com.movie.dto.RegisterDTO;
import com.movie.dto.UserVO;
import com.movie.entity.User;
import com.movie.mapper.UserMapper;
import com.movie.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final long TOKEN_EXPIRATION = 24 * 60 * 60; // 24小时（秒）

    @Override
    public String register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        User existingUser = userMapper.selectOne(new QueryWrapper<User>().eq("username", registerDTO.getUsername()));
        if (existingUser != null) {
            throw new BusinessException(400, "用户名已被注册");
        }

        // 检查邮箱是否已存在
        if (StringUtils.hasText(registerDTO.getEmail())) {
            User existingEmail = userMapper.selectOne(new QueryWrapper<User>().eq("email", registerDTO.getEmail()));
            if (existingEmail != null) {
                throw new BusinessException(400, "邮箱已被注册");
            }
        }

        // 创建用户
        User user = new User();
        BeanUtil.copyProperties(registerDTO, user);
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRole("NORMAL");
        user.setStatus(1);
        // 自动生成默认头像（基于用户名首字母）
        user.setAvatar("https://api.dicebear.com/7.x/initials/svg?seed="
                + registerDTO.getUsername().substring(0, Math.min(2, registerDTO.getUsername().length())).toUpperCase()
                + "&backgroundColor=16213e&textColor=ffffff");

        userMapper.insert(user);

        // 生成Token并存入Redis
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        redisTemplate.opsForValue().set("user:token:" + token, user.getId(), TOKEN_EXPIRATION, TimeUnit.SECONDS);

        return token;
    }

    @Override
    public String login(LoginDTO loginDTO) {
        // 查找用户
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", loginDTO.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 检查账号状态
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(403, "账号已被禁用");
        }

        // 校验密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 生成Token并存入Redis
        String token = jwtUtil.generateToken(user.getId(), user.getRole());
        redisTemplate.opsForValue().set("user:token:" + token, user.getId(), TOKEN_EXPIRATION, TimeUnit.SECONDS);

        return token;
    }

    @Override
    public void logout(String token) {
        // 从Redis删除Token
        redisTemplate.delete("user:token:" + token);
    }

    @Override
    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    @Override
    public UserVO getCurrentUser(String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new BusinessException(401, "无效的令牌");
        }

        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = getUserById(userId);

        UserVO userVO = new UserVO();
        BeanUtil.copyProperties(user, userVO);
        if (user.getCreateTime() != null) {
            userVO.setCreateTime(user.getCreateTime().toString());
        }
        if (user.getVipExpireTime() != null) {
            userVO.setVipExpireTime(user.getVipExpireTime().toString());
        }

        return userVO;
    }

    @Override
    public void updateUser(Long userId, User user) {
        User existingUser = getUserById(userId);
        // 只允许更新部分字段
        if (user.getEmail() != null) {
            existingUser.setEmail(user.getEmail());
        }
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        userMapper.updateById(existingUser);
    }
}
