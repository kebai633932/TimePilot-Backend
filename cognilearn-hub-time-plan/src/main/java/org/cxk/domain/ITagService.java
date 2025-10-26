package org.cxk.domain;

import org.cxk.domain.model.entity.TagEntity;

import java.util.List;
import java.util.Map;

/**
 * @author KJH
 * @description 标签领域服务接口
 * @create 2025/8/14 11:04
 */
public interface ITagService {

    /**
     * 创建标签
     */
    Tag createTag(Long userId, String name, String color, String description);

    /**
     * 更新标签
     */
    Tag updateTag(Long userId, Long tagId, String name, String color, String description);

    /**
     * 删除标签（逻辑删除）
     */
    void deleteTag(Long userId, Long tagId);

    /**
     * 根据ID查询标签
     */
    Tag getTagById(Long userId, Long tagId);

    /**
     * 查询用户所有标签
     */
    List<Tag> listUserTags(Long userId);

    /**
     * 批量查询标签
     */
    List<Tag> listTagsByIds(Long userId, List<Long> tagIds);

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
}