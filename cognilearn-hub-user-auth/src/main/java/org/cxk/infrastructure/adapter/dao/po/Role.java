package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * @author KJH
 * @description 角色实体
 * @create 2025/6/7 22:46
 */
@Data
@TableName("role")
public class Role implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    //todo 这里的crud都没做
    private Long roleId;
    private String code;

    private String name;

    private String description;

    @TableLogic  // 逻辑删除字段，配合 MyBatis-Plus 的自动注入
    private Boolean isDeleted;

    @TableField(exist = false)  // 该字段不在 role 表中，只用于查询后封装
    private Set<Permission> permissions;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}