package org.cxk.trigger.http;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.api.dto.*;
import org.cxk.api.response.Response;
import org.cxk.api.response.TagVO;
import org.cxk.domain.ITagService;
import org.cxk.types.enums.ResponseCode;
import org.cxk.util.AuthenticationUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @description 标签管理
 */
@Slf4j
@RestController
@RequestMapping("/api/tag")
@AllArgsConstructor
public class TagController {

    private final ITagService tagService;

    /**
     * 创建标签
     */
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Long> createTag(@Valid @RequestBody TagCreateDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            Long tagId = tagService.createTag(userId, dto.getName(), dto.getColor(), dto.getDescription());
            return Response.success(tagId, "标签创建成功");
        } catch (Exception e) {
            log.error("创建标签失败，name={}", dto.getName(), e);
            return Response.error(ResponseCode.UN_ERROR, "创建标签失败");
        }
    }


    /**
     * 删除标签（逻辑删除）
     */
    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<Boolean> deleteTag(@Valid @RequestBody TagDeleteDTO dto) {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            tagService.deleteTag(userId, dto.getTagId());
            return Response.success(true, "标签删除成功");
        } catch (Exception e) {
            log.error("删除标签失败，id={}", dto.getTagId(), e);
            return Response.error(ResponseCode.UN_ERROR, "删除标签失败");
        }
    }

    /**
     * 查询用户所有标签
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Response<List<TagVO>> listTags() {
        try {
            Long userId = AuthenticationUtil.getCurrentUserId();
            List<TagVO> tags = tagService.listUserTags(userId);
            return Response.success(tags, "查询成功");
        } catch (Exception e) {
            log.error("查询标签列表失败", e);
            return Response.error(ResponseCode.UN_ERROR, "查询标签列表失败");
        }
    }
}