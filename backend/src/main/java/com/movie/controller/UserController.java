package com.movie.controller;

import com.movie.common.Result;
import com.movie.dto.LoginDTO;
import com.movie.dto.RegisterDTO;
import com.movie.dto.UserVO;
import com.movie.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.movie.mapper.UserMapper;
import com.movie.entity.User;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("/list")
    public Result<List<User>> list(@RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return Result.success(userMapper.selectList(new QueryWrapper<User>().like("username", keyword)));
        }
        return Result.success(userMapper.selectList(null));
    }

    @PutMapping("/set-status")
    public Result<Void> setStatus(@RequestBody Map<String, Object> body) {
        Long id = Long.valueOf(body.get("id").toString());
        Integer status = Integer.valueOf(body.get("status").toString());
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
        return Result.success();
    }

    @PostMapping("/register")
    public Result<String> register(@Valid @RequestBody RegisterDTO registerDTO) {
        String token = userService.register(registerDTO);
        return Result.success(token);
    }

    @PostMapping("/login")
    public Result<String> login(@Valid @RequestBody LoginDTO loginDTO) {
        String token = userService.login(loginDTO);
        return Result.success(token);
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        userService.logout(token);
        return Result.success();
    }

    @GetMapping("/info")
    public Result<UserVO> info(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        UserVO userVO = userService.getCurrentUser(token);
        return Result.success(userVO);
    }

    @PutMapping("/update")
    public Result<Void> update(@RequestAttribute("userId") Long userId,
                                @RequestBody com.movie.entity.User user) {
        userService.updateUser(userId, user);
        return Result.success();
    }
}
