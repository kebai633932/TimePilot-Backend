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
    private Long id;
    private String code;
    private String name;
    private Integer type; // 1-菜单, 2-按钮, 3-API
    private String url;
    private Boolean isDeleted;
}
