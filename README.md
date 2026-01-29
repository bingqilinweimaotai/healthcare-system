# AI 智能问诊平台

前后端分离的智能问诊系统：用户可进行 **AI 问诊** 或 **人工咨询**（与医生实时沟通、开处方）；医生端可认领会话、回复、开药；管理员管理用户、医生、药品及平台统计数据（ECharts 可视化）。

## 技术栈

- **前端**：Vue 3 + Vite + TypeScript + Pinia + Vue Router + Element Plus + ECharts + SockJS/STOMP
- **后端**：Spring Boot 3 + **MyBatis**（非 Plus）+ MySQL + Sa-Token + LangChain4j + WebSocket (STOMP)

## 数据库配置

已在 `backend/src/main/resources/application.yml` 中配置：

- 主机：`118.25.16.103`
- 端口：`3306`
- 用户名：`root`
- 密码：`202203401290`
- 数据库：`biyeshixi`

表结构由 `schema.sql`（`CREATE TABLE IF NOT EXISTS`）在启动时初始化；可根据需要自行维护 DDL。

## 启动说明

### 1. 后端

```bash
cd backend
mvn spring-boot:run
```

- 服务地址：`http://localhost:8080`
- 接口前缀：`/api`（如 `/api/auth/login`）
- 默认管理员：`admin` / `admin123`
- 初始会创建示例药品（阿莫西林、布洛芬等）

### 2. 前端

```bash
cd frontend
npm install
npm run dev
```

- 开发地址：`http://localhost:5173`
- 已配置代理：`/api` → 后端，`/ws` → WebSocket

### 3. AI 问诊（LangChain4j）

- 未配置 `OPENAI_API_KEY` 时，AI 问诊返回**模拟回复**，仅作演示。
- 使用真实模型：设置环境变量 `OPENAI_API_KEY`，或在 `application.yml` 中配置 `langchain4j.open-ai.chat-model.api-key`。

## 功能概览

| 角色 | 功能 |
|------|------|
| **患者** | 注册/登录、AI 问诊、人工咨询（发消息、等医生认领）、查看问诊记录与处方 |
| **医生** | 登录、查看待认领会话、认领、与患者聊天、开具处方、查看处方记录 |
| **管理员** | 用户管理（启用/禁用）、医生管理（审核、启用/禁用）、药品增删改、数据概览与 ECharts 统计 |

## 接口与权限

- 登录 / 注册：`POST /api/auth/login`、`POST /api/auth/register`（无需登录）
- 其余接口需在请求头携带：`Authorization: Bearer <token>`
- 管理员接口需 `ADMIN` 角色（如 `/api/admin/*`）

## 目录结构

```
healthcare system/
├── backend/          # Spring Boot + MyBatis
│   └── src/main/
│       ├── java/com/healthcare/
│       │   ├── config/   # Sa-Token、WebSocket、Security、全局异常、DataInit 等
│       │   ├── controller/
│       │   ├── dto/
│       │   ├── entity/   # 表对应 POJO
│       │   ├── mapper/   # MyBatis Mapper 接口（数据访问层，替代原 JPA Repository）
│       │   └── service/
│       └── resources/
│           ├── mapper/   # Mapper XML（SQL）
│           ├── schema.sql
│           └── application.yml
├── frontend/
│   └── src/
│       ├── api/      # request、stomp
│       ├── layouts/
│       ├── router/
│       ├── stores/
│       └── views/    # patient、doctor、admin 各角色页面
└── README.md
```

## 生产部署建议

1. 使用 `schema.sql` 或 Flyway/Liquibase 等管理表结构；可关闭 `spring.sql.init.mode` 避免重复执行。
2. 数据库账号、`OPENAI_API_KEY` 等敏感信息使用环境变量。
3. 前端构建后由 Nginx 托管静态资源，并反向代理 `/api`、`/ws` 到后端。
