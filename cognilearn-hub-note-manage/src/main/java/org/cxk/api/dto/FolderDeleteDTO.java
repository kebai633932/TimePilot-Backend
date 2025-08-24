package org.cxk.api.dto;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 删除文件夹 DTO
 */
@Data
public class FolderDeleteDTO  implements Serializable {
    @NotNull(message = "文件夹ID不能为空")
    private Long folderId;
}
