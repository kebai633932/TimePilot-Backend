package org.cxk.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 向量搜索请求 DTO
 */
@Data
public class VectorSearchRequestDTO {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private Double threshold;

    @NotNull(message = "查询内容不能为空")
    @Size(max = 255, message = "查询内容不能超过255个字符")
    private String query;

//    // 可选：搜索范围，例如文件夹ID
//    private Long folderId;
}
