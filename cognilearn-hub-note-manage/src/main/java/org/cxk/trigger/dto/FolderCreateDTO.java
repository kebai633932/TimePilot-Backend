package org.cxk.trigger.dto;

import lombok.Data;

import javax.validation.constraints.Size;
import javax.validation.constraints.NotNull;
/**
 * 新建文件夹 DTO
 */
@Data
public class FolderCreateDTO {

    @Size(max = 255, message = "文件夹名称不能超过255个字符")
    @NotNull(message = "文件夹名称不能为空")
    private String name;
    @NotNull(message = "父文件夹ID不能为空")
    private Long parentId; // 可为空，表示顶级文件夹
}