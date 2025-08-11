package org.cxk.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author KJH
 * @description 权限实体
 * @create 2025/6/7 22:46
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PermissionEntity {
    //todo 怎么做权限表呢？？？menu 表示权限表
    // id 菜单名，路由地址，组件路径，菜单状态，菜单图标，权限表是，备注
    private Long id;
    private Long permissionId;
    private String code;
    private String name;
    private Integer type; // 1-菜单, 2-按钮, 3-API
    private String url;
    private Boolean isDeleted;
}
