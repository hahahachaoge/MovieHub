# 第06步：电影展示API后端

## 目标

实现电影展示的所有后端接口，包括热播排行、按分类浏览、按地区浏览、组合筛选（分类+地区）、电影搜索、电影详情、分类列表和地区列表。使用 Redis 缓存热播数据和电影详情以提升性能。

## 核心产出

| 文件 | 作用 |
|------|------|
| `mapper/MovieMapper.java` | 电影数据访问（含自定义查询：分类ID、地区列表） |
| `mapper/CategoryMapper.java` | 分类数据访问 |
| `mapper/MovieCrewMapper.java` | 电影-主创关联访问 |
| `mapper/MovieTypeMapper.java` | 电影-分类关联访问 |
| `mapper/CrewMapper.java` | 主创人员访问 |
| `dto/MovieVO.java` | 电影返回VO（含分类名称、导演列表、演员列表） |
| `dto/CrewVO.java` | 主创返回VO（含角色名/职位） |
| `service/MovieService.java` | 电影服务接口 |
| `service/impl/MovieServiceImpl.java` | 电影服务实现（含Redis缓存 + 组合筛选逻辑） |
| `controller/MovieController.java` | 电影公开接口控制器（含7个公开API + 视频流式播放） |

## API接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/movie/public/hot?limit=10` | 热播电影列表 |
| GET | `/api/movie/public/category/{categoryId}?page=1&size=12` | 按分类分页查询 |
| GET | `/api/movie/public/region/{region}?page=1&size=12` | 按地区分页查询 |
| GET | `/api/movie/public/detail/{id}` | 电影详情（含分类、主创、相关推荐） |
| GET | `/api/movie/public/categories` | 所有分类列表 |
| GET | `/api/movie/public/regions` | 所有地区列表 |
| GET | `/api/movie/public/search?keyword=&page=1&size=12` | 按关键字搜索 |
| GET | `/api/movie/public/filter?categoryId=&region=&keyword=&page=1&size=12` | 组合筛选（分类/地区/搜索+分页） |
| GET | `/api/movie/public/latest?limit=6` | 最新上线电影 |
| GET | `/api/movie/public/related/{id}` | 相关推荐（同分类/同地区/热门兜底） |
| GET | `/api/movie/public/video/{id}` | 视频流式播放（支持Range分段请求） |

## 关键代码说明

- **MovieServiceImpl.java**: 使用 `convertToVO()` 方法将 Movie 实体转为 MovieVO，过程中关联查询分类名称和主创信息。热播数据和详情使用 Redis 缓存（热播1小时，详情30分钟）。
- **组合筛选 `getMoviesByCategoryAndRegion()`**: 当 `categoryId` 和 `region` 都为空时（选择"全部"+"全部地区"），返回全部电影列表；有筛选条件时取交集。
- **视频流播放**: 使用 `RandomAccessFile` 支持 HTTP Range 请求实现拖拽播放。

## 测试要点

1. `GET /api/movie/public/categories` → 返回6个分类
2. `GET /api/movie/public/filter?categoryId=&region=&page=1&size=5` → 返回全部274部电影的第1页
3. `GET /api/movie/public/filter?categoryId=1&region=美国&page=1` → 返回美剧动作片
4. `GET /api/movie/public/detail/1` → 返回电影详情（含分类和主创信息）
5. `GET /api/movie/public/regions` → 返回地区列表

## 注意事项

- 所有公开接口不需要 Token，拦截器已放行 `/api/movie/public/**`
- 分页使用 MyBatis Plus 的 `Page<T>`，参数 page 从1开始
- 组合筛选接口 `categoryId` 和 `region` 均为可选参数，不传时查全部
- Redis 缓存可能因序列化问题导致类型转换异常，确保 Entity 和 VO 支持 Jackson 序列化
