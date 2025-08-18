package org.cxk.api.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author KJH
 * @description
 * @create 2025/8/14 13:30
 */
@Data
public class NoteDeleteDTO {
    @NotNull(message = "笔记ID不能为空")
    private Long noteId;
}
