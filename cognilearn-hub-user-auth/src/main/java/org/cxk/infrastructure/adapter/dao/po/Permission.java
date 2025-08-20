package org.cxk.infrastructure.adapter.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @author KJH
 * @description 权限实体
 * @create 2025/6/7 22:46
 */
@Data
public class Permission {
    private Long id;
    private Long permissionId;
    private String code;
    private String name;
    private Integer type; // 1-菜单, 2-按钮, 3-API
    private String url;
    private Boolean isDeleted;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
}
