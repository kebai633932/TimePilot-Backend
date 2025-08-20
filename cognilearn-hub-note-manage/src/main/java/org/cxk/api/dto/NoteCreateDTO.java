package org.cxk.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * @author KJH
 * @description 笔记创建请求
 * @create 2025/8/14 13:30
 */
@Data
public class NoteCreateDTO {
    @Size(max = 255, message = "笔记标题不能超过255个字符")
    @NotNull(message = "笔记标题不能为空")
    private String title;

    @NotNull(message = "父文件夹ID不能为空")
    // 为0 表示顶级
    private Long folderId;

    @NotNull(message = "笔记内容不能为空")
    private String contentMd; // Markdown 原文
}
