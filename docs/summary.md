# 📋 项目总结报告

## 项目概述

| 项目 | 内容 |
|------|------|
| 项目名称 | MovieHub - 电影网站 |
| 技术栈 | Spring Boot 3.2.5 + MyBatis Plus 3.5.5 + Redis 7 + MySQL 8 + Vue 3 + Element Plus |
| 开发语言 | Java 17 (后端) + TypeScript (前端) |
| 构建工具 | Maven (后端) + Vite (前端) |
| 完成时间 | 2026年6月 |

---

## 一、系统架构

### 1.1 后端分层架构

```
Controller (接收请求、参数校验)
    ↓
Service (业务逻辑、事务管理)
    ↓
Mapper/DAO (MyBatis Plus 数据访问)
    ↓
MySQL (持久化存储) + Redis (缓存/Token)
```

### 1.2 前端组件架构

```
App.vue (根组件)
├── NavBar.vue (导航栏 — 分类下拉、用户菜单、VIP标签)
├── Router View (页面路由)
│   ├── Home.vue (首页 — 轮播图 + 热播推荐 + 分类浏览 + 最新上线)
│   ├── Login/Register.vue (登录/注册 — 表单校验 + 演示账号一键登录)
│   ├── MovieList.vue (电影列表 — 分类标签 + 地区筛选 + 搜索 + 分页)
│   ├── MovieDetail.vue (电影详情 — 海报 + 主创 + 评论 + 播放 + VIP引导)
│   ├── Rankings.vue (排行榜 — 周/月/全部/好评四维度切换)
│   ├── CrewSearch.vue (主创查询 — 搜索 + 详情弹窗 + 作品网格)
│   ├── VideoPlayer.vue (观影页面)
│   ├── Payment.vue (充值中心 — 三档套餐卡片)
│   ├── PaymentResult.vue (支付结果页)
│   ├── Statistics.vue (数据统计 — ECharts双图表)
│   ├── UserCenter.vue (个人中心 — 播放历史)
│   └── Admin.vue → Dashboard/MovieManage/UserManage (管理后台)
└── FooterBar.vue (页脚)
```

### 1.3 数据库设计 (9张表)

| 表名 | 说明 | 核心字段 |
|------|------|---------|
| user | 用户表 | username, password(BCrypt), role(VIP/NORMAL/ADMIN), vip_expire_time |
| movie | 电影表 | title, cover_url, rating, is_vip, play_count, region (共274部) |
| category | 分类表 | name(动作/喜剧/爱情/科幻/动画/悬疑) |
| movie_type | 电影-分类关联 | movie_id + category_id UNIQUE |
| crew | 主创人员表 | name, role_type(ACTOR/DIRECTOR) (共825人) |
| movie_crew | 电影-主创关联 | movie_id + crew_id + position UNIQUE |
| play_record | 播放记录 | user_id + movie_id + watch_date UNIQUE (每日去重) |
| payment_record | 支付记录 | order_no UNIQUE, status(PENDING/SUCCESS/FAILED) |
| review | 评论表 | user_id + movie_id, rating(1-10), content (无唯一约束支持多次评论) |

### 1.4 认证架构

```
JWT (无状态令牌)
  └─ 签发：登录/注册成功时生成
  └─ 验证：AuthInterceptor 拦截器
  └─ Redis：存储有效 Token（支持踢下线，24h过期）
```

---

## 二、功能实现说明

### 2.1 会员注册、登录、退出 (10分)

- **后端**: 注册时 BCrypt 加密密码，用户名/邮箱唯一校验；登录成功生成 JWT Token 并存入 Redis（24h 过期）；退出时清除 Redis Token
- **前端**: Login.vue 提供表单校验 + 3个演示账号快速登录按钮；Register.vue 含确认密码校验
- **双Token校验**: JWT 验签名 → Redis 验存在，管理员可主动使 Token 失效

### 2.2 主页、影片展示、分页展示 (15分)

- **后端**: 公开 API（热播/分类/地区/搜索/组合筛选/详情/分类列表/地区列表/最新），Redis 缓存热播和详情数据
- **前端**: 首页含轮播图 + 热播推荐 + 分类浏览 + 最新上线；MovieList 支持分类标签 + 地区下拉 + 关键词搜索 + 分页；组合筛选（全部分类+全部地区返回全部274部电影）
- **响应式**: PC 6列 → 平板 4列 → 手机 2列

### 2.3 电影排行 (5分)

- 四种维度：本周（周一至今日）、本月、全部（历史累计）、好评（评分排序）
- 周/月排行基于 play_record 表聚合 COUNT，全部排行基于 movie.play_count
- Redis 缓存 10 分钟，`@Scheduled` 定时任务每 10 分钟刷新
- 前端 el-tabs 切换 + RankingTable 组件（前三名 🥇🥈🥉）

### 2.4 主创作品检索 (5分)

- 按姓名模糊搜索 671 位演员和 154 位导演
- 点击主创查看详情和个人作品列表
- 前端搜索框 + 结果列表 + Dialog 弹窗详情

### 2.5 电影播放权限控制 (10分)

- 后端 PlayServiceImpl 校验：is_vip=1 → 检查用户 role=VIP 或 vip_expire_time 未过期
- 普通用户访问 VIP 影片返回 403 + "请充值后观看"
- **同一用户同一天可多次播放同一影片**（已移除UNIQUE KEY限制，每次播放都新增记录）
- 播放时清除周/月排行缓存，下次请求自动拉最新数据
- 前端 MovieDetail 页面：VIP 影片显示金色锁定按钮 → 弹出充值引导 Dialog

### 2.6 POI 报表导出（加分）

- 使用 Apache POI XSSFWorkbook 生成 .xlsx 文件
- 表头红色背景白色字体，前三名金/银/铜色背景
- **四种排行独立导出**：本周/本月/全部/好评各自导出真实数据，分别统计
- 排行榜页面右上角 "导出 Excel" 按钮

### 2.7 ECharts 图表展示（加分）

- **评分分布图**: ECharts 柱状图，合并统计电影平均分+用户评论评分，五个区间均有数据
- **地区分布图**: 南丁格尔玫瑰图，展示各地区电影数量占比
- 使用 echarts dark 主题

### 2.8 支付宝沙箱支付（加分）

- 三档套餐：月度(30元)、季度(80元/最划算)、年度(288元/省更多)
- 后端使用 RSA2 签名构建支付参数，以 GET 重定向方式跳转支付宝沙箱（URL编码防乱码）
- 异步 notify 处理支付结果（幂等控制），内网穿透使用 NATAPP/花生壳
- **支付成功后返回含VIP角色的新JWT Token**，前端替换旧Token后自动刷新VIP状态
- 管理员(ADMIN)充值不会覆盖管理员角色
- 支付后自动更新用户 role=VIP + 设置/续期 vip_expire_time
- **沙箱测试账号**：商家 `unptgw0002@sandbox.com` / `111111`，买家 `jvftvx0984@sandbox.com` / `111111`（支付密码 `111111`）

### 2.9 Spring AI 整合（加分）

- AI 智能推荐：基于用户观看历史，推荐同分类未看过的电影
- 无历史时推荐热门电影
- application.yml 预留 Spring AI OpenAI 配置（通过环境变量 AI_API_KEY 激活）

### 2.10 其他功能（加分）

- **评论系统**: 用户可评分(1-10)+文字评论，**支持多次评论**（已移除唯一约束）
- **播放历史**: UserCenter.vue 展示用户观看记录
- **数据统计页面**: 整合 ECharts 双图表
- **演示账号快速登录**: Login.vue 一键填充
- **管理后台**: Dashboard 数据概览（含每日播放趋势折线图/用户构成饼图/TOP10播放量条形图）+ 电影管理 + 用户管理
- **演员名单表格**: `actors.html` 含完整 671 人名单，带搜索功能
- **角色名填充**: TMDB API批量查询填充1091条演员角色名，中文覆盖率71.4%
- **关系图谱**: Neo4j图数据库可视化展示电影/演员/导演/分类关系网

---

## 三、项目文件清单

### 后端文件

| 包/目录 | 文件数 | 说明 |
|---------|--------|------|
| controller/ | 11个 | Health, User, Movie, Ranking, Crew, Play, Payment, Review, Report, AI, **Graph** |
| service/ | 7个接口 + 7个实现 | User, Movie, Ranking, Crew, Play, Payment, Report, **Graph** + 对应 impl |
| mapper/ | 9个 | User, Movie, Category, MovieType, Crew, MovieCrew, PlayRecord, PaymentRecord, Review |
| entity/ | 9个 | User, Movie, Category, MovieType, Crew, MovieCrew, PlayRecord, PaymentRecord, Review |
| dto/ | 8个 | RegisterDTO, LoginDTO, UserVO, MovieVO, CrewVO, RankingVO, PlayHistoryVO, VipProductVO |
| config/ | 9个 | Cors, MyBatisPlus, Redis, WebMvc, Alipay, Jwt, Security, MyMetaObjectHandler, **Neo4j** |
| common/ | 4个 | Result, BusinessException, GlobalExceptionHandler, JwtUtil |
| interceptor/ | 1个 | AuthInterceptor |
| enums/ | 3个 | UserRoleEnum, PayStatusEnum, CrewRoleEnum |

### 前端文件

| 目录 | 文件数 | 说明 |
|------|--------|------|
| views/ | 14个 | Login, Register, Home, MovieList, MovieDetail, Rankings, CrewSearch, VideoPlayer, Payment, PaymentResult, Statistics, UserCenter, **Graph**, Admin + admin/子页面 |
| components/ | 5个 | NavBar, FooterBar, MovieCard, MovieCarousel, RankingTable |
| components/Charts/ | 2个 | MovieRatingChart, MovieRegionChart |
| api/ | 7个 | index, movie, ranking, crew, payment, ai, **graph** |
| stores/ | 2个 | user, movie |
| router/ | 1个 | 路由配置 + 路由守卫 |
| styles/ | 1个 | global.css |

---

## 四、小组成员分工

| 班别 | 学号 | 姓名 | 具体分工 |
|------|------|------|---------|
| 2024级软件工程三班 | 202440025318 | 黄彪骐 | 项目架构设计、数据库设计、后端开发、前端开发、UI设计、接口联调、数据搜集、支付对接、文档编写、测试调试 |

---

## 五、遇到的问题和解决方案

### 5.1 跨域问题
**问题**: 前端 5173 端口调用后端 8088 端口被浏览器拦截  
**解决**: 配置 CorsFilter，允许所有 Origin/Headers/Methods

### 5.2 JWT 密钥长度
**问题**: jjwt 0.12.5 要求 HmacSHA256 密钥至少 32 字节  
**解决**: JwtUtil 中检查密钥长度，不足时自动填充到 32 字节

### 5.3 MyBatis Plus 自动填充
**问题**: 实体类的 createTime/updateTime 需要手动设置  
**解决**: 实现 MetaObjectHandler 接口，在 insert/update 时自动填充

### 5.4 Redis 序列化
**问题**: RedisTemplate 默认 JDK 序列化导致数据不可读且体积大  
**解决**: 配置 Jackson2JsonRedisSerializer + JavaTimeModule 支持 LocalDateTime

### 5.5 支付宝签名乱码
**问题**: POST 提交表单时浏览器用 GBK 编码，导致中文乱码验签失败  
**解决**: 改为 GET 重定向，后端用 URLEncoder.encode(value, "UTF-8") 编码所有参数

### 5.6 内网穿透不稳定
**问题**: NATAPP 免费隧道频繁断开，支付宝回调失败  
**解决**: 改用更稳定的花生壳内网穿透，并编写 watchdog 自动保活脚本

### 5.7 评论重复插入
**问题**: 数据库唯一约束导致同一用户不能对同一电影多次评论  
**解决**: 删除唯一索引 `uk_user_movie_review`，支持多次评论

### 5.8 支付成功VIP状态不刷新
**问题**: 支付后用户角色已更新为VIP，但前端仍显示普通用户  
**解决**: mock支付接口返回含VIP角色的新JWT Token，前端替换旧Token后重新获取用户信息

### 5.9 播放记录唯一约束
**问题**: 短时间内多次观看同一电影只记录一次，播放量统计不准  
**解决**: 删除 `play_record` 表的 `uk_user_movie_date` 唯一索引，每次播放都新增记录

### 5.10 报表导出差错
**问题**: 四种排行类型导出数据相同（都按play_count排序）  
**解决**: 改为各自查询真实排行数据：本周/本月从 play_record 聚合，好评按rating排序，全部按play_count排序

### 5.11 排行榜数据不刷新
**问题**: 前端tab切换时只请求一次数据，后续切换直接读缓存  
**解决**: 去掉 `if (length === 0)` 条件，每次切换都重新请求后端（Redis 10分钟缓存兜底性能）

---

## 六、种子数据统计

| 数据类型 | 数量 | 说明 |
|---------|------|------|
| 电影 | 274部 | 含多国多类型，含VIP影片 |
| 分类 | 6个 | 动作、喜剧、爱情、科幻、动画、悬疑 |
| 演员 | 671位 | 含国内外演员 |
| 导演 | 154位 | 含国内外导演 |
| 主创总数 | 825位 | 演员+导演 |
| 测试用户 | 5个 | admin(管理员)、vipuser(VIP)、normal(普通)等 |
| 播放记录 | 700+条 | 各日期的播放数据 |
| 评论 | 1177+条 | 各区间均有分布 |
| 电影-主创关联 | 1365条 | 多对多关联数据 |
