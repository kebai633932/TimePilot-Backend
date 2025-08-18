package org.cxk.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author KJH
 * @description
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
    //为0 为顶级
    private Long parentId; // 可为空，表示顶级文件夹
}
