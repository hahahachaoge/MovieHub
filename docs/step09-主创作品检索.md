# 第09步：主创作品检索（后端+前端）

## 目标

实现按演员或导演姓名检索的功能，支持搜索主创人员、查看主创详情、浏览其参与的电影作品。

## 核心产出

| 文件 | 作用 |
|------|------|
| `service/CrewService.java` | 主创服务接口 |
| `service/impl/CrewServiceImpl.java` | 主创服务实现 |
| `controller/CrewController.java` | 主创API控制器 |
| `api/crew.ts` | 主创API调用封装 |
| `views/CrewSearch.vue` | 主创搜索页面（搜索框 + 结果列表 + 详情弹窗） |

## API接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/crew/search?keyword=&page=1&size=10` | 按姓名模糊搜索主创 |
| GET | `/api/crew/{id}/detail` | 主创详细信息 |
| GET | `/api/crew/{id}/movies?page=1&size=12` | 主创参与的电影列表 |

## 关键代码说明

- **CrewSearch.vue**: 顶部搜索框（带清除和搜索按钮），结果列表显示头像 + 姓名 + 角色标签（演员/导演），点击弹出详情Dialog展示个人简介和作品网格。
- **CrewServiceImpl.java**: `searchCrew` 使用 `like` 模糊匹配姓名，`getMoviesByCrewId` 通过 `movie_crew` 中间表查询电影ID再关联 `movie` 表。

## 注意事项

- 搜索关键词为空时阻止搜索
- 主创不存在返回404
- 搜索接口已在 `WebMvcConfig` 中放行（`/api/crew/**`）
