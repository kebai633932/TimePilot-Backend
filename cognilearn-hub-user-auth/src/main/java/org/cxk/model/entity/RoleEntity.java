package org.cxk.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author KJH
 * @description 角色实体
 * @create 2025/6/7 22:46
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleEntity {
    private Long id;
    private Long roleId;
    private String code;
    private String name;
    private String description;
    private Boolean isDeleted;
    private Set<PermissionEntity> permissions;

    // 构造函数、getter/setter、equals/hashCode
}
