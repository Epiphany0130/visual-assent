# 视觉素材管理平台 - 后端接口文档

> 基础路径: `http://localhost:8123/api`
> 认证方式: Cookie-based Session（登录后自动携带 JSESSIONID）
> 前端可直接访问 `http://localhost:8123/doc.html` 使用 Knife4j 在线调试

---

## 通用说明

### 统一响应格式

```json
{
  "code": 0,
  "data": {},
  "message": ""
}
```

- `code = 0` 表示成功，其他为错误码
- 分页响应中 `data` 为：

```json
{
  "current": 1,
  "pageSize": 10,
  "total": 100,
  "records": []
}
```

### 权限说明

| 标记 | 含义 |
|------|------|
| 🔓 | 无需登录 |
| 🔑 | 需要登录 |
| 👑 | 仅管理员 |
| 🏠 | 需要空间权限 |

### 空间用户角色

| 角色 | 权限 |
|------|------|
| `viewer` | 查看图片 |
| `editor` | 查看、上传、编辑图片 |
| `admin` | 全部权限（含成员管理、流转） |

---

## 1. 健康检查

### `GET /health` 🔓

系统健康检查，用于监控和负载均衡探活。

**响应示例：**
```json
{ "code": 0, "data": "ok", "message": "" }
```

---

## 2. 用户模块

### `POST /user/register` 🔓

用户注册。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userAccount | String | ✅ | 账号 |
| userPassword | String | ✅ | 密码 |
| checkPassword | String | ✅ | 确认密码（需与密码一致） |

**响应：** `data` 为用户 `Long` 类型 id

---

### `POST /user/login` 🔓

用户登录，服务端创建 Session。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userAccount | String | ✅ | 账号 |
| userPassword | String | ✅ | 密码 |

**响应 `data`：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 用户 id |
| userAccount | String | 账号 |
| userName | String | 昵称 |
| userAvatar | String | 头像 URL |
| userProfile | String | 简介 |
| userRole | String | 角色（`user` / `admin`） |
| editTime | Date | 编辑时间 |
| createTime | Date | 创建时间 |
| updateTime | Date | 更新时间 |

---

### `GET /user/get/login` 🔑

获取当前登录用户信息（已脱敏）。

**响应 `data`：** 同登录接口返回的 `LoginUserVO`

---

### `POST /user/logout` 🔑

用户注销，销毁 Session。

**响应：** `data` 为 `Boolean`

---

### `POST /user/add` 👑

管理员创建用户，默认密码 `12345678`。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| userName | String | | 昵称 |
| userAccount | String | ✅ | 账号 |
| userAvatar | String | | 头像 URL |
| userProfile | String | | 简介 |
| userRole | String | | 角色：`user` / `admin` |

**响应：** `data` 为用户 `Long` 类型 id

---

### `GET /user/get` 👑

管理员根据 id 获取用户完整信息。

**Query 参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 用户 id |

**响应 `data`：** 完整 `User` 对象

---

### `GET /user/get/vo` 🔑

根据 id 获取用户信息（脱敏）。

**Query 参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 用户 id |

**响应 `data`：** `UserVO`（不含密码等敏感字段）

---

### `POST /user/delete` 👑

管理员删除用户。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 用户 id |

---

### `POST /user/update` 👑

管理员更新用户信息。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 用户 id |
| userName | String | | 昵称 |
| userAvatar | String | | 头像 URL |
| userProfile | String | | 简介 |
| userRole | String | | 角色：`user` / `admin` |

---

### `POST /user/list/page/vo` 👑

管理员分页查询用户列表。

**请求体（分页查询）：**
| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| current | int | 1 | 当前页号 |
| pageSize | int | 10 | 页面大小 |
| sortField | String | | 排序字段 |
| sortOrder | String | `descend` | 排序方式：`ascend` / `descend` |
| id | Long | | 用户 id |
| userName | String | | 昵称（模糊） |
| userAccount | String | | 账号（模糊） |
| userProfile | String | | 简介（模糊） |
| userRole | String | | 角色：`user` / `admin` / `ban` |

---

## 3. 图片模块

### `POST /picture/upload` 🏠

上传图片（支持重新上传覆盖已有图片）。需要空间 `PICTURE_UPLOAD` 权限。

**Content-Type:** `multipart/form-data`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | ✅* | 图片文件（与 fileUrl 二选一） |
| id | Long | | 图片 id（传则覆盖更新） |
| fileUrl | String | ✅* | 图片 URL（与 file 二选一） |
| picName | String | | 图片名称 |
| spaceId | Long | | 空间 id（不传则上传到公共图库） |

**响应 `data`：** `PictureVO`

---

### `POST /picture/upload/url` 🏠

通过 URL 上传图片。需要空间 `PICTURE_UPLOAD` 权限。

**请求体：** 同上传接口的 form 参数（以 JSON 形式传 `id`, `fileUrl`, `picName`, `spaceId`）

---

### `POST /picture/upload/batch` 👑

管理员批量抓取图片（从网络搜索下载）。

**请求体：**
| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| searchText | String | | 搜索词 |
| count | int | 10 | 抓取数量 |
| namePrefix | String | | 名称前缀 |
| spaceId | Long | | 空间 id |

**响应：** `data` 为上传成功的图片数量 `Integer`

---

### `POST /picture/upload/batch/files` 🏠

批量上传本地文件，自动按 `项目名称_上传人_日期_序号` 命名。需要空间 `PICTURE_UPLOAD` 权限。

**Content-Type:** `multipart/form-data`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| files | File[] | ✅ | 图片文件数组 |
| projectName | String | | 项目名称（命名前缀） |
| spaceId | Long | | 空间 id |

**响应 `data`：** `PictureVO[]`

---

### `POST /picture/delete` 🏠

删除图片。需要空间 `PICTURE_DELETE` 权限。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 图片 id |

---

### `POST /picture/update` 👑

管理员更新图片信息。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 图片 id |
| name | String | | 图片名称 |
| introduction | String | | 简介 |
| category | String | | 分类 |
| tags | String[] | | 标签数组 |

---

### `GET /picture/get` 👑

管理员根据 id 获取图片完整信息。

**Query 参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 图片 id |

---

### `GET /picture/get/vo` 🔑

根据 id 获取图片信息（脱敏），包含用户信息和权限列表。

**Query 参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 图片 id |

**响应 `data` (`PictureVO`)：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 图片 id |
| url | String | 图片 URL |
| name | String | 图片名称 |
| introduction | String | 简介 |
| tags | String[] | 标签列表 |
| category | String | 分类 |
| picSize | Long | 文件体积（字节） |
| picWidth | Integer | 宽度 |
| picHeight | Integer | 高度 |
| picScale | Double | 宽高比例 |
| picFormat | String | 格式（jpg/png/...） |
| userId | Long | 上传用户 id |
| thumbnailUrl | String | 缩略图 URL |
| ocrResult | String | OCR 识别结果（JSON） |
| dominantColor | String | 主色调名称 |
| dominantColorHex | String | 主色调 Hex |
| colorInfo | String | 颜色识别结果（JSON） |
| aiAnalysis | String | AI 分析结果（JSON） |
| reviewStatus | Integer | 审核状态：0-待审核 1-通过 2-拒绝 |
| reviewMessage | String | 审核信息 |
| spaceId | Long | 空间 id |
| createTime | Date | 创建时间 |
| editTime | Date | 编辑时间 |
| updateTime | Date | 更新时间 |
| user | UserVO | 上传用户信息 |
| permissionList | String[] | 当前用户权限列表 |

---

### `POST /picture/list/page` 👑

管理员分页查询图片列表。

**请求体：** 同分页查询格式 + 以下字段：

| 字段 | 类型 | 说明 |
|------|------|------|
| current | int | 当前页（默认 1） |
| pageSize | int | 页面大小（默认 10） |
| id | Long | 图片 id |
| name | String | 名称（模糊） |
| introduction | String | 简介（模糊） |
| category | String | 分类 |
| tags | String[] | 标签列表 |
| searchText | String | 搜索词（同时搜名称、简介） |
| userId | Long | 用户 id |
| dominantColor | String | 主色调 |
| reviewStatus | Integer | 审核状态 |
| spaceId | Long | 空间 id |

---

### `POST /picture/list/page/vo` 🔓

分页查询图片列表（脱敏）。公开图库无需登录，私有空间需要 `PICTURE_VIEW` 权限。

> `pageSize` 最大 20

**请求体：** 同管理员分页查询

---

### `POST /picture/edit` 🏠

编辑图片信息（本人或管理员）。需要空间 `PICTURE_EDIT` 权限。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 图片 id |
| name | String | | 图片名称 |
| introduction | String | | 简介 |
| category | String | | 分类 |
| tags | String[] | | 标签数组 |

---

### `GET /picture/tag_category` 🔓

获取预设的图片标签和分类列表。

**响应 `data`：**
```json
{
  "tagList": ["热门", "搞笑", "生活", "高清", "艺术", "校园", "背景", "简历", "创意"],
  "categoryList": ["模板", "电商", "表情包", "素材", "海报"]
}
```

---

### `POST /picture/review` 👑

管理员审核图片。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 图片 id |
| reviewStatus | Integer | ✅ | 1-通过 2-拒绝 |
| reviewMessage | String | | 审核意见 |

---

### `POST /picture/search/image` 🏠

以图搜图（上传文件），返回相似图片列表。需要空间 `PICTURE_VIEW` 权限。

**Content-Type:** `multipart/form-data`

| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| file | File | ✅ | 搜索用的图片 |
| limit | int | 10 | 返回数量上限 |
| threshold | int | 60 | 最低相似度分数 |

---

### `GET /picture/search/image` 🏠

以图搜图（通过 URL）。需要空间 `PICTURE_VIEW` 权限。

**Query 参数：**
| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| imageUrl | String | ✅ | | 图片 URL |
| limit | int | | 10 | 返回数量上限 |
| threshold | int | | 60 | 最低相似度分数 |

---

### `POST /picture/search/color` 🏠

按颜色搜索图片。需要空间 `PICTURE_VIEW` 权限。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| picColor | String | ✅ | 主色调名称 |
| spaceId | Long | | 空间 id |

---

## 4. 空间模块

### `POST /space/add` 🔑

创建空间。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| spaceName | String | ✅ | 空间名称 |
| spaceDesc | String | | 空间描述 |
| spaceLevel | Integer | | 空间级别：0-免费版 1-基础版 2-专业版 |
| spaceType | Integer | | 空间类型：0-私有 1-团队 |

**响应：** `data` 为空间 `Long` 类型 id

---

### `DELETE /space/delete` 🔑

删除空间（级联删除空间内所有图片）。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 空间 id |

---

### `POST /space/update` 👑

管理员更新空间信息。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 空间 id |
| spaceName | String | | 空间名称 |
| spaceDesc | String | | 空间描述 |
| spaceLevel | Integer | | 空间级别 |
| maxSize | Long | | 最大存储大小（字节） |
| maxCount | Long | | 最大图片数量 |

---

### `GET /space/get` 👑

管理员根据 id 获取空间完整信息。

---

### `GET /space/get/vo` 🔑

根据 id 获取空间信息（脱敏），包含用户信息和权限列表。

**响应 `data` (`SpaceVO`)：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 空间 id |
| spaceName | String | 空间名称 |
| spaceDesc | String | 空间描述 |
| spaceLevel | Integer | 空间级别 |
| maxSize | Long | 最大存储 |
| maxCount | Long | 最大数量 |
| totalSize | Long | 已用存储 |
| totalCount | Long | 图片数量 |
| userId | Long | 创建者 id |
| spaceType | Integer | 空间类型 |
| createTime | Date | 创建时间 |
| user | UserVO | 创建者信息 |
| permissionList | String[] | 当前用户权限列表 |

---

### `POST /space/list/page` 👑

管理员分页查询空间列表。

**请求体：**
| 字段 | 类型 | 说明 |
|------|------|------|
| current | int | 当前页（默认 1） |
| pageSize | int | 页面大小（默认 10） |
| id | Long | 空间 id |
| userId | Long | 创建者 id |
| spaceName | String | 名称（模糊） |
| spaceLevel | Integer | 空间级别 |
| spaceType | Integer | 空间类型 |

---

### `POST /space/list/page/vo` 🔓

分页查询空间列表（脱敏）。`pageSize` 最大 20。

---

### `POST /space/edit` 🔑

编辑空间信息（本人或管理员）。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 空间 id |
| spaceName | String | | 空间名称 |
| spaceDesc | String | | 空间描述 |

---

### `GET /space/list/level` 🔓

获取所有空间级别信息。

**响应 `data`：**
```json
[
  { "value": 0, "text": "免费版", "maxCount": 100, "maxSize": 104857600 },
  { "value": 1, "text": "基础版", "maxCount": 1000, "maxSize": 1073741824 },
  { "value": 2, "text": "专业版", "maxCount": 10000, "maxSize": 10737418240 }
]
```

---

## 5. 空间成员模块

### `POST /spaceUser/add` 🏠

添加成员到空间。需要 `SPACE_USER_MANAGE` 权限。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| spaceId | Long | ✅ | 空间 id |
| userId | Long | ✅ | 用户 id |
| spaceRole | String | ✅ | 角色：`viewer` / `editor` / `admin` |

---

### `POST /spaceUser/delete` 🏠

从空间移除成员。需要 `SPACE_USER_MANAGE` 权限。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 空间成员记录 id |

---

### `POST /spaceUser/get` 🏠

查询某个成员在某个空间的信息。需要 `SPACE_USER_MANAGE` 权限。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| spaceId | Long | ✅ | 空间 id |
| userId | Long | ✅ | 用户 id |

**响应 `data` (`SpaceUser`)：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 记录 id |
| spaceId | Long | 空间 id |
| userId | Long | 用户 id |
| spaceRole | String | 角色 |
| createTime | Date | 加入时间 |

---

### `POST /spaceUser/list` 🏠

查询空间成员列表。需要 `SPACE_USER_MANAGE` 权限。

**请求体：**
| 字段 | 类型 | 说明 |
|------|------|------|
| spaceId | Long | 空间 id |
| userId | Long | 用户 id |
| spaceRole | String | 角色 |

**响应 `data`：** `SpaceUserVO[]`（含 user 和 space 信息）

---

### `POST /spaceUser/edit` 🏠

编辑成员权限。需要 `SPACE_USER_MANAGE` 权限。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | ✅ | 空间成员记录 id |
| spaceRole | String | ✅ | 新角色：`viewer` / `editor` / `admin` |

---

### `POST /spaceUser/list/my` 🔑

查询我加入的团队空间列表。

**响应 `data`：** `SpaceUserVO[]`

---

## 6. 图片流转模块

### `POST /picture/transfer/add` 🏠

发起图片流转（将图片从一个空间移动到另一个空间）。需要 `PICTURE_TRANSFER` 权限。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| pictureId | Long | ✅ | 图片 id |
| targetSpaceId | Long | | 目标空间 id（null 表示公共图库） |
| reason | String | | 流转原因 |

**响应：** `data` 为流转记录 `Long` 类型 id

---

### `POST /picture/transfer/review` 🔑

审核图片流转（空间管理员操作）。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| transferId | Long | ✅ | 流转记录 id |
| reviewStatus | Integer | ✅ | 1-通过 2-拒绝 |
| reviewMessage | String | | 审核意见 |

---

### `GET /picture/transfer/get/vo` 🔑

根据 id 获取流转记录（脱敏）。

**响应 `data` (`PictureTransferVO`)：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 流转记录 id |
| pictureId | Long | 图片 id |
| picture | PictureVO | 图片信息 |
| sourceSpaceId | Long | 源空间 id |
| sourceSpace | SpaceVO | 源空间信息 |
| targetSpaceId | Long | 目标空间 id |
| targetSpace | SpaceVO | 目标空间信息 |
| userId | Long | 操作用户 id |
| user | UserVO | 操作用户信息 |
| transferType | String | 流转类型：`move` |
| reason | String | 流转原因 |
| reviewStatus | Integer | 0-待审核 1-通过 2-拒绝 |
| reviewMessage | String | 审核信息 |
| status | Integer | 0-失败 1-成功 |
| createTime | Date | 创建时间 |

---

### `POST /picture/transfer/list/page` 👑

管理员分页查询流转记录列表。

**请求体：**
| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| current | int | 1 | 当前页 |
| pageSize | int | 10 | 页面大小 |
| pictureId | Long | | 图片 id |
| sourceSpaceId | Long | | 源空间 id |
| targetSpaceId | Long | | 目标空间 id |
| userId | Long | | 操作用户 id |
| reviewStatus | Integer | | 审核状态 |

> `pageSize` 最大 20

---

### `POST /picture/transfer/list/page/vo` 👑

管理员分页查询流转记录列表（脱敏）。

---

### `POST /picture/transfer/review/list/page/vo` 👑

管理员分页查询**待审核**的流转记录列表（脱敏）。

---

## 7. AI 绘图模块

### `POST /ai/generate/prompt/optimize` 🔓

优化用户输入的 Prompt（调用 AI 生成更精确的描述）。

**请求体：**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| prompt | String | ✅ | 用户原始描述（最长 500 字） |

**响应 `data`：**
| 字段 | 类型 | 说明 |
|------|------|------|
| optimizedPrompt | String | 优化后的 prompt |

---

### `POST /ai/generate/image` 🔑

提交 AI 图片生成任务（异步）。

**请求体：**
| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| prompt | String | ✅ | | prompt 描述 |
| width | Integer | | 1024 | 图片宽度 |
| height | Integer | | 1024 | 图片高度 |

**响应：** `data` 为任务 `Long` 类型 id，需轮询查询状态

---

### `GET /ai/generate/task/{taskId}` 🔓

查询生图任务状态。

**路径参数：**
| 参数 | 类型 | 说明 |
|------|------|------|
| taskId | Long | 任务 id |

**响应 `data` (`TaskStatusResponse`)：**
| 字段 | 类型 | 说明 |
|------|------|------|
| taskId | Long | 任务 id |
| status | Integer | 0-待生成 1-生成中 2-成功 3-失败 |
| imageUrl | String | 生成的图片 URL（成功时） |
| pictureId | Long | 入库后的素材 id（成功且入库后） |
| errorMsg | String | 失败原因 |

---

## 8. 操作日志模块

### `GET /log/get` 👑

管理员根据 id 获取日志。

---

### `GET /log/get/vo` 👑

管理员根据 id 获取日志（脱敏）。

**响应 `data` (`LogVO`)：**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long | 日志 id |
| userId | Long | 操作用户 id |
| userName | String | 操作用户名称 |
| operationType | String | 操作类型 |
| targetType | String | 操作对象类型 |
| targetId | Long | 操作对象 id |
| targetName | String | 操作对象名称 |
| ipAddress | String | 操作 IP |
| operationDetail | String | 操作详情（JSON） |
| status | Integer | 0-失败 1-成功 2-待审批 |
| operationTime | Date | 操作时间 |

---

### `POST /log/list/page` 👑

管理员分页查询日志列表。

**请求体：**
| 字段 | 类型 | 说明 |
|------|------|------|
| current | int | 当前页 |
| pageSize | int | 页面大小 |
| userId | Long | 操作用户 id |
| userName | String | 操作用户名称 |
| operationType | String | 操作类型 |
| targetType | String | 操作对象类型 |
| targetName | String | 操作对象名称 |
| spaceId | Long | 关联空间 id |
| status | Integer | 操作状态 |
| searchText | String | 搜索词 |

> `pageSize` 最大 20

---

### `POST /log/list/page/vo` 👑

管理员分页查询日志列表（脱敏）。

---

### `POST /log/add/share` 🔑

记录分享日志（前端分享时调用）。

**请求体：**
| 字段 | 类型 | 说明 |
|------|------|------|
| targetType | String | 操作对象类型（如 `picture`） |
| targetId | Long | 操作对象 id |
| targetName | String | 操作对象名称 |
| spaceId | Long | 关联空间 id |

> `operationType` 会自动设为 `share`，`userId`/`userName` 自动从登录态获取

---

### `POST /log/add/download` 🔑

记录下载日志（前端下载时调用）。

**请求体：** 同分享日志，`operationType` 自动设为 `download`

---

## 9. 空间分析模块

### `POST /space/analyze/usage` 🔑

获取空间使用状态（存储大小、图片数量占比）。

**请求体：**
| 字段 | 类型 | 说明 |
|------|------|------|
| spaceId | Long | 空间 id |
| queryPublic | boolean | 是否查询公共图库 |
| queryAll | boolean | 全空间分析 |

**响应 `data`：**
| 字段 | 类型 | 说明 |
|------|------|------|
| usedSize | Long | 已使用大小（字节） |
| maxSize | Long | 总大小（字节） |
| sizeUsageRatio | Double | 存储使用比例 |
| usedCount | Long | 当前图片数量 |
| maxCount | Long | 最大图片数量 |
| countUsageRatio | Double | 数量占比 |

---

### `POST /space/analyze/category` 🔑

空间图片分类分析。

**请求体：** 同空间使用分析

**响应 `data`：** 数组
| 字段 | 类型 | 说明 |
|------|------|------|
| category | String | 图片分类 |
| count | Long | 图片数量 |
| totalSize | Long | 分类图片总大小 |

---

### `POST /space/analyze/tag` 🔑

空间图片标签分析。

**响应 `data`：** 数组
| 字段 | 类型 | 说明 |
|------|------|------|
| tag | String | 标签名称 |
| count | Long | 使用次数 |

---

### `POST /space/analyze/size` 🔑

空间图片大小分布分析。

**响应 `data`：** 数组
| 字段 | 类型 | 说明 |
|------|------|------|
| sizeRange | String | 大小范围（如 `0-1MB`） |
| count | Long | 图片数量 |

---

### `POST /space/analyze/user` 🔑

用户上传行为分析。

**请求体：**
| 字段 | 类型 | 说明 |
|------|------|------|
| spaceId | Long | 空间 id |
| queryPublic | boolean | 是否查询公共图库 |
| queryAll | boolean | 全空间分析 |
| userId | Long | 用户 id（可选） |
| timeDimension | String | 时间维度：`day` / `week` / `month` |

**响应 `data`：** 数组
| 字段 | 类型 | 说明 |
|------|------|------|
| period | String | 时间区间 |
| count | Long | 上传数量 |

---

### `POST /space/analyze/rank` 🔑

空间使用排行分析。

**请求体：**
| 字段 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| topN | int | 10 | 排名前 N 的空间 |

**响应 `data`：** `Space[]` 数组（按使用量降序）

---

## 10. 文件模块

### `POST /file/test/upload` 👑

测试文件上传（管理员）。

**Content-Type:** `multipart/form-data`

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | ✅ | 文件 |

**响应：** `data` 为文件存储路径 `String`

---

### `GET /file/test/download/` 👑

测试文件下载（管理员）。

**Query 参数：**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| filepath | String | ✅ | 文件路径 |

**响应：** 文件流（`application/octet-stream`）

---

## 附录：操作类型枚举

| operationType | 含义 |
|---------------|------|
| `upload` | 上传 |
| `download` | 下载 |
| `delete` | 删除 |
| `edit` | 编辑 |
| `share` | 分享 |
| `permission_change` | 权限变更 |
| `approve` | 审批 |
| `login` | 登录 |
| `logout` | 登出 |

## 附录：操作对象类型枚举

| targetType | 含义 |
|------------|------|
| `picture` | 图片 |
| `space` | 空间 |
| `space_user` | 空间成员 |
| `user` | 用户 |
