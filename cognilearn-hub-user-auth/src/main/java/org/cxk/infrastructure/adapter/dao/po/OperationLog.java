package org.cxk.infrastructure.adapter.dao.po;

import lombok.Data;

import java.util.Date;

/**
 * @author KJH
 * @description 操作日志
 * @create 2025/6/7 22:50
 */
@Data
public class OperationLog {
    private Long id;
    private Long userId;
    private String module;
    private String type;
    private String content;
    private Boolean isDeleted;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;
}
