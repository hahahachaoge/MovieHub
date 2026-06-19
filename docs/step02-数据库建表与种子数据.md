# 第02步：数据库建表与种子数据

## 目标

设计并创建电影网站的完整数据库结构（9张表），插入充足的种子数据用于开发和测试，同时创建对应的 Java Entity 类。

## 核心产出

| 文件 | 作用 |
|------|------|
| `database/init.sql` | 完整DDL+DML，包含建库、建表、索引、种子数据（274电影+825主创+1177评论） |
| `backend/.../entity/User.java` | 用户实体 |
| `backend/.../entity/Category.java` | 分类实体 |
| `backend/.../entity/Movie.java` | 电影实体（含非数据库字段：categories、crews） |
| `backend/.../entity/MovieType.java` | 电影-分类关联实体 |
| `backend/.../entity/Crew.java` | 主创人员实体 |
| `backend/.../entity/MovieCrew.java` | 电影-主创关联实体（含非数据库字段：crew） |
| `backend/.../entity/PlayRecord.java` | 播放记录实体 |
| `backend/.../entity/PaymentRecord.java` | 支付记录实体 |
| `backend/.../entity/Review.java` | 评论实体（含非数据库字段：username、userAvatar） |
| `backend/.../enums/UserRoleEnum.java` | 用户角色枚举（VIP/NORMAL/ADMIN） |
| `backend/.../enums/PayStatusEnum.java` | 支付状态枚举（PENDING/SUCCESS/FAILED/REFUND） |
| `backend/.../enums/CrewRoleEnum.java` | 主创角色枚举（ACTOR/DIRECTOR） |
| `backend/.../config/MyMetaObjectHandler.java` | MyBatis Plus自动填充处理器（createTime/updateTime） |

## 数据库表结构

| 表名 | 说明 | 核心字段 |
|------|------|---------|
| `user` | 用户表 | username(UNIQUE), password(BCrypt), role(VIP/NORMAL/ADMIN), vip_expire_time |
| `category` | 电影分类 | name(UNIQUE), description, sort_order |
| `movie` | 电影表 | title, cover_url, description, rating, is_vip, play_count, region |
| `movie_type` | 电影-分类关联 | movie_id(FK), category_id(FK), UNIQUE(movie_id, category_id) |
| `crew` | 主创人员表 | name, role_type(ACTOR/DIRECTOR), bio, birth_date |
| `movie_crew` | 电影-主创关联 | movie_id(FK), crew_id(FK), character_name, position |
| `play_record` | 播放记录 | user_id(FK), movie_id(FK), watch_date, UNIQUE(user_id, movie_id, watch_date) |
| `payment_record` | 支付记录 | order_no(UNIQUE), amount, status, trade_no |
| `review` | 评论表 | user_id(FK), movie_id(FK), rating(1-10), content（无唯一约束，可多次评论） |

## 种子数据

- **6个分类**: 动作、喜剧、爱情、科幻、动画、悬疑
- **274部电影**: 含多国多种类型，覆盖不同评分区间
- **825位主创**: 671位演员 + 154位导演，来自中外影视作品
- **5个测试用户**: admin(管理员, 123456)、vipuser(VIP)、normal(普通用户)等
- **700+条播放记录**: 用于排行测试
- **1177+条评论**: 各评分区间均有分布（0-2分66条，2-4分221条，4-6分301条，6-8分350条，8-10分514条）

## 关键代码说明

- **MyMetaObjectHandler.java**: 继承 `MetaObjectHandler`，自动为实体类的 `createTime` 和 `updateTime` 字段填充值。
- **Movie.java 中的 `@TableField(exist = false)`**: 标记 `categories` 和 `crews` 为非数据库字段。
- **Review表无唯一约束**: 已移除 `uk_user_movie_review` 索引，支持同一用户对同一电影多次评论。

## 测试要点

1. 在 MySQL 中执行 `source database/init.sql`，应无错误输出
2. 检查 `movie_website` 库中存在9张表，数据量正确
3. 验证密码加密：`SELECT password FROM user WHERE username='admin'` 应看到BCrypt格式密文

## 注意事项

- 建库语句使用 `IF NOT EXISTS`，可重复执行不报错
- 种子数据中的密码使用 BCrypt 对"123456"加密生成
- `picsum.photos` 作为占位图源，开发阶段可用
- 播放记录表的 UNIQUE KEY 确保同一用户同一天只记一次播放
