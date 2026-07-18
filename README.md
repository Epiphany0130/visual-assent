# 科创空间云端素材库

覆盖上传入库、智能检索、AI 生图与多人实时协作的全链路云端图片资源管理平台。

## 技术栈

| 层级 | 技术选型 |
|------|---------|
| 基础框架 | Spring Boot 3.5、Java 21 |
| ORM | MyBatis-Plus 3.5.15 |
| 数据库 | MySQL |
| 缓存 | Redis + Caffeine（多级缓存） |
| 分布式锁 | Redisson 3.25 |
| 对象存储 | 腾讯云 COS |
| AI 能力 | Spring AI（MiMo 模型）+ 腾讯混元生图 |
| 权限控制 | Sa-Token（RBAC） |
| 实时协作 | WebSocket + LMAX Disruptor |
| API 文档 | Knife4j（Swagger） |
| 工具库 | Hutool、Lombok |

## 功能模块

| 模块 | 说明 |
|------|------|
| 用户模块 | 注册、登录、分布式 Session（Redis） |
| 图片模块 | 上传（文件/URL/批量）、搜索（文字/图片/色彩）、压缩、缩略图、OCR、AI 内容识别 |
| 空间模块 | 私有/团队空间、三级会员（免费/基础/专业）、容量配额管理 |
| 成员模块 | 团队空间成员管理、viewer/editor/admin 三级角色 |
| AI 生图模块 | Prompt 优化、异步生图、任务轮询 |
| 协作编辑模块 | WebSocket 实时多人编辑、编辑锁、Disruptor 高性能事件驱动 |
| 图片流转模块 | 跨空间图片迁移与审批 |
| 数据分析模块 | 空间用量、图片分类、标签排行、尺寸分布、上传行为分析 |
| 操作日志模块 | 全操作审计日志、多维度检索 |

## 项目结构

```
backend/
├── sql/                          # 数据库建表脚本（7 张表）
├── src/main/java/com/guyuqi/backend/
│   ├── controller/               # REST 控制器（10 个）
│   ├── service/                  # 业务逻辑层
│   ├── mapper/                   # 数据访问层
│   ├── model/
│   │   ├── dto/                  # 请求 DTO
│   │   ├── vo/                   # 响应 VO
│   │   ├── entity/               # 数据库实体
│   │   └── enums/                # 枚举常量
│   ├── manager/
│   │   ├── upload/               # 图片上传策略（模板模式）
│   │   ├── websocket/            # WebSocket + Disruptor 协作编辑
│   │   ├── auth/                 # Sa-Token 权限管理
│   │   └── ai/                   # AI 生图集成
│   ├── config/                   # Spring 配置（CORS、COS、MyBatis 等）
│   ├── annotation/               # 自定义注解
│   ├── aop/                      # 切面拦截器
│   ├── exception/                # 全局异常处理
│   └── utils/                    # 工具类
└── API.md                        # 完整 API 文档
```

## 快速启动

### 环境要求

- JDK 21+
- MySQL 8.0+
- Redis 6.0+
- Maven 3.8+

### 配置环境变量

```yaml
spring:
  datasource:
    url: your_database_url
    username: your_database_username
    password: your_database_password
  data:
    redis:
      host: your_redis_host
      password: your_redis_password
  ai:
    openai:
      api-key: your_api_key_for_openai
      base-url: your_base-url_for_openai
      chat:
        options:
          model: your_model

# 腾讯混元生图配置
tencent:
  image:
    api-key: your_tencent_xunhuan_api_key

# 对象存储配置（需要从腾讯云获取）
cos:
  client:
    host: your_cos_host
    secretId: your_cos_secretId
    secretKey: your_cos_seretKey
    region: your_cos_region
    bucket: your_cos_bucket
    datasetName: your_cos_datasetName

```

### 启动项目

```bash
cd backend
mvn spring-boot:run
```

启动后访问：
- API 文档：`http://localhost:8123/api/doc.html`
- 健康检查：`http://localhost:8123/api/health`

## 数据库

共 7 张表，建表脚本位于 `backend/sql/` 目录：

| 表名 | 说明 |
|------|------|
| `user` | 用户表 |
| `picture` | 图片表（含 OCR、色彩、AI 分析字段） |
| `space` | 空间表（免费/基础/专业三级） |
| `space_user` | 空间成员关联表 |
| `ai_generate_task` | AI 生图任务表 |
| `operation_log` | 操作日志表 |
| `picture_transfer` | 图片流转审批表 |

## 联系方式

- 个人网站：[richardgu.xyz](https://richardgu.xyz/)
- Issues：欢迎通过 [GitHub Issues](../../issues) 提交问题和建议

