package org.cxk.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * @author KJH
 * @description 标签领域服务接口
 * @create 2025/8/14 11:04
 */
public interface ITagService {
    

    /**
     * 删除标签（逻辑删除）
     */
    void deleteTag(Long userId, Long tagId);

    /**
     * 验证标签是否存在且属于用户
     */
    void validateTagOwnership(Long userId, Long tagId);

    /**
     * 检查标签名称是否重复
     */
    boolean isTagNameDuplicate(Long userId, String name, Long excludeTagId);

    /**
     * 获取用户标签统计
     */
    Map<String, Object> getUserTagStats(Long userId);

    Long createTag(Long userId, @NotBlank(message = "标签名称不能为空") @Size(max = 50, message = "标签名称长度不能超过50个字符") String name, @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式不正确，必须为#FFFFFF格式") String color, @Size(max = 200, message = "标签描述长度不能超过200个字符") String description);

}