package org.cxk.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author KJH
 * @description 笔记更新请求
 * @create 2025/8/14 13:30
 */
@Data
public class NoteUpdateDTO {
    @Size(max = 255, message = "笔记标题不能超过255个字符")
    @NotNull(message = "笔记标题不能为空")
    private String title;

    @NotNull(message = "笔记ID不能为空")
    private Long noteId;

    @NotNull(message = "父文件夹ID不能为空")
    // 为0 表示顶级
    private Long folderId;

    @NotNull(message = "笔记内容不能为空")
    private String contentMd; // Markdown 原文
}
