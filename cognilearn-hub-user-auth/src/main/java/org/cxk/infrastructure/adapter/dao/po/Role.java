package org.cxk.infrastructure.adapter.dao.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Set;

/**
 * @author KJH
 * @description 角色实体
 * @create 2025/6/7 22:46
 */
@Data
public class Role {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Boolean isDeleted;
    private Set<Permission> permissions;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
    // 构造函数、getter/setter、equals/hashCode
}
