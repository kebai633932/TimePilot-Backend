package org.cxk.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author KJH
 * @description 操作日志
 * @create 2025/6/7 22:50
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationLogEntity {
    private Long id;
    private Long userId;
    private String module;
    private String type;
    private String content;
    private Boolean isDeleted;
    private Date createTime;
}
