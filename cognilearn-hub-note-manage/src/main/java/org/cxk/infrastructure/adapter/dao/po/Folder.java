package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author KJH
 * @description 文件夹实体
 * @create 2025/8/14 20:02
 */
@Data
@TableName("folder") // PostgreSQL 表名
public class Folder {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 文件夹ID */
    private Long folderId;
    /** 用户ID */
    private Long userId;

    /** 父文件夹ID */
    private Long parentId;
    /** 文件夹名称 */
    private String name;

    /** 逻辑删除（MyBatis-Plus 会用 is_deleted 字段处理） */
    @TableLogic
    private Boolean isDeleted;

    /** 删除时间 */
    private Date deleteTime;

    /** 创建时间，自动填充 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新时间，自动填充 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;
}