package org.cxk.api.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author KJH
 * @description
 * @create 2025/8/21 18:10
 */
@Data
public class FlashCardResponseDTO {
    private List<Long> noteIds;
    private Map<Long,FlashCard> flashCardDTOMap;
    @Data
    /*
      @author KJH
     * @description 复习卡片
     * @create 2025/8/21 18:10
     */
    public class FlashCard {

        private String question;  // 问题

        private String answer;    // 答案（可以用换行或分点分隔）
        private String title;    // 笔记标题
        private String contentMd;    // 笔记 markdown 原文
    }
}
