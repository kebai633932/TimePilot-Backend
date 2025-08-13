package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 用户实体类
 * 对应数据库表：user（如果不是，请在@TableName中指定真实表名）
 */
@Data
@TableName("\"user\"") // 双引号包起来，PostgreSQL 允许
public class User {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String username;
    private String password;
    private String email;
    private String phone;

    @TableLogic // 启用逻辑删除（MP 自动用 is_deleted 字段）
    private Boolean isDeleted;

    private Date lastLoginTime;

    private Long delVersion;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT) // 配合 MP 自动填充
    private Date createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
