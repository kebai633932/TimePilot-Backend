package org.cxk.domain;


import org.cxk.api.dto.FlashCardRequestDTO;
import org.cxk.api.dto.FlashCardResponseDTO;
import org.cxk.api.dto.VectorSearchRequestDTO;
import org.cxk.api.dto.VectorSearchResponseDTO;

import java.util.List;

/**
 * @author KJH
 * @description
 * @create 2025/8/14 11:04
 */
public interface IAiService {

    VectorSearchResponseDTO vectorSearch(VectorSearchRequestDTO dto);

    FlashCardResponseDTO generateFlashCards(FlashCardRequestDTO dto);

    FlashCardResponseDTO generateFlashCards(Long userId);

    void vectorizeNote(List<Long> noteIds);
}
