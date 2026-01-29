# AI 智能问诊平台

一个前后端分离的智能医疗问诊系统，支持 AI 智能问诊和人工医生咨询两种模式。患者可以通过 AI 快速获得初步诊断建议，也可以选择与专业医生进行实时沟通并获得处方。医生可以认领会话、与患者交流、开具电子处方。管理员可以管理用户、审核医生资质、管理药品库，并查看平台数据统计。

## ✨ 核心功能

### 👤 患者端
- **用户注册/登录**：支持患者注册和登录
- **AI 智能问诊**：
  - 创建问诊会话，与 AI 进行多轮对话
  - 支持流式响应（SSE），实时显示 AI 回复
  - 查看历史问诊记录和对话内容
- **人工咨询**：
  - 发起人工咨询会话
  - 通过 WebSocket 与医生实时聊天
  - 等待医生认领会话并开始咨询
  - 关闭会话
- **问诊记录**：
  - 查看所有问诊历史
  - 查看处方详情和药品信息
- **个人信息管理**：修改昵称、手机号、头像等

### 👨‍⚕️ 医生端
- **工作台**：查看待认领会话、我的会话等统计信息
- **会话管理**：
  - 查看待认领的咨询会话列表
  - 认领会话并开始咨询
  - 通过 WebSocket 与患者实时聊天
  - 完成会话
- **处方管理**：
  - 为患者开具电子处方
  - 选择药品、设置用法用量
  - 添加诊断信息
  - 查看历史处方记录
- **个人信息管理**：完善医生资料（姓名、医院、科室、职称等）

### 👨‍💼 管理员端
- **数据概览**：
  - 用户统计（总数、患者数、医生数）
  - 会话统计（总数、进行中、已完成）
  - 处方统计
  - ECharts 可视化图表（医生咨询量统计等）
- **用户管理**：
  - 查看用户列表（支持分页、搜索、状态筛选）
  - 启用/禁用用户账号
- **医生管理**：
  - 查看医生列表（支持分页、搜索、审核状态筛选）
  - 审核医生资质（通过/拒绝）
  - 启用/禁用医生账号
- **药品管理**：
  - 查看药品列表（支持分页、搜索）
  - 新增药品
  - 编辑药品信息（名称、规格、单位、用法说明）
  - 启用/停用药品

## 🛠️ 技术栈

### 前端
- **框架**：Vue 3.5.8 + TypeScript 5.6.2
- **构建工具**：Vite 5.4.8
- **状态管理**：Pinia 2.2.4
- **路由**：Vue Router 4.4.5
- **UI 组件库**：Element Plus 2.6.1
- **图表库**：ECharts 5.5.0
- **HTTP 客户端**：Axios 1.7.7
- **WebSocket**：SockJS 1.6.1 + @stomp/stompjs 7.0.0
- **图标**：@element-plus/icons-vue 2.3.1

### 后端
- **框架**：Spring Boot 3.2.5
- **Java 版本**：17
- **ORM**：MyBatis 3.0.3（非 Plus 版本）
- **数据库**：MySQL 8.0+
- **缓存**：Redis（用于 Sa-Token 持久化）
- **认证授权**：Sa-Token 1.37.0
  - 支持 JWT Token
  - 基于 Redis 的 Token 存储
  - 基于角色的权限控制（RBAC）
- **AI 集成**：LangChain4j 0.34.0
  - 支持 OpenAI API（GPT-4o-mini）
  - 支持流式响应（SSE）
- **实时通信**：WebSocket + STOMP
- **安全**：Spring Security
- **工具库**：Lombok

## 📋 环境要求

- **JDK**：17+
- **Node.js**：16+（推荐 18+）
- **Maven**：3.6+
- **MySQL**：8.0+
- **Redis**：5.0+（用于 Sa-Token）

## 🗄️ 数据库配置

数据库配置位于 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://118.25.16.103:3306/biyeshixi?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: root
    password: "202203401290"
  data:
    redis:
      host: 118.25.16.103
      port: 10086
      password: "202203401290"
```

### 数据库表结构

系统包含以下核心表：

- **sys_user**：用户表（患者、医生、管理员）
- **doctor_profile**：医生资料表（姓名、医院、科室、职称、审核状态）
- **ai_session**：AI 问诊会话表
- **ai_message**：AI 问诊消息表
- **consult_session**：人工咨询会话表（支持乐观锁）
- **consult_message**：人工咨询消息表
- **drug**：药品表
- **prescription**：处方表
- **prescription_item**：处方明细表
- **sys_permission**：权限表
- **sys_role_permission**：角色权限关联表

表结构由 `backend/src/main/resources/schema.sql` 在启动时自动初始化（`CREATE TABLE IF NOT EXISTS`）。

## 🚀 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd healthcare-system
```

### 2. 配置数据库和 Redis

修改 `backend/src/main/resources/application.yml` 中的数据库和 Redis 配置。

### 3. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动，接口前缀为 `/api`。

**默认管理员账号**：
- 用户名：`admin`
- 密码：`admin123`

**初始化数据**：
- 系统启动时会自动创建示例药品（阿莫西林、布洛芬等）
- 初始化权限数据和角色权限关联

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端开发服务器将在 `http://localhost:5173` 启动。

**代理配置**：
- `/api` → `http://localhost:8080/api`（后端 API）
- `/ws` → `ws://localhost:8080/ws`（WebSocket）

### 5. AI 问诊配置（可选）

系统支持两种模式：

1. **模拟模式**（默认）：
   - 未配置 `OPENAI_API_KEY` 时，AI 问诊返回模拟回复，仅用于演示

2. **真实 AI 模式**：
   - 设置环境变量 `OPENAI_API_KEY`
   - 或在 `application.yml` 中配置：
     ```yaml
     langchain4j:
       open-ai:
         chat-model:
           api-key: "your-api-key"
           model-name: gpt-4o-mini
           base-url: https://api.openai.com/v1
     ```

## 📡 API 接口说明

### 认证接口

- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册
- `POST /api/auth/logout` - 退出登录
- `GET /api/auth/info` - 获取当前用户信息

### 患者接口

- `POST /api/ai/consult` - AI 问诊（普通模式）
- `POST /api/ai/consult/stream` - AI 问诊（流式响应，SSE）
- `GET /api/ai/sessions` - 获取 AI 问诊会话列表
- `GET /api/ai/sessions/{sessionId}/messages` - 获取会话消息列表
- `POST /api/patient/consult/send` - 发送人工咨询消息
- `GET /api/patient/consult/sessions` - 获取咨询会话列表
- `GET /api/patient/consult/sessions/{sessionId}` - 获取会话详情
- `POST /api/patient/consult/sessions/{sessionId}/close` - 关闭会话
- `GET /api/patient/consult/sessions/{sessionId}/messages` - 获取会话消息
- `GET /api/patient/prescriptions` - 获取处方列表
- `GET /api/patient/prescriptions/{id}` - 获取处方详情

### 医生接口

- `GET /api/doctor/consult/waiting` - 获取待认领会话列表
- `POST /api/doctor/consult/claim/{sessionId}` - 认领会话
- `GET /api/doctor/consult/sessions` - 获取我的会话列表
- `POST /api/doctor/consult/sessions/{sessionId}/complete` - 完成会话
- `GET /api/doctor/consult/sessions/{sessionId}` - 获取会话详情
- `POST /api/doctor/consult/send` - 发送消息
- `GET /api/doctor/consult/sessions/{sessionId}/messages` - 获取会话消息
- `GET /api/doctor/drugs` - 获取药品列表
- `POST /api/doctor/prescriptions` - 创建处方
- `GET /api/doctor/prescriptions` - 获取处方列表
- `GET /api/doctor/prescriptions/{id}` - 获取处方详情

### 管理员接口

- `GET /api/admin/users` - 获取用户列表（分页、搜索、筛选）
- `GET /api/admin/users/{id}` - 获取用户详情
- `PUT /api/admin/users/{id}/status` - 更新用户状态
- `GET /api/admin/doctors` - 获取医生列表（分页、搜索、筛选）
- `GET /api/admin/doctors/{id}` - 获取医生详情
- `PUT /api/admin/doctors/{id}/audit` - 审核医生
- `PUT /api/admin/doctors/{id}/status` - 更新医生状态
- `GET /api/admin/drugs` - 获取药品列表（分页、搜索）
- `POST /api/admin/drugs` - 创建药品
- `PUT /api/admin/drugs/{id}` - 更新药品
- `GET /api/admin/stats/overview` - 获取数据概览
- `GET /api/admin/stats/doctor-consult` - 获取医生咨询统计

### 通用接口

- `GET /api/user/profile` - 获取个人信息
- `PUT /api/user/profile` - 更新个人信息
- `PUT /api/user/password` - 修改密码

### 认证方式

除登录和注册接口外，所有接口都需要在请求头中携带 Token：

```
Authorization: <token>
```

注意：Sa-Token 配置中 `token-prefix` 为空，所以不需要 `Bearer` 前缀。

## 🔐 权限系统

系统采用基于角色的权限控制（RBAC），包含以下角色：

- **PATIENT**：患者角色
- **DOCTOR**：医生角色
- **ADMIN**：管理员角色

### 权限码说明

权限通过权限码（Permission Code）进行控制，格式为：`角色:模块:操作`

**患者权限**：
- `patient:dashboard` - 患者首页
- `patient:consult:create` - 发起咨询
- `patient:consult:list` - 咨询记录
- `patient:history:view` - 问诊记录

**医生权限**：
- `doctor:dashboard` - 工作台
- `doctor:consult:list` - 会话列表
- `doctor:consult:handle` - 处理会话
- `doctor:prescription:create` - 开具处方
- `doctor:prescription:list` - 处方记录

**管理员权限**：
- `admin:dashboard` - 数据概览
- `admin:user:list` - 用户列表
- `admin:user:update` - 用户管理
- `admin:doctor:list` - 医生列表
- `admin:doctor:update` - 医生审核
- `admin:drug:list` - 药品列表
- `admin:drug:update` - 药品管理

权限数据在系统启动时自动初始化，详见 `schema.sql`。

## 💬 WebSocket 实时通信

系统使用 WebSocket + STOMP 协议实现实时消息推送，主要用于人工咨询功能。

### 连接地址

```
ws://localhost:8080/ws
```

### STOMP 端点

- **订阅路径**：`/user/{userId}/queue/messages` - 接收消息
- **发送路径**：`/app/chat` - 发送消息

### 消息格式

发送消息：
```json
{
  "sessionId": 1,
  "content": "患者消息内容"
}
```

接收消息：
```json
{
  "id": 1,
  "sessionId": 1,
  "senderType": "PATIENT",
  "senderId": 1,
  "content": "消息内容",
  "createdAt": "2026-01-29T10:00:00"
}
```

## 📁 项目结构

```
healthcare-system/
├── backend/                    # 后端项目
│   └── src/main/
│       ├── java/com/healthcare/
│       │   ├── config/         # 配置类
│       │   │   ├── SaTokenConfig.java          # Sa-Token 配置
│       │   │   ├── WebSocketConfig.java        # WebSocket 配置
│       │   │   ├── SecurityConfig.java         # Spring Security 配置
│       │   │   ├── GlobalExceptionHandler.java # 全局异常处理
│       │   │   ├── DataInit.java               # 数据初始化
│       │   │   └── StpInterfaceImpl.java       # Sa-Token 权限接口实现
│       │   ├── controller/     # 控制器层
│       │   │   ├── AuthController.java         # 认证控制器
│       │   │   ├── UserController.java         # 用户控制器
│       │   │   ├── AiConsultController.java    # AI 问诊控制器
│       │   │   ├── PatientConsultController.java # 患者咨询控制器
│       │   │   ├── DoctorController.java       # 医生控制器
│       │   │   └── AdminController.java        # 管理员控制器
│       │   ├── service/         # 服务层
│       │   ├── mapper/          # MyBatis Mapper 接口
│       │   ├── entity/          # 实体类（POJO）
│       │   └── dto/             # 数据传输对象
│       └── resources/
│           ├── mapper/          # MyBatis Mapper XML
│           ├── schema.sql       # 数据库表结构
│           └── application.yml # 应用配置
├── frontend/                   # 前端项目
│   └── src/
│       ├── api/                # API 封装
│       │   ├── request.ts      # Axios 请求封装
│       │   └── stomp.ts        # WebSocket 封装
│       ├── layouts/            # 布局组件
│       │   └── MainLayout.vue  # 主布局
│       ├── router/             # 路由配置
│       │   └── index.ts
│       ├── stores/             # Pinia 状态管理
│       │   └── auth.ts         # 认证状态
│       └── views/              # 页面组件
│           ├── Login.vue        # 登录页
│           ├── Register.vue     # 注册页
│           ├── patient/        # 患者页面
│           │   ├── Dashboard.vue
│           │   ├── AiConsult.vue
│           │   ├── ManualConsult.vue
│           │   └── History.vue
│           ├── doctor/         # 医生页面
│           │   ├── Dashboard.vue
│           │   ├── Sessions.vue
│           │   └── Prescriptions.vue
│           └── admin/          # 管理员页面
│               ├── Dashboard.vue
│               ├── Users.vue
│               ├── Doctors.vue
│               └── Drugs.vue
└── README.md
```

## 🔧 开发指南

### 后端开发

1. **添加新的实体类**：
   - 在 `entity` 包下创建实体类
   - 在 `schema.sql` 中添加对应的表结构
   - 创建对应的 Mapper 接口和 XML

2. **添加新的接口**：
   - 在 `controller` 包下创建控制器
   - 使用 `@SaCheckRole` 和 `@SaCheckPermission` 注解进行权限控制
   - 在 `service` 包下实现业务逻辑

3. **权限控制**：
   - 使用 `@SaCheckRole("ROLE")` 进行角色校验
   - 使用 `@SaCheckPermission("permission:code")` 进行权限校验
   - 在 `schema.sql` 中添加新的权限码和角色权限关联

### 前端开发

1. **添加新页面**：
   - 在 `views` 对应角色目录下创建 Vue 组件
   - 在 `router/index.ts` 中添加路由配置
   - 在 `MainLayout.vue` 中添加菜单项（如需要）

2. **API 调用**：
   - 使用 `@/api/request` 中的封装方法（`get`, `post`, `put`, `delete`）
   - 所有请求会自动携带 Token

3. **WebSocket 使用**：
   - 使用 `@/api/stomp` 中的封装方法
   - 连接后订阅消息，发送消息时调用对应方法

## 📦 生产部署

### 后端部署

1. **构建 JAR 包**：
   ```bash
   cd backend
   mvn clean package
   ```

2. **运行**：
   ```bash
   java -jar target/healthcare-backend-1.0.0.jar
   ```

3. **环境变量配置**：
   - 使用环境变量覆盖敏感配置（数据库密码、Redis 密码、API Key 等）
   - 或使用 `application-prod.yml` 配置文件

4. **数据库初始化**：
   - 生产环境建议关闭自动初始化：`spring.sql.init.mode: never`
   - 使用 Flyway 或 Liquibase 管理数据库版本

### 前端部署

1. **构建**：
   ```bash
   cd frontend
   npm run build
   ```

2. **Nginx 配置示例**：
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;
       
       # 静态资源
       location / {
           root /path/to/frontend/dist;
           try_files $uri $uri/ /index.html;
       }
       
       # API 代理
       location /api {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
       
       # WebSocket 代理
       location /ws {
           proxy_pass http://localhost:8080;
           proxy_http_version 1.1;
           proxy_set_header Upgrade $http_upgrade;
           proxy_set_header Connection "upgrade";
       }
   }
   ```

## ⚠️ 注意事项

1. **数据库密码**：生产环境务必修改默认数据库密码
2. **API Key**：不要将 OpenAI API Key 提交到代码仓库，使用环境变量
3. **Token 安全**：Sa-Token 配置了 30 天过期时间，可根据需要调整
4. **并发控制**：人工咨询会话使用了乐观锁（`version` 字段）防止并发冲突
5. **WebSocket 连接**：前端需要处理 WebSocket 断开重连逻辑

## 📝 许可证

本项目仅供学习和研究使用。

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！
