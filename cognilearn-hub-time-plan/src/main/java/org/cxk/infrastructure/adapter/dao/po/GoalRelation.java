package org.cxk.infrastructure.adapter.dao.po;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * @author KJH
 * @description 目标之间的关系实体（支持 DAG，有向无环图）
 *              可用于长期->中期->短期，或任意目标依赖关系
 * @create 2025/11/14
 */
@Data
@TableName("goal_relations")
public class GoalRelation {

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID（保证不同用户的图互相隔离） */
    private Long userId;

    /** 父目标ID（from） */
    private Long parentGoalId;

    /** 子目标ID（to） */
    private Long childGoalId;

    /**
     * 关系类型：
     * 1 - 拆解关系（decomposition）
     * 2 - 依赖关系（dependency）
     * 3 - 衍生关系（derived，可选）
     * 未来可继续扩展
     */
    private Integer relationType;

    /** 是否逻辑删除 */
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
}