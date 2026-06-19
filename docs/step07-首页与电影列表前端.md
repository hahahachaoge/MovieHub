# 第07步：首页与电影列表前端

## 目标

实现电影网站的首页展示（轮播图、热播推荐、分类浏览、最新上线）和电影列表页（按分类/地区筛选、分页、搜索）。

## 核心产出

| 文件 | 作用 |
|------|------|
| `api/movie.ts` | 电影相关API调用封装（含TypeScript类型定义） |
| `stores/movie.ts` | 电影状态管理 |
| `components/MovieCard.vue` | 电影卡片组件（封面、评分、VIP角标、分类标签、悬浮动效） |
| `components/MovieCarousel.vue` | 首页轮播图组件（自动轮播、渐变覆盖层、电影信息展示） |
| `views/Home.vue` | 首页页面（轮播 + 三个展示区域） |
| `views/MovieList.vue` | 电影列表页面（筛选栏 + 网格 + 分页） |

## 关键特性

- **MovieCard.vue**: 2:3海报比例，图片懒加载，VIP金色角标，评分展示，分类标签，悬浮上移动效 + 图片缩放
- **MovieCarousel.vue**: 4秒自动轮播，左侧渐变遮罩展示电影信息，点击跳转详情
- **Home.vue**: 并行请求热播/最新/分类数据，分类卡片使用emoji图标，响应式网格
- **MovieList.vue**: 分类标签组（全部/动作/喜剧等）、地区下拉选择、搜索关键词、分页组件、路由query参数同步

## UI/UX要点

- 全站深色主题：背景 `#1a1a2e` → `#16213e` 渐变
- 卡片圆角12px，悬浮上移6px + 阴影增强
- 响应式：PC 6列 → 平板4列 → 手机2列
- 加载态使用骨架屏 `el-skeleton`
- 空数据使用 `el-empty`
- 页面切换使用 `fade-slide` 过渡动画

## 注意事项

- `MovieCard.vue` 使用 `el-image` + `lazy` 实现图片懒加载
- `MovieList.vue` 通过 `route.query` 同步筛选条件，支持直接URL访问
- 分类图标使用 emoji，无需额外图标库
