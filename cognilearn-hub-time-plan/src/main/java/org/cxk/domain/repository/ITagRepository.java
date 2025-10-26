package org.cxk.domain.repository;

import org.cxk.domain.model.entity.TagEntity;

import java.util.List;
import java.util.Optional;

/**
 * @author KJH
 * @description 标签仓储接口
 * @create 2025/10/26 09:34
 */
public interface ITagRepository {

    /**
     * 检查用户是否存在相同名称的标签
     */
    boolean existsByUserIdAndName(Long userId, String name);

    /**
     * 根据标签ID查询
     */
    Optional<TagEntity> findByTagId(Long tagId);

    /**
     * 保存标签
     */
    void save(TagEntity tagEntity);

    /**
     * 删除标签
     */
    void delete(TagEntity tagEntity);

    /**
     * 查询用户的所有标签
     */
    List<TagEntity> findByUserId(Long userId);

    /**
     * 根据标签ID和用户ID查询
     */
    Optional<TagEntity> findByTagIdAndUserId(Long tagId, Long userId);

    /**
     * 批量查询标签
     */
    List<TagEntity> findByIds(List<Long> tagIds);

    /**
     * 检查标签名称是否重复（排除指定标签）
     */
    boolean isTagNameDuplicate(Long userId, String name, Long excludeTagId);
}