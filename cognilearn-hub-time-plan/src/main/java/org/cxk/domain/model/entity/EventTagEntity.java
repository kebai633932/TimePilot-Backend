package org.cxk.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author KJH
 * @description 事件标签关联实体
 * @create 2025/10/26 09:17
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventTagEntity {
    /** 关联ID */
    private Long id;
    /** 事件类型：1-习惯性事件, 2-突发性事件 */
    private Integer eventType;
    /** 事件ID */
    private Long eventId;
    /** 标签ID */
    private Long tagId;
}