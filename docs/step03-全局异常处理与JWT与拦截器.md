# 第03步：全局异常处理 + JWT工具 + 拦截器

## 目标

搭建后端的全局异常处理机制、JWT令牌管理、请求认证拦截器以及密码加密配置。这些是后续所有业务模块的基础设施。

## 核心产出

| 文件 | 作用 |
|------|------|
| `common/BusinessException.java` | 自定义业务异常（含code和message） |
| `common/GlobalExceptionHandler.java` | 全局异常处理器（@RestControllerAdvice） |
| `common/JwtUtil.java` | JWT令牌生成、解析、验证工具类 |
| `config/JwtConfig.java` | JWT配置属性类（@ConfigurationProperties） |
| `config/SecurityConfig.java` | BCrypt密码编码器 Bean |
| `config/WebMvcConfig.java` | 拦截器注册（配置放行路径） |
| `interceptor/AuthInterceptor.java` | JWT + Redis双层Token校验拦截器 |

## 关键代码说明

- **GlobalExceptionHandler.java**: 覆盖6种异常类型：
  - `MethodArgumentNotValidException` → 400 + 字段错误消息（@Valid校验失败）
  - `BindException` → 400 + 参数绑定错误
  - `MissingServletRequestParameterException` → 400 + 缺少参数名
  - `BusinessException` → 自定义code+message（业务层主动抛出）
  - `AccessDeniedException` → 403 + 无权限提示
  - `Exception` → 500 + 兜底错误消息 + 日志记录完整堆栈

- **JwtUtil.java**: 
  - 使用 jjwt 0.12.5 新版 API（`Jwts.builder().signWith().compact()`）
  - 密钥从配置读取，不足32字节自动填充
  - Token中含 userId(subject) 和 role(claim) 两个信息

- **AuthInterceptor.java**:
  - 从 Header 取 `Authorization: Bearer <token>`
  - 先 JWT 验证签名 → 再 Redis 验证存在性（支持管理员主动踢下线）
  - 验证通过后将 userId 和 role 存入 request attribute
  - 失败时直接返回 401 JSON 响应（不抛出异常）

- **WebMvcConfig.java**: 拦截 `/api/**`，放行登录/注册/公开接口/排行/主创/回调等路径

- **SecurityConfig.java**: 仅引入 `spring-security-crypto`（非完整 Spring Security），提供 `BCryptPasswordEncoder` Bean

## 测试要点

1. 不传 Token 访问需认证接口 → 返回 401 "未提供认证令牌"
2. 传无效 Token 访问 → 返回 401 "令牌无效或已过期"
3. 使用 `curl -X POST http://localhost:8088/api/user/register` 不传必填参数 → 返回 400 + 字段错误消息
4. `BCryptPasswordEncoder.matches("123456", encodedPassword)` 返回 true

## 注意事项

- `spring-security-crypto` 是一个轻量依赖，只包含密码加密功能，不会启用 Spring Security 过滤器链
- 拦截器返回 401 时手动写 JSON 响应，避免全局异常处理器处理认证失败场景
- Redis 中 Token 的 key 格式为 `user:token:{token}`，TTL 24小时（与 JWT 过期时间一致）
