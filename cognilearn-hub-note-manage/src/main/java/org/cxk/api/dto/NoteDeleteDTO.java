package org.cxk.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * @author KJH
 * @description 笔记删除请求
 * @create 2025/8/14 13:30
 */
@Data
public class NoteDeleteDTO  implements Serializable {
    @NotNull(message = "笔记ID不能为空")
    private Long noteId;
}
