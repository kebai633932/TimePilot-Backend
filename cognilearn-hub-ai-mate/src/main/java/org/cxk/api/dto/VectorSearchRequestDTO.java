package org.cxk.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 向量搜索请求 DTO
 */
@Data
public class VectorSearchRequestDTO  implements Serializable {

    @NotNull(message = "用户ID不能为空")
    private Long userId;
    // 可选：相关系数
    private Float threshold;
    // 可选：限制返回条数
    private Integer topK;
    @NotNull(message = "查询内容不能为空")
    @Size(max = 255, message = "查询内容不能超过255个字符")
    private String query;

}
