# 爪爪手记 (ClawMark)

> 情侣专属微信小程序 — 记录旅行足迹、共享相册、协同代办。

---

## 技术栈

| 层级 | 技术 | 目录 |
|------|------|------|
| 小程序前端 | 微信原生（JS / WXML / WXSS） | `APP/` |
| 后端 API | Spring Boot 2.7 + MyBatis-Plus + MySQL + JWT | `front/` |

---

## 功能

- **地图点亮** — 中国城市级地图，根据照片拍摄城市自动点亮
- **共享相册** — 按省份/城市/年份浏览，支持自定义相册、批量上传、评论
- **情侣代办** — 协同创建待办、标记完成、催办提醒，超时自动失效
- **情侣绑定** — 邀请码绑定，数据共享；支持解绑
- **用户系统** — 微信授权登录，JWT 鉴权，个人资料编辑

---

## 快速开始

### 环境要求

- JDK 1.8+ / Maven 3.6+ / MySQL 8.0+ / 微信开发者工具

### 后端

1. 创建数据库 `claw1`
2. 修改 `front/src/main/resources/application-dev.yml` 中的数据库连接和微信 AppID/Secret
3. 启动：
   ```bash
   cd front && mvn spring-boot:run
   ```
   默认运行在 `http://localhost:8080`，API 前缀 `/api/v1`。

### 小程序

1. 微信开发者工具导入 `APP/` 目录
2. 按需修改 `APP/miniprogram/config/env.js` 中的 API 地址
3. 编译预览

---

## API 概览

| 模块 | 路径 |
|------|------|
| 认证 / 用户 | `/api/v1/auth` `/api/v1/users` |
| 情侣 / 地图 | `/api/v1/couples` `.../map` |
| 相册 / 照片 / 评论 | `.../albums` `.../photos` `.../comments` |
| 待办 / 上传 / 通知 | `.../todos` `/api/v1/upload` `/api/v1/notifications` |

> 完整接口文档见 `小程序_API接口文档_v1.0.docx`

---

## 项目结构

```
ClawMark/
├── APP/miniprogram/     # 小程序（pages / services / utils / config）
├── front/src/main/      # Spring Boot（interfaces / application / infrastructure）
└── 小程序_API接口文档_v1.0.docx
```

---

## 架构

```
微信小程序 ── HTTP ──▶ JWT 拦截器 ──▶ REST Controller ──▶ AppService ──▶ MyBatis-Plus ──▶ MySQL
```

- 后端分层：`interfaces → application → infrastructure`
- 鉴权通过 `JwtAuthInterceptor`，白名单路径无需认证
- 用户身份通过 `UserContext` 线程上下文传递
- 文件本地存储，待办超时由定时任务自动标记
