package org.cxk.api.response;

import lombok.Data;

/**
 * @author KJH
 * @description 标签视图对象
 * @create 2025/10/26 09:17
 */
@Data
public class TagVO {

    private Long tagId;

    private String name;

    private String color;

    private String description;
}