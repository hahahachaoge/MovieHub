# 第08步：电影排行（后端+前端）

## 目标

实现电影排行榜功能，支持本周、本月、全部、好评排行四种维度，后端缓存排行数据定时刷新，前端使用标签页切换展示。

## 核心产出

| 文件 | 作用 |
|------|------|
| `dto/RankingVO.java` | 排行返回VO（排名序号、电影信息、播放量/评分） |
| `service/RankingService.java` | 排行服务接口 |
| `service/impl/RankingServiceImpl.java` | 排行服务实现（DB聚合查询 + Redis缓存 + @Scheduled定时刷新） |
| `controller/RankingController.java` | 排行API控制器 |
| `api/ranking.ts` | 排行API调用封装 |
| `components/RankingTable.vue` | 排行表格组件（前三名金银铜牌样式） |
| `views/Rankings.vue` | 排行页面（el-tabs切换四个维度） |

## API接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/ranking/weekly` | 本周排行（周一00:00至今） |
| GET | `/api/ranking/monthly` | 本月排行 |
| GET | `/api/ranking/all` | 全部排行（按play_count） |
| GET | `/api/ranking/top-rated` | 好评排行（按rating，需rating_count>10） |

## 关键代码说明

- **RankingServiceImpl.java**: 周/月排行使用 `play_record` 表按日期范围聚合 COUNT，全部排行直接查 `movie.play_count`，好评排行查 `movie.rating`。结果缓存 Redis 10分钟（好评1小时），`@Scheduled` 定时任务每10分钟清除缓存强制刷新。
- **RankingTable.vue**: 前三行显示 🥇🥈🥉 奖牌符号，电影行可点击跳转详情，评分列带星星图标。

## 注意事项

- 周/月排行依赖 `play_record` 表的 `watch_date` 字段和 `UNIQUE KEY`（避免同一天重复计数）
- 好评排行过滤 `rating_count > 10` 防止评分次数过少导致排名失真
