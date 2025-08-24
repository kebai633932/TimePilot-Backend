package org.cxk.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 生成复习卡片 DTO
 */
@Data
public class FlashCardRequestDTO  implements Serializable {

    /** 要生成卡片的笔记 ID 列表 */
    @NotNull(message = "笔记ID 列表 不能为空")
    private List<Long> noteIds;
}