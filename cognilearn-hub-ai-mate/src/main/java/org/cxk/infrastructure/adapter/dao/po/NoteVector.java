package org.cxk.infrastructure.adapter.dao.po;


import com.baomidou.mybatisplus.annotation.*;
import com.pgvector.PGvector;
import lombok.Data;

import java.util.Date;
import java.util.Map;

/**
 * @author KJH
 * @description 笔记向量实体
 * @create 2025/8/22
 */
@Data
@TableName("note_vector") // PostgreSQL 表名
public class NoteVector {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 笔记ID */
    private Long noteId;

    /** 租户ID（多租户场景） */
    private Long tenantId;

    /** 向量数据（pgvector 类型，建议自定义类型处理器） */
    private PGvector embedding;

    /** 逻辑删除 */
    @TableLogic
    private Boolean isDeleted;

    /** 删除时间 */
    private Date deleteTime;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /** 向量化的原数据 */
    private String content;

    /** 文档的额外属性
     * todo json2
     * */
    private Map<String,Object> metadata;

    /** 版本号 */
    private Long version;
}
