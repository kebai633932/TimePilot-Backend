package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author KJH
 * @description
 * @create 2025/7/31 18:40
 */
@Data
@TableName("user_role") // 注意反引号防止关键字冲突，如表名是其他名称请修改
public class UserRole {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private Long roleId;

    @TableLogic // 启用逻辑删除（MP 自动用 is_deleted 字段）
    private Boolean isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT) // 配合 MP 自动填充
    private Date createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}
