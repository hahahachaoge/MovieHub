# 第04步：用户注册/登录后端

## 目标

实现用户模块的后端完整功能，包括注册、登录、退出、用户信息查询和更新。结合 JWT + Redis 实现安全的Token认证。

## 核心产出

| 文件 | 作用 |
|------|------|
| `dto/RegisterDTO.java` | 注册请求体（含 @Valid 校验注解） |
| `dto/LoginDTO.java` | 登录请求体 |
| `dto/UserVO.java` | 用户信息返回对象（脱敏，不含密码） |
| `mapper/UserMapper.java` | 用户数据访问（继承 BaseMapper） |
| `service/UserService.java` | 用户服务接口 |
| `service/impl/UserServiceImpl.java` | 用户服务实现 |
| `controller/UserController.java` | 用户接口控制器 |

## API接口

| 方法 | 路径 | 认证 | 说明 |
|------|------|------|------|
| POST | `/api/user/register` | 无需 | 注册（username/password/email/phone）→ 返回Token |
| POST | `/api/user/login` | 无需 | 登录（username/password）→ 返回Token |
| POST | `/api/user/logout` | 需要 | 退出（清除Redis中的Token）|
| GET | `/api/user/info` | 需要 | 获取当前用户信息 |
| PUT | `/api/user/update` | 需要 | 更新用户信息（邮箱、手机号等）|

## 关键代码说明

- **UserServiceImpl.java**: 注册时使用 `BCryptPasswordEncoder` 加密密码，`QueryWrapper` 检查用户名和邮箱唯一性；登录成功生成 JWT token 并存入 Redis（24小时过期），注销时从 Redis 删除
- **UserController.java**: `logout` 和 `info` 接口从 `Authorization` 头提取 Bearer Token，拦截器已预先校验通过；`update` 使用 `@RequestAttribute("userId")` 获取拦截器解析的用户ID

## 测试要点

1. 注册新用户：`POST /api/user/register` 传入 `{"username":"test","password":"123456","email":"test@test.com"}` → 返回200 + token
2. 重复注册：相同用户名再次注册 → 返回400 "用户名已被注册"
3. 登录：`POST /api/user/login` 传入正确凭据 → 返回200 + token
4. 登录：传入错误密码 → 返回401 "用户名或密码错误"
5. 获取信息：`GET /api/user/info` 带 Authorization header → 返回用户信息（无密码）
6. 退出：`POST /api/user/logout` → 返回200；再次使用同一token访问 → 返回401

## 注意事项

- 密码使用 `spring-security-crypto` 的 `BCryptPasswordEncoder`，不是明文存储
- Token 采用 JWT（jjwt 0.12.5）+ Redis 双层校验
- Redis key 格式 `user:token:{token}`，与拦截器校验一致
- 注册时默认角色为 `NORMAL`（普通用户）
