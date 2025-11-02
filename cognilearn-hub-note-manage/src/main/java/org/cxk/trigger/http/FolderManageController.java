package org.cxk.trigger.http;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.api.dto.FolderCreateDTO;
import org.cxk.api.dto.FolderDeleteDTO;
import org.cxk.api.dto.FolderUpdateDTO;
import org.cxk.api.response.Response;
import org.cxk.application.IFolderAppService;
import org.cxk.types.enums.ResponseCode;
import org.cxk.util.AuthenticationUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author KJH
 * @description 文件夹管理
 * @create 2025/8/11 14:48
 */
@Slf4j
@RestController
@RequestMapping("/api/folder")
@AllArgsConstructor

public class FolderManageController{

    private final IFolderAppService folderService;

    /**
     * 创建文件夹
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Long> createFolder(@Valid @RequestBody FolderCreateDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            Long folderId = folderService.createFolder(userId, dto.getName(), dto.getParentId());
            return Response.success(folderId, "文件夹创建成功");
        } catch (Exception e) {
            log.error("创建文件夹失败，name={}", dto.getName(), e);
            return Response.error(ResponseCode.UN_ERROR, "创建失败");
        }
    }

    /**
     * 修改文件夹信息
     */
    @PostMapping("/update")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Deprecated()
    public Response<Boolean> updateFolder(@Valid @RequestBody FolderUpdateDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            folderService.updateFolder(userId, dto.getName(), dto.getFolderId(), dto.getNewParentId());
            return Response.success(true, "修改文件夹成功");
        } catch (Exception e) {
            log.error("修改文件夹失败，id={}", dto.getFolderId(), e);
            return Response.error(ResponseCode.UN_ERROR, "修改文件夹失败");
        }
    }

    /**
     * 删除文件夹（逻辑删除）
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @Deprecated()
    public Response<Boolean> deleteFolder(@Valid @RequestBody FolderDeleteDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            folderService.deleteFolder(userId, dto.getFolderId());
            return Response.success(true, "删除成功");
        } catch (Exception e) {
            log.error("删除文件夹失败，id={}", dto.getFolderId(), e);
            return Response.error(ResponseCode.UN_ERROR, "删除失败");
        }
    }
}
