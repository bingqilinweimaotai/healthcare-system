# Sa-Token 权限管理说明

## 1. 权限体系架构

本系统采用 **基于角色的权限控制（RBAC）**，权限通过数据库管理：

- **`sys_permission`**：权限表，存储所有权限码（如 `admin:user:list`）
- **`sys_role_permission`**：角色权限关联表，定义每个角色拥有哪些权限
- **`sys_user`**：用户表，每个用户有一个角色（`role` 字段：PATIENT/DOCTOR/ADMIN）

## 2. 新用户创建时的权限处理

### ✅ 自动关联（无需手动操作）

**创建新用户时，只需要设置用户的 `role` 字段，权限会自动关联！**

**原因：**
- 权限是基于**角色**的，不是基于**用户**的
- `sys_role_permission` 表已经预先配置好了每个角色对应的权限
- `StpInterfaceImpl.getPermissionList()` 会从数据库查询该角色对应的所有权限

### 📝 示例：用户注册流程

```java
// AuthService.register() 中
User user = new User();
user.setRole(User.Role.valueOf(dto.getRole().toUpperCase())); // 设置角色：PATIENT/DOCTOR/ADMIN
userMapper.insert(user);

// ✅ 完成！权限会自动关联，无需额外操作
```

### 🔍 权限查询流程

1. 用户登录后，Sa-Token 调用 `StpInterfaceImpl.getPermissionList(userId)`
2. `StpInterfaceImpl` 从数据库查询：
   ```sql
   SELECT p.code FROM sys_permission p
   INNER JOIN sys_role_permission rp ON p.id = rp.permission_id
   WHERE rp.role = 'PATIENT'  -- 根据用户角色查询
   ```
3. 返回权限码列表，Sa-Token 缓存到 Redis
4. 后续权限校验直接从 Redis 读取

## 3. Controller 权限注解配置

### 类级别注解（整个 Controller 需要特定角色）

```java
@SaCheckRole("ADMIN")  // 整个 Controller 只允许 ADMIN 角色访问
public class AdminController { ... }

@SaCheckRole("DOCTOR")  // 整个 Controller 只允许 DOCTOR 角色访问
public class DoctorController { ... }

@SaCheckRole("PATIENT")  // 整个 Controller 只允许 PATIENT 角色访问
public class PatientConsultController { ... }
```

### 方法级别注解（细化权限控制）

```java
@SaCheckPermission("admin:user:list")  // 需要 admin:user:list 权限
public Result listUsers() { ... }

@SaCheckPermission("doctor:prescription:create")  // 需要 doctor:prescription:create 权限
public Result createPrescription() { ... }
```

### 已配置的 Controller

- ✅ **AdminController**：类级别 `@SaCheckRole("ADMIN")` + 方法级别 `@SaCheckPermission`
- ✅ **DoctorController**：类级别 `@SaCheckRole("DOCTOR")` + 方法级别 `@SaCheckPermission`
- ✅ **PatientConsultController**：类级别 `@SaCheckRole("PATIENT")` + 方法级别 `@SaCheckPermission`
- ✅ **AiConsultController**：类级别 `@SaCheckRole("PATIENT")` + 方法级别 `@SaCheckPermission`
- ✅ **AuthController**：登录注册接口无需权限（已在 `SaTokenConfig` 中排除）
- ✅ **UserController**：仅需要登录（`StpUtil.checkLogin()`），所有角色都可访问

## 4. 如何添加新权限

### 步骤 1：在 `sys_permission` 表中插入新权限

```sql
INSERT INTO sys_permission (code, name, description) VALUES
('admin:new:feature', '新功能', '管理员新功能权限');
```

### 步骤 2：在 `sys_role_permission` 表中关联角色

```sql
-- 给 ADMIN 角色添加新权限
INSERT INTO sys_role_permission (role, permission_id)
SELECT 'ADMIN', id FROM sys_permission WHERE code = 'admin:new:feature';
```

### 步骤 3：在 Controller 中使用

```java
@SaCheckPermission("admin:new:feature")
public Result newFeature() { ... }
```

### 步骤 4：清除 Redis 缓存（可选）

权限变更后，Sa-Token 会在下次登录时重新加载权限。如需立即生效，可以：

- 让用户重新登录
- 或在代码中调用 `StpUtil.getPermissionList(userId)` 强制刷新缓存

## 5. 权限码命名规范

- **格式**：`角色:模块:操作`
- **示例**：
  - `admin:user:list` - 管理员查看用户列表
  - `admin:user:update` - 管理员修改用户
  - `doctor:prescription:create` - 医生开具处方
  - `patient:consult:create` - 患者发起咨询

## 6. Redis 缓存机制

- Sa-Token 会将权限列表缓存到 Redis，key 格式：`satoken:login:permission-list:{token}`
- 缓存有效期：与 token 有效期一致（默认 30 天）
- 权限变更后，用户重新登录时会自动刷新缓存

## 7. 常见问题

### Q: 新用户创建后为什么没有权限？

**A:** 检查以下几点：
1. 用户表的 `role` 字段是否正确设置（PATIENT/DOCTOR/ADMIN）
2. `sys_role_permission` 表中是否有该角色的权限关联
3. 用户是否已登录（权限在登录时加载）

### Q: 如何给某个用户单独添加权限？

**A:** 当前系统是**基于角色**的权限控制，不支持用户级权限。如需用户级权限，需要：
1. 创建 `sys_user_permission` 表
2. 修改 `StpInterfaceImpl.getPermissionList()` 合并查询用户权限和角色权限

### Q: 权限变更后如何立即生效？

**A:** 让用户重新登录，或调用 `StpUtil.logout(userId)` 强制下线后重新登录。
