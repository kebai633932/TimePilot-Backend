package org.cxk.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新文件夹 DTO
 */
@Data
public class FolderUpdateDTO  implements Serializable {
    @NotNull(message = "文件夹ID不能为空")
    private Long folderId;
//    @NotNull(message = "用户ID不能为空")
//    private Long userId;

    @Size(max = 255, message = "文件夹名称不能超过255个字符")
    private String name; // 可修改名称
    @NotNull(message = "父文件夹ID不能为空")
    private Long newParentId; // 可修改上级文件夹
}
