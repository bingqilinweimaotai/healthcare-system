# 项目不足分析与改进建议

> 生成时间：2026-01-29  
> 项目：AI 智能问诊平台

本文档详细分析了当前项目存在的不足，并提供了针对性的改进建议。

## 🚨 快速参考：关键问题摘要

### 必须立即处理的问题
1. **敏感信息泄露** - 数据库密码、API Key 等硬编码在配置文件中
2. **CSRF 保护缺失** - Spring Security 禁用了 CSRF 保护
3. **缺少单元测试** - 项目中没有测试代码，无法保证代码质量
4. **缺少 `.gitignore`** - 项目根目录缺少版本控制忽略文件

### 近期需要处理的问题
- 输入验证和 XSS 防护不足
- 数据库缺少索引和外键约束
- 日志记录不完善
- 缺少 API 文档（Swagger）
- 缺少监控和告警机制

### 长期优化方向
- 性能优化（缓存、数据库查询优化）
- 代码重构和规范
- Docker 化和 CI/CD 流程
- 文档完善

---

## 📋 目录

1. [安全问题](#安全问题)
2. [测试覆盖](#测试覆盖)
3. [代码质量](#代码质量)
4. [数据库设计](#数据库设计)
5. [配置管理](#配置管理)
6. [文档完善](#文档完善)
7. [性能优化](#性能优化)
8. [监控与日志](#监控与日志)
9. [前端改进](#前端改进)
10. [部署与运维](#部署与运维)

---

## 🔒 安全问题

### 1.1 敏感信息泄露 ⚠️ **严重**

**问题描述：**
- 数据库密码、Redis 密码、API Key 等敏感信息直接硬编码在 `application.yml` 中
- 腾讯云 COS 的 Secret ID 和 Secret Key 暴露在配置文件中
- OpenAI API Key 明文存储在配置文件中
- **项目根目录缺少 `.gitignore` 文件**，可能导致敏感文件被提交到版本控制

**风险：**
- 代码泄露会导致敏感信息暴露
- 可能被恶意利用造成数据泄露或经济损失
- 敏感文件可能被意外提交到 Git 仓库

**改进建议：**
```yaml
# 使用环境变量
spring:
  datasource:
    password: ${DB_PASSWORD:default_password}
  data:
    redis:
      password: ${REDIS_PASSWORD:}
langchain4j:
  open-ai:
    chat-model:
      api-key: ${OPENAI_API_KEY:}
tencent:
  cos:
    secret-id: ${COS_SECRET_ID:}
    secret-key: ${COS_SECRET_KEY:}
```

**行动项：**
- [ ] **创建项目根目录 `.gitignore` 文件**（见下方示例）
- [ ] 将所有敏感配置迁移到环境变量
- [ ] 创建 `.env.example` 文件作为配置模板
- [ ] 在 `.gitignore` 中添加 `.env` 文件
- [ ] 使用 Spring Cloud Config 或 Vault 管理配置（生产环境）
- [ ] 检查 Git 历史，移除已提交的敏感信息

**`.gitignore` 文件示例：**
```gitignore
# 环境变量文件
.env
.env.local
.env.*.local

# IDE
.idea/
*.iml
.vscode/
*.swp
*.swo

# 编译输出
backend/target/
frontend/dist/
frontend/node_modules/

# 日志文件
*.log
logs/

# 操作系统
.DS_Store
Thumbs.db

# 临时文件
*.tmp
*.bak
*.swp
```

### 1.2 CSRF 保护缺失 ⚠️ **严重**

**问题描述：**
- `SecurityConfig.java` 中禁用了 CSRF 保护：`http.csrf(csrf -> csrf.disable())`
- 所有接口都允许跨站请求，存在 CSRF 攻击风险

**改进建议：**
```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf
        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
        .ignoringRequestMatchers("/api/auth/**") // 仅登录注册接口忽略
    )
    .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
}
```

**行动项：**
- [ ] 启用 CSRF 保护
- [ ] 前端请求时携带 CSRF Token
- [ ] 或使用 SameSite Cookie 策略

### 1.3 输入验证不足 ⚠️ **中等**

**问题描述：**
- 虽然使用了 `@Valid` 注解，但缺少对 SQL 注入、XSS 攻击的防护
- 文件上传接口缺少文件类型和大小验证
- 缺少对恶意输入的过滤和转义

**改进建议：**
- 使用 MyBatis 参数化查询（已使用，需确保所有查询都使用）
- 添加 XSS 过滤器
- 文件上传添加白名单验证
- 使用 OWASP ESAPI 进行输入验证

**行动项：**
- [ ] 添加 XSS 防护过滤器
- [ ] 文件上传接口添加文件类型和大小限制
- [ ] 对所有用户输入进行转义处理
- [ ] 添加请求频率限制（Rate Limiting）

### 1.4 密码策略不明确 ⚠️ **中等**

**问题描述：**
- 注册时没有明确的密码复杂度要求
- 密码修改接口缺少旧密码验证逻辑检查
- 没有密码过期策略

**改进建议：**
```java
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$", 
         message = "密码必须包含大小写字母和数字，长度至少8位")
private String password;
```

**行动项：**
- [ ] 添加密码复杂度验证规则
- [ ] 实现密码历史记录（防止重复使用）
- [ ] 添加密码过期提醒功能

### 1.5 Token 安全 ⚠️ **中等**

**问题描述：**
- Token 过期时间设置为 30 天，时间过长
- 没有 Token 刷新机制
- Token 存储在 Redis，但缺少 Token 撤销机制

**改进建议：**
- 缩短 Token 有效期（建议 2-4 小时）
- 实现 Refresh Token 机制
- 添加 Token 黑名单功能

---

## 🧪 测试覆盖

### 2.1 缺少单元测试 ⚠️ **严重**

**问题描述：**
- 项目中没有发现任何单元测试文件
- 缺少对 Service 层和 Controller 层的测试
- 无法保证代码质量和功能正确性

**改进建议：**
```java
// 示例：AuthServiceTest.java
@SpringBootTest
@Transactional
class AuthServiceTest {
    @Autowired
    private AuthService authService;
    
    @Test
    void testLoginSuccess() {
        // 测试登录成功场景
    }
    
    @Test
    void testLoginWithWrongPassword() {
        // 测试密码错误场景
    }
}
```

**行动项：**
- [ ] 为所有 Service 类编写单元测试
- [ ] 使用 Mockito 模拟依赖
- [ ] 目标测试覆盖率：≥ 70%
- [ ] 集成到 CI/CD 流程中

### 2.2 缺少集成测试 ⚠️ **严重**

**问题描述：**
- 没有 API 接口的集成测试
- 无法验证端到端的功能流程

**改进建议：**
- 使用 `@SpringBootTest` 和 `MockMvc` 进行集成测试
- 使用 Testcontainers 进行数据库集成测试

**行动项：**
- [ ] 编写关键业务流程的集成测试
- [ ] 测试认证授权流程
- [ ] 测试 WebSocket 连接

### 2.3 缺少前端测试 ⚠️ **中等**

**问题描述：**
- 前端项目没有测试框架
- 没有组件测试和 E2E 测试

**改进建议：**
- 添加 Vitest 进行单元测试
- 添加 Vue Test Utils 进行组件测试
- 考虑使用 Playwright 进行 E2E 测试

---

## 💻 代码质量

### 3.1 异常处理不规范 ⚠️ **中等**

**问题描述：**
- 大量使用 `RuntimeException`，异常信息不够具体
- 缺少自定义业务异常类
- 异常处理逻辑分散

**改进建议：**
```java
// 创建自定义异常类
public class BusinessException extends RuntimeException {
    private final int code;
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}

// 使用
if (user == null) {
    throw new BusinessException(404, "用户不存在");
}
```

**行动项：**
- [ ] 创建业务异常类体系
- [ ] 统一异常处理逻辑
- [ ] 添加异常码枚举

### 3.2 日志记录不足 ⚠️ **中等**

**问题描述：**
- 关键业务操作缺少日志记录
- 没有操作审计日志
- 日志级别配置不合理

**改进建议：**
```java
@Slf4j
public class AuthService {
    public Map<String, Object> login(AuthDto dto) {
        log.info("用户登录尝试: username={}", dto.getUsername());
        // ... 业务逻辑
        log.info("用户登录成功: userId={}, role={}", user.getId(), user.getRole());
    }
}
```

**行动项：**
- [ ] 添加关键操作的日志记录
- [ ] 实现操作审计日志
- [ ] 配置日志轮转和归档策略

### 3.3 缺少 API 文档 ⚠️ **中等**

**问题描述：**
- 没有 Swagger/OpenAPI 文档
- API 接口缺少详细注释
- 前端开发者需要查看代码才能了解接口

**改进建议：**
- 集成 SpringDoc OpenAPI（Spring Boot 3 推荐）
- 为所有接口添加详细注释

**行动项：**
- [ ] 添加 SpringDoc OpenAPI 依赖
- [ ] 为所有 Controller 添加 API 文档注解
- [ ] 配置 Swagger UI 访问路径

### 3.4 代码重复 ⚠️ **轻微**

**问题描述：**
- 部分代码存在重复逻辑
- 缺少工具类和公共方法抽取

**改进建议：**
- 抽取公共方法到工具类
- 使用 AOP 处理横切关注点（如日志、权限）

---

## 🗄️ 数据库设计

### 4.1 缺少索引 ⚠️ **中等**

**问题描述：**
- 数据库表缺少必要的索引
- 查询性能可能受影响

**改进建议：**
```sql
-- 为常用查询字段添加索引
CREATE INDEX idx_user_username ON sys_user(username);
CREATE INDEX idx_consult_session_patient ON consult_session(patient_id);
CREATE INDEX idx_consult_session_doctor ON consult_session(doctor_id);
CREATE INDEX idx_consult_session_status ON consult_session(status);
CREATE INDEX idx_ai_session_user ON ai_session(user_id);
CREATE INDEX idx_prescription_patient ON prescription(patient_id);
CREATE INDEX idx_prescription_doctor ON prescription(doctor_id);
```

**行动项：**
- [ ] 分析查询模式，添加必要索引
- [ ] 定期检查慢查询日志
- [ ] 优化数据库查询性能

### 4.2 缺少外键约束 ⚠️ **中等**

**问题描述：**
- 虽然 `schema.sql` 中有外键定义，但部分表缺少外键约束
- 可能导致数据不一致

**改进建议：**
```sql
ALTER TABLE doctor_profile 
ADD CONSTRAINT fk_doctor_user FOREIGN KEY (user_id) REFERENCES sys_user(id);

ALTER TABLE prescription 
ADD CONSTRAINT fk_prescription_session FOREIGN KEY (session_id) REFERENCES consult_session(id);
```

**行动项：**
- [ ] 为所有关联表添加外键约束
- [ ] 设置合理的级联删除策略

### 4.3 缺少数据库迁移工具 ⚠️ **中等**

**问题描述：**
- 使用 `schema.sql` 直接建表，不适合生产环境
- 缺少版本管理和回滚机制

**改进建议：**
- 使用 Flyway 或 Liquibase 管理数据库版本
- 创建迁移脚本而非直接 SQL

**行动项：**
- [ ] 集成 Flyway 或 Liquibase
- [ ] 将现有表结构转换为迁移脚本
- [ ] 建立数据库版本管理流程

### 4.4 缺少数据备份策略 ⚠️ **中等**

**问题描述：**
- 没有明确的数据备份和恢复方案
- 医疗数据丢失风险高

**改进建议：**
- 制定定期备份策略（每日/每周）
- 实现数据恢复测试流程
- 考虑使用云数据库的自动备份功能

---

## ⚙️ 配置管理

### 4.5 缺少环境分离 ⚠️ **中等**

**问题描述：**
- 只有一个 `application.yml` 配置文件
- 开发、测试、生产环境配置混在一起

**改进建议：**
```
resources/
  ├── application.yml          # 公共配置
  ├── application-dev.yml     # 开发环境
  ├── application-test.yml    # 测试环境
  └── application-prod.yml    # 生产环境
```

**行动项：**
- [ ] 创建多环境配置文件
- [ ] 使用 Spring Profile 切换环境
- [ ] 生产环境使用环境变量覆盖敏感配置

---

## 📚 文档完善

### 5.1 API 文档缺失 ⚠️ **中等**

**问题描述：**
- README 中有 API 列表，但缺少详细的请求/响应示例
- 没有错误码说明文档

**改进建议：**
- 集成 Swagger/OpenAPI
- 创建 API 文档站点
- 添加错误码对照表

### 5.2 开发规范文档缺失 ⚠️ **轻微**

**问题描述：**
- 缺少代码规范文档
- 新成员上手困难

**改进建议：**
- 创建开发规范文档（编码规范、Git 提交规范等）
- 添加架构设计文档
- 创建常见问题 FAQ

### 5.3 部署文档不完整 ⚠️ **中等**

**问题描述：**
- README 中有部署说明，但缺少详细的运维文档
- 没有故障排查指南

**改进建议：**
- 完善部署文档（Docker、K8s 等）
- 添加故障排查指南
- 创建运维手册

---

## ⚡ 性能优化

### 6.1 缺少缓存策略 ⚠️ **中等**

**问题描述：**
- Redis 仅用于 Token 存储
- 没有对热点数据进行缓存（如药品列表、用户信息）

**改进建议：**
```java
@Cacheable(value = "drugs", key = "#keyword")
public Page<Drug> listDrugs(int page, int size, String keyword) {
    // 查询逻辑
}
```

**行动项：**
- [ ] 为热点数据添加缓存
- [ ] 实现缓存更新策略
- [ ] 添加缓存预热机制

### 6.2 数据库连接池配置 ⚠️ **轻微**

**问题描述：**
- HikariCP 连接池配置较简单
- 可能需要根据实际负载调整

**改进建议：**
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
```

### 6.3 分页查询优化 ⚠️ **轻微**

**问题描述：**
- 分页查询可能缺少优化
- 大数据量查询性能可能不佳

**改进建议：**
- 使用游标分页替代偏移量分页（大数据量场景）
- 添加查询结果缓存

---

## 📊 监控与日志

### 7.1 日志配置简单 ⚠️ **中等**

**问题描述：**
- 日志配置较简单
- 缺少日志聚合和分析

**改进建议：**
- 集成 Logback 或 Log4j2
- 配置日志文件轮转
- 集成 ELK 或 Loki 进行日志聚合

### 7.2 缺少监控和告警 ⚠️ **中等**

**问题描述：**
- 没有应用性能监控（APM）
- 缺少健康检查端点
- 没有告警机制

**改进建议：**
- 集成 Spring Boot Actuator
- 添加 Prometheus 指标暴露
- 集成 Grafana 进行可视化
- 配置告警规则（CPU、内存、错误率等）

**行动项：**
- [ ] 添加 Actuator 健康检查端点
- [ ] 集成 Prometheus + Grafana
- [ ] 配置关键指标告警

---

## 🎨 前端改进

### 8.1 缺少错误边界 ⚠️ **中等**

**问题描述：**
- 前端没有全局错误处理
- 组件错误可能导致整个应用崩溃

**改进建议：**
- 添加全局错误处理组件
- 使用 Vue 的错误边界机制
- 实现友好的错误提示

### 8.2 缺少加载状态 ⚠️ **轻微**

**问题描述：**
- 部分接口调用缺少加载状态提示
- 用户体验可能不佳

**改进建议：**
- 统一使用 Element Plus 的 Loading 组件
- 添加骨架屏（Skeleton）

### 8.3 缺少请求重试机制 ⚠️ **轻微**

**问题描述：**
- 网络错误时没有自动重试
- 可能影响用户体验

**改进建议：**
- 在 Axios 拦截器中添加重试逻辑
- 使用指数退避策略

### 8.4 WebSocket 重连机制 ⚠️ **中等**

**问题描述：**
- README 中提到需要处理 WebSocket 断开重连，但实现可能不完善

**改进建议：**
- 实现自动重连机制
- 添加连接状态提示
- 处理消息丢失和重复问题

---

## 🚀 部署与运维

### 9.1 缺少 Docker 化 ⚠️ **中等**

**问题描述：**
- 没有 Dockerfile
- 部署不够便捷

**改进建议：**
```dockerfile
# Dockerfile 示例
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/healthcare-backend-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**行动项：**
- [ ] 创建 Dockerfile
- [ ] 创建 docker-compose.yml
- [ ] 考虑使用 Kubernetes 部署

### 9.2 缺少 CI/CD ⚠️ **中等**

**问题描述：**
- 没有自动化构建和部署流程
- 代码质量检查依赖人工

**改进建议：**
- 集成 GitHub Actions 或 GitLab CI
- 自动化测试、构建、部署流程
- 添加代码质量检查（SonarQube）

### 9.3 缺少健康检查 ⚠️ **中等**

**问题描述：**
- 没有应用健康检查端点
- 无法监控应用状态

**改进建议：**
- 添加 Spring Boot Actuator
- 配置健康检查端点
- 集成到监控系统

### 9.4 缺少版本控制配置 ⚠️ **轻微**

**问题描述：**
- 项目根目录缺少 `.gitignore` 文件
- 可能导致不必要的文件被提交到版本控制

**改进建议：**
- 创建 `.gitignore` 文件（见 1.1 节示例）
- 配置 Git 提交规范（如 Conventional Commits）
- 添加 `.gitattributes` 文件统一换行符

---

## 📈 优先级建议

### 🔴 高优先级（立即处理）
1. **敏感信息泄露** - 安全风险极高
2. **CSRF 保护缺失** - 安全漏洞
3. **缺少单元测试** - 代码质量保障
4. **缺少 API 文档** - 开发效率

### 🟡 中优先级（近期处理）
1. 输入验证和 XSS 防护
2. 数据库索引优化
3. 日志记录完善
4. 缓存策略实现
5. 监控和告警配置

### 🟢 低优先级（长期优化）
1. 代码重构和优化
2. 性能调优
3. 文档完善
4. Docker 化和 CI/CD

---

## 📝 总结

当前项目在功能实现上较为完整，但在**安全性**、**测试覆盖**、**代码质量**、**运维监控**等方面存在明显不足。建议优先处理高优先级问题，特别是安全相关的问题，然后逐步完善其他方面。

**预计改进时间：**
- 高优先级问题：2-3 周
- 中优先级问题：1-2 个月
- 低优先级问题：持续优化

---

## 📞 联系方式

如有疑问或需要进一步讨论，请联系项目负责人。

---

*本文档将根据项目进展持续更新*
