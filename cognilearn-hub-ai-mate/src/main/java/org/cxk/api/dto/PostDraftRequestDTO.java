package org.cxk.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 根据笔记生成发帖草稿 DTO
 */
@Data
public class PostDraftRequestDTO {

    @NotNull(message = "笔记ID不能为空")
    private Long noteId;

    @NotBlank(message = "笔记标题不能为空")
    @Size(max = 100, message = "笔记标题不能超过100个字符")
    private String title;

    @Size(max = 5000, message = "笔记内容不能超过5000个字符")
    private String content;
}
