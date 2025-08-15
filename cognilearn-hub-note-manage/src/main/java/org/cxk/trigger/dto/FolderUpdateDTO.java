package org.cxk.trigger.dto;

import lombok.Data;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
/**
 * 更新文件夹 DTO
 */
@Data
public class FolderUpdateDTO {
    @NotNull(message = "文件夹ID不能为空")
    private Long folderId;
//    @NotNull(message = "用户ID不能为空")
//    private Long userId;

    @Size(max = 255, message = "文件夹名称不能超过255个字符")
    private String name; // 可修改名称
    @NotNull(message = "父文件夹ID不能为空")
    private Long newParentId; // 可修改上级文件夹
}
