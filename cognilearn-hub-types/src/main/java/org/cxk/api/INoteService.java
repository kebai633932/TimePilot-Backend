package org.cxk.api;

import org.apache.dubbo.config.annotation.DubboService;
import org.cxk.api.dto.NoteVectorDTO;

import java.util.List;

/**
 * @author KJH
 * @description dubbo 笔记模块对外接口
 * @create 2025/8/22 14:23
 */
public interface INoteService {

    List<NoteVectorDTO> findNotesByIds(List<Long> batchIds);
}
