package org.cxk.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 新建文件夹 DTO
 */
@Data
public class FolderCreateDTO  implements Serializable {

    @Size(max = 255, message = "文件夹名称不能超过255个字符")
    @NotNull(message = "文件夹名称不能为空")
    private String name;
    @NotNull(message = "父文件夹ID不能为空")
    //为0 为顶级
    private Long parentId; // 可为空，表示顶级文件夹
}