package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author KJH
 * @description 标签实体
 * @create 2025/10/26 09:17
 */
@Data
@TableName("tags")
public class Tag {
    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 标签名称 */
    private String name;

    /** 标签颜色 */
    private String color;

    /** 标签描述 */
    private String description;

    /** 逻辑删除 */
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