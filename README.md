# 🎬 MovieHub - 电影网站

> 基于 **Spring Boot 3.2.5 + MyBatis Plus 3.5.5 + Redis 7 + Vue 3 + Element Plus** 的全栈电影网站项目  
> 📌 2024软件工程《Java框架技术》期末作品

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=flat&logo=spring-boot&logoColor=white)
![MyBatis Plus](https://img.shields.io/badge/MyBatis_Plus-3.5.5-blue)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=flat&logo=mysql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-7.0-DC382D?style=flat&logo=redis&logoColor=white)
![Vue 3](https://img.shields.io/badge/Vue_3-4FC08D?style=flat&logo=vue.js&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.0-3178C6?style=flat&logo=typescript&logoColor=white)
![Element Plus](https://img.shields.io/badge/Element_Plus-2.5-409EFF?style=flat)

---

## 📖 项目简介

MovieHub 是一个功能完整的电影网站，支持会员注册/登录、影片分类浏览（274部电影，含筛选/搜索/分页）、电影排行榜（本周/本月/全部/好评四种维度）、主创作品检索（671名演员+154名导演）、VIP会员播放权限控制、支付宝沙箱支付、评论系统（评分+文字，可多次评论）、POI报表导出、ECharts数据可视化、Spring AI智能推荐、Neo4j关系图谱可视化等。

### 项目截图

| 页面 | 预览 |
|------|------|
| 🏠 首页 | ![首页](界面截图/image.png) |
| 🎬 热播推荐 | ![热播推荐](界面截图/image-1.png) |
| 📂 分类浏览 | ![分类浏览](界面截图/image-2.png) |
| 🆕 最新上线 | ![最新上线](界面截图/image-3.png) |
| 🎞️ 电影分类 | ![电影分类](界面截图/image-4.png) |
| 🔍 电影搜索 | ![电影搜索](界面截图/image-5.png) |
| 🏆 本周排行 | ![本周排行](界面截图/image-6.png) |
| 🏆 本月排行 | ![本月排行](界面截图/image-7.png) |
| 🏆 全部排行 | ![全部排行](界面截图/image-8.png) |
| ⭐ 好评排行 | ![好评排行](界面截图/image-9.png) |
| 🎭 主创查询 | ![主创查询](界面截图/image-10.png) |
| 🤖 AI搜索 | ![AI搜索](界面截图/image-11.png) |
| 📊 数据统计 | ![数据统计](界面截图/image-12.png) |
| 🕸️ 关系图谱 | ![关系图谱](界面截图/image-13.png) |
| 🕸️ 人物关系 | ![人物关系](界面截图/image-14.png) |
| 👤 个人中心 | ![个人中心](界面截图/image-15.png) |
| 🖼️ 更新头像 | ![更新头像](界面截图/image-16.png) |
| 📋 浏览历史 | ![浏览历史](界面截图/image-17.png) |
| 💎 升级VIP | ![升级VIP](界面截图/image-18.png) |
| 📈 数据概览 | ![数据概览1](界面截图/image-19.png) |
| 📈 数据详情 | ![数据详情](界面截图/image-20.png) |
| 🎬 电影管理 | ![电影管理](界面截图/image-21.png) |
| ✅ 上下架 | ![上下架](界面截图/image-22.png) |
| 👥 用户管理 | ![用户管理](界面截图/image-23.png) |
| 🚫 用户禁用 | ![用户禁用](界面截图/image-24.png) |
| 🔑 用户登录 | ![用户登录](界面截图/image-25.png) |
| 📝 用户注册 | ![用户注册](界面截图/image-26.png) |
---

## ✨ 功能特性

### 核心功能

- [x] **会员注册、登录、退出** — JWT + Redis 双 Token 认证，区分 VIP/普通/管理员账号
- [x] **影片展示** — 274部电影，按热播排行、类型分类、地区分类展示，支持分页（含组合筛选：分类+地区+关键词）
- [x] **电影排行** — 本周排行、本月排行、全部排行、好评排行（Redis缓存+定时刷新）
- [x] **主创作品检索** — 按演员或导演姓名搜索，查看详情和其参与的所有电影
- [x] **电影播放权限控制** — VIP影片限制，普通账号充值后可观看，不限播放次数
- [x] **电影详情页** — 全宽海报、主创列表（可点击跳转）、评论系统（评分+文字，可多次评论）、VIP充值引导

### 加分功能

- [x] **POI 报表导出** — 生成 Excel 电影排行榜（含金/银/铜色样式），四种排行独立导出
- [x] **ECharts 图表** — 评分分布柱状图（电影+评论双源合并）+ 地区分布南丁格尔玫瑰图
- [x] **支付宝沙箱支付** — 三档 VIP 套餐（月/季/年），沙箱环境支付，GET跳转防乱码，支付后自动刷新VIP Token
- [x] **Spring AI 整合** — 基于观看历史的 AI 智能推荐 + AI 搜索
- [x] **数据统计页面** — 可视化展示电影数据（双图表）
- [x] **评论系统** — 可多次评论，评分1-10星 + 文字内容
- [x] **管理后台** — Dashboard 数据概览（含图表）+ 电影管理 + 用户管理（管理员专属）
- [x] **播放历史** — 用户在个人中心查看历史观看记录
- [x] **关系图谱** — Neo4j图数据库可视化展示电影、演员、导演、分类关系网

---

## 🛠️ 技术栈

### 后端

| 技术 | 版本 | 用途 |
|------|------|------|
| Java | 17 | 开发语言 |
| Spring Boot | 3.2.5 | 后端框架 |
| MyBatis Plus | 3.5.5 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | 7.0 | 缓存 + Token 存储 |
| JWT (jjwt) | 0.12.5 | 令牌认证 |
| Apache POI | 5.2.5 | Excel 报表导出 |
| Spring AI | 1.0.0-M6 | AI 智能推荐 |
| Neo4j | 5.15.0 | 图数据库 + 关系图谱 |
| Hutool | 5.8.26 | 工具类库 |

### 前端

| 技术 | 版本 | 用途 |
|------|------|------|
| Vue 3 | 3.4+ | 前端框架（Composition API + script setup） |
| TypeScript | 5.0+ | 类型安全 |
| Vite | 5.0+ | 构建工具 |
| Element Plus | 2.5+ | UI 组件库 |
| ECharts | 5.4+ | 数据可视化 |
| Pinia | 2.1+ | 状态管理 |
| Axios | 1.6+ | HTTP 请求 |
| Vue Router | 4.3+ | 路由管理 |

---

## 📁 项目结构

```
movie-website/
├── backend/                          # 后端（Spring Boot）
│   ├── pom.xml                       # Maven 依赖
│   ├── backend.log                   # 运行时日志
│   └── src/main/java/com/movie/
│       ├── MovieApplication.java     # 启动类
│       ├── controller/               # 控制器层
│       │   ├── UserController.java   # 用户注册/登录/退出
│       │   ├── MovieController.java  # 电影展示/筛选/搜索/详情
│       │   ├── RankingController.java# 排行榜（本周/本月/全部/好评）
│       │   ├── CrewController.java   # 主创检索
│       │   ├── PlayController.java   # 播放权限 + 播放历史
│       │   ├── PaymentController.java# 支付宝支付（创建/通知/模拟）
│       │   ├── ReviewController.java # 评论（获取/提交，支持多次评论）
│       │   ├── ReportController.java # 报表导出 + 图表数据
│       │   ├── AIController.java     # AI推荐 + AI搜索
│       │   ├── GraphController.java  # Neo4j关系图谱
│       │   └── HealthController.java # 健康检查
│       ├── service/                  # 服务层接口 + 实现
│       ├── mapper/                   # 数据访问层（9个Mapper）
│       ├── entity/                   # 实体类（9张表）
│       ├── dto/                      # 数据传输对象（VO + DTO）
│       ├── config/                   # 配置类（CORS/Redis/JWT/MyBatisPlus/Neo4j等）
│       ├── common/                   # 公共工具（Result/BizException/GlobalHandler/JwtUtil）
│       ├── interceptor/              # AuthInterceptor（JWT+Redis双层校验）
│       └── enums/                    # 枚举（UserRole/PayStatus/CrewRole）
├── frontend/                         # 前端（Vue 3 + TypeScript）
│   ├── package.json
│   ├── vite.config.ts                # Vite配置（API代理到8088）
│   └── src/
│       ├── views/                    # 页面组件
│       │   ├── Home.vue              # 首页（轮播+热播+分类+最新）
│       │   ├── Login.vue             # 登录（含演示账号快速登录）
│       │   ├── Register.vue          # 注册（含确认密码校验）
│       │   ├── MovieList.vue         # 电影列表（分类/地区/搜索/分页）
│       │   ├── MovieDetail.vue       # 电影详情（海报/评论/播放/VIP引导）
│       │   ├── Rankings.vue          # 排行榜（四维度切换）
│       │   ├── CrewSearch.vue        # 主创查询
│       │   ├── VideoPlayer.vue       # 观影页面
│       │   ├── Payment.vue           # 充值中心（三档套餐）
│       │   ├── PaymentResult.vue     # 支付结果页
│       │   ├── Statistics.vue        # 数据统计（双图表）
│       │   ├── Graph.vue             # Neo4j关系图谱页面
│       │   ├── UserCenter.vue        # 个人中心
│       │   ├── Admin.vue             # 管理后台布局
│       │   └── admin/                # 管理后台子页面
│       │       ├── Dashboard.vue     # 数据概览
│       │       ├── MovieManage.vue   # 电影管理
│       │       └── UserManage.vue    # 用户管理
│       ├── components/               # 可复用组件
│       │   ├── NavBar.vue            # 顶部导航栏（分类下拉/用户菜单）
│       │   ├── FooterBar.vue         # 页脚
│       │   ├── MovieCard.vue         # 电影卡片
│       │   ├── MovieCarousel.vue     # 轮播图
│       │   ├── RankingTable.vue      # 排行表格
│       │   └── Charts/               # ECharts图表组件
│       │       ├── MovieRatingChart.vue
│       │       └── MovieRegionChart.vue
│       ├── api/                      # Axios 接口封装
│       │   ├── index.ts              # Axios实例（拦截器/Token管理）
│       │   ├── movie.ts              # 电影API
│       │   ├── ranking.ts            # 排行API
│       │   ├── crew.ts               # 主创API
│       │   ├── payment.ts            # 支付API
│       │   └── ai.ts                 # AI API
│       ├── stores/                   # Pinia 状态管理
│       │   ├── user.ts               # 用户状态（登录/注册/退出/信息）
│       │   └── movie.ts              # 电影状态
│       ├── router/index.ts           # 路由配置 + 路由守卫
│       └── styles/global.css         # 全局样式（深色主题/CSS变量）
├── database/                         # 数据库
│   └── init.sql                      # 完整建表DDL + 种子DML
├── docs/                             # 项目文档
│   ├── step01-项目初始化与环境搭建.md
│   ├── step02-数据库建表与种子数据.md
│   ├── step03-全局异常处理与JWT与拦截器.md
│   ├── step04-用户注册登录后端.md
│   ├── step05-用户注册登录前端.md
│   ├── step06-电影展示API后端.md
│   ├── step07-首页与电影列表前端.md
│   ├── step08-电影排行后端+前端.md
│   ├── step09-主创作品检索.md
│   ├── step10-11-播放权限与详情页.md
│   ├── step12-13-支付宝沙箱支付.md
│   ├── step16-17-18-AI与后台与文档.md
│   └── summary.md                    # 项目总结报告
├── natapp.exe                        # NATAPP内网穿透客户端
├── config.ini                        # NATAPP运行配置
├── 分工.txt                          # 具体分工（独立完成）
└── README.md                         # 本文件
```

---

## 🚀 快速开始

### 环境要求

| 环境 | 版本要求 |
|------|---------|
| JDK | 17 或更高 |
| Maven | 3.6+ |
| Node.js | 18+ |
| npm | 9+ |
| MySQL | 8.0+ |
| Redis | 7.0+ |

### 第一步：数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 执行初始化脚本（建库+建表+种子数据）
source database/init.sql
```

### 第二步：启动 Redis

```bash
redis-server
```

### 第三步：配置环境变量

```bash
cd backend

# 复制环境变量模板
cp .env.example .env

# 编辑 .env 填入你的真实配置（数据库密码、API Key 等）
# .env 已加入 .gitignore，不会被提交
```

支付宝密钥文件需放入 `backend/certs/` 目录：
```bash
certs/alipay-private-key.pem     # 应用私钥（PKCS8格式）
certs/alipay-public-key.pem      # 支付宝公钥
```

### 第四步：启动后端

```bash
cd backend
mvn spring-boot:run
```

后端启动后访问：`http://localhost:8088/api/health` 验证是否成功

### 第五步：启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动后访问：`http://localhost:5173`

### 第六步：内网穿透（如需支付宝回调）

支付宝沙箱支付需要异步回调地址可公网访问：**NATAPP**
1. 参考 `config.ini.example` 创建 `config.ini`，填入你的 authtoken
2. 双击 `natapp.exe` 启动隧道

编辑 `backend/src/main/resources/application.yml` 的 `alipay` 配置：

```yaml
alipay:
  app-id: 你的沙箱APPID
  private-key: |
    -----BEGIN PRIVATE KEY-----
    你的应用私钥（PKCS8格式）
    -----END PRIVATE KEY-----
  alipay-public-key: |
    -----BEGIN PUBLIC KEY-----
    支付宝公钥（在沙箱页面上传应用公钥后获取）
    -----END PUBLIC KEY-----
  notify-url: http://你的穿透地址/api/payment/notify
  return-url: http://localhost:5173/payment/result
  gateway-url: https://openapi-sandbox.dl.alipaydev.com/gateway.do
```

---

## 🧪 支付宝沙箱测试账号

| 类型 | 账号 | 密码 |
|------|------|------|
| 商家账号 | `unptgw0002@sandbox.com` | `111111` |
| 买家账号 | `jvftvx0984@sandbox.com` | `111111`（支付密码 `111111`） |

---

## 🔑 演示账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | 管理员 | 可访问管理后台（/admin） |
| vipuser | 123456 | VIP会员 | 可观看VIP影片 |
| normal | 123456 | 普通用户 | 需充值后观看VIP影片 |

---

## 📊 数据统计

| 数据类型 | 数量 |
|---------|------|
| 电影 | 274部（含多国多类型） |
| 演员 | 671位 |
| 导演 | 154位 |
| 分类 | 6个（动作/喜剧/爱情/科幻/动画/悬疑） |
| 评论 | 1451+条 |
| 播放记录 | 780+条 |
| 支付记录 | 30+条 |
| 用户 | >=5个 |

---

## 📋 评分标准覆盖

| 评分项 | 分值 | 对应实现 |
|--------|------|---------|
| 数据库设计及数据搜集 | 15 | 9张表 + 274部电影 + 825位主创 + 完整种子数据 |
| 会员注册、登录、退出 | 10 | JWT + Redis 双Token，密码BCrypt加密 |
| 主页、影片展示、分页展示 | 15 | 热播排行 + 6分类筛选 + 地区筛选 + 关键词搜索 + 分页 |
| 电影排行 | 5 | 本周/本月/全部/好评四种维度，Redis缓存定时刷新 |
| 主创作品 | 5 | 671演员+154导演搜索 + 详情 + 作品列表 |
| 播放权限控制 | 10 | VIP权限校验 + 过期检测 + 充值引导 + 多次播放记录 |
| POI报表 | 加分 | Excel导出（金/银/铜色样式），四种排行独立导出 |
| ECharts图表 | 加分 | 评分分布柱状图 + 地区分布玫瑰图 |
| 支付宝沙箱支付 | 加分 | 三档套餐 + 沙箱支付 + 异步回调 + GET跳转防乱码 + 自动刷新Token |
| Spring AI整合 | 加分 | 基于观看历史的智能推荐 + AI搜索 |
| Neo4j关系图谱 | 加分 | 图数据库可视化展示电影/演员/导演/分类关系网 |
| 其他功能加分 | 加分 | 多次评论、播放历史、数据统计页面、管理后台 |

---

## 📄 文档索引

| 文档 | 说明 |
|------|------|
| [docs/step01-项目初始化与环境搭建.md](docs/step01-项目初始化与环境搭建.md) | 项目骨架搭建 |
| [docs/step02-数据库建表与种子数据.md](docs/step02-数据库建表与种子数据.md) | 数据库设计 |
| [docs/step03-全局异常处理与JWT与拦截器.md](docs/step03-全局异常处理与JWT与拦截器.md) | 基础设施 |
| [docs/step04-用户注册登录后端.md](docs/step04-用户注册登录后端.md) | 用户模块后端 |
| [docs/step05-用户注册登录前端.md](docs/step05-用户注册登录前端.md) | 用户模块前端 |
| [docs/step06-电影展示API后端.md](docs/step06-电影展示API后端.md) | 电影API |
| [docs/step07-首页与电影列表前端.md](docs/step07-首页与电影列表前端.md) | 首页前端 |
| [docs/step08-电影排行后端+前端.md](docs/step08-电影排行后端+前端.md) | 排行榜 |
| [docs/step09-主创作品检索.md](docs/step09-主创作品检索.md) | 主创检索 |
| [docs/step10-11-播放权限与详情页.md](docs/step10-11-播放权限与详情页.md) | 播放权限+评论 |
| [docs/step12-13-支付宝沙箱支付.md](docs/step12-13-支付宝沙箱支付.md) | 支付功能 |
| [docs/step14-15-关系图谱.md](docs/step14-15-关系图谱.md) | Neo4j关系图谱 |
| [docs/step16-17-18-AI与后台与文档.md](docs/step16-17-18-AI与后台与文档.md) | AI/后台/文档 |
| [docs/summary.md](docs/summary.md) | 项目总结报告 |
| [docs/分工.txt](docs/分工.txt) | 小组成员分工 |

---

## 📝 License

本项目为 Java 框架技术课程期末作品，仅供学习交流使用。
