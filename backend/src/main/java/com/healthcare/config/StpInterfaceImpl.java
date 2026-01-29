package com.healthcare.config;

import cn.dev33.satoken.stp.StpInterface;
import com.healthcare.entity.Permission;
import com.healthcare.entity.User;
import com.healthcare.mapper.PermissionMapper;
import com.healthcare.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sa-Token 权限接口实现，从数据库查询用户角色和权限
 */
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final UserMapper userMapper;
    private final PermissionMapper permissionMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        User user = userMapper.selectById(Long.parseLong(loginId.toString()));
        if (user == null) return Collections.emptyList();
        
        // 从数据库查询该角色对应的所有权限码
        List<Permission> permissions = permissionMapper.selectByRole(user.getRole().name());
        return permissions.stream()
                .map(Permission::getCode)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User user = userMapper.selectById(Long.parseLong(loginId.toString()));
        if (user == null) return Collections.emptyList();
        return List.of(user.getRole().name());
    }
}
