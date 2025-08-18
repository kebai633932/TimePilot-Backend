package org.cxk.api;

import api.response.Response;
import org.cxk.api.dto.FolderCreateDTO;
import org.cxk.api.dto.FolderDeleteDTO;
import org.cxk.api.dto.FolderUpdateDTO;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @author KJH
 * @description
 * @create 2025/8/18 10:09
 */
public interface IFolderManageService {
    Response<Long> createFolder(@RequestBody FolderCreateDTO dto);
    Response<Boolean> updateFolder(@RequestBody FolderUpdateDTO dto);

    Response<Boolean> deleteFolder(@RequestBody FolderDeleteDTO dto);
}
