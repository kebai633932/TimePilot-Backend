package org.cxk.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author KJH
 * @description 标签实体
 * @create 2025/10/26 09:17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagEntity {
    /** 标签ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 标签名称 */
    private String name;
    /** 标签颜色 */
    private String color;
    /** 标签描述 */
    private String description;
}