# 第01步：项目初始化与环境搭建

## 目标

搭建电影网站项目的完整开发环境，包括后端 Spring Boot 项目骨架、前端 Vue 3 + TypeScript 项目骨架、以及项目基础设施（统一返回、跨域配置、Redis/MyBatis Plus 配置等）。

## 核心产出

### 后端 (backend/)
| 文件 | 作用 |
|------|------|
| `pom.xml` | Maven 依赖管理（Spring Boot 3.2.5, MyBatis Plus, Redis, JWT, POI, Spring AI 等） |
| `src/main/resources/application.yml` | 主配置文件（MySQL、Redis、JWT、支付宝、AI 配置） |
| `src/main/resources/application-dev.yml` | 开发环境配置 |
| `src/main/java/com/movie/MovieApplication.java` | Spring Boot 启动类 |
| `src/main/java/com/movie/common/Result.java` | 统一返回结果封装 |
| `src/main/java/com/movie/config/CorsConfig.java` | 跨域配置 |
| `src/main/java/com/movie/config/MyBatisPlusConfig.java` | MyBatis Plus 分页插件 |
| `src/main/java/com/movie/config/RedisConfig.java` | Redis 序列化配置 |
| `src/main/java/com/movie/controller/HealthController.java` | 健康检查接口 |

### 前端 (frontend/)
| 文件 | 作用 |
|------|------|
| `package.json` | 依赖管理（Vue 3, Element Plus, ECharts, Pinia, Axios 等） |
| `vite.config.ts` | Vite 配置（路径别名、API 代理到 localhost:8088） |
| `src/main.ts` | 入口文件（注册 Pinia、Router、Element Plus、图标库） |
| `src/App.vue` | 根组件（导航栏 + 路由视图 + 底部） |
| `src/router/index.ts` | 路由配置（含路由守卫，需认证页面自动跳转登录） |
| `src/api/index.ts` | Axios 实例（请求/响应拦截器、Token管理、统一错误处理） |
| `src/stores/user.ts` | 用户状态管理（login/register/logout/fetchUserInfo） |
| `src/styles/global.css` | 全局样式（深色主题、CSS变量、响应式布局） |
| `src/components/NavBar.vue` | 顶部导航栏（Logo、电影分类下拉、用户信息、VIP标签） |
| `src/components/FooterBar.vue` | 底部版权信息 |

### 其他
| 文件 | 作用 |
|------|------|
| `.gitignore` | Git 忽略规则 |

## 关键代码说明

- **Result.java**: 泛型统一返回类 `{code, message, data, timestamp}`，提供 `success()`、`error()`、`unauthorized()` 等静态工厂方法。
- **CorsConfig.java**: 使用 `CorsFilter` 允许所有跨域请求（开发环境前端 5173 端口、后端 8088 端口）。
- **MyBatisPlusConfig.java**: 配置分页拦截器 `PaginationInnerInterceptor`，设置最大查询限制 500 条。
- **RedisConfig.java**: 使用 Jackson2JsonRedisSerializer 实现对象序列化，注册 `JavaTimeModule` 支持 `LocalDateTime`。
- **NavBar.vue**: 固定顶部导航栏，包含电影分类动态下拉菜单、用户登录状态显示、VIP 标签。滚动时添加阴影效果。
- **全局样式**: 定义了 CSS 变量统一管理主题色（`--primary-color: #e50914`），电影卡片网格响应式布局。

## 测试要点

1. 后端启动：`cd backend && mvn spring-boot:run`，访问 `http://localhost:8088/api/health` 应返回 `{"code":200,"message":"success","data":{"status":"UP"}}`
2. 前端启动：`cd frontend && npm run dev`，访问 `http://localhost:5173` 应看到带导航栏的首页
3. 导航栏分类下拉应显示空（待 Step 02 数据库初始化后生效）
4. 未登录时右上角显示"登录/注册"按钮

## 注意事项

- 后端需要 Java 17+、Maven 3.6+、MySQL 8+、Redis 7+
- 前端需要 Node.js 18+、npm 9+
- 支付宝沙箱配置需替换为实际沙箱信息（APPID、应用私钥、支付宝公钥）
- 首次启动后端前需确保 MySQL 已建库 `movie_website`，或等 Step 02 执行 `init.sql`
- `application.yml` 中的数据库密码可根据实际情况修改
