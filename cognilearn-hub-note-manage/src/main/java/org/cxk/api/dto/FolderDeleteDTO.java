package org.cxk.api.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;
/**
 * 删除文件夹 DTO
 */
@Data
public class FolderDeleteDTO {
    @NotNull(message = "文件夹ID不能为空")
    private Long folderId;
}
