package org.cxk.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author KJH
 * @description
 * @create 2025/8/18 18:13
 */
@Data
public class NoteMoveDTO {

    @NotNull(message = "笔记ID不能为空")
    private Long noteId;

    @NotNull(message = "父文件夹ID不能为空")
    // 为0 表示顶级
    private Long folderId;
}
