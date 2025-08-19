package org.cxk.domain.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 笔记聚合根
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NoteEntity {
    /** 笔记ID */
    private Long noteId;
    /** 用户ID */
    private Long userId;
    /** 所属文件夹ID */
    private Long folderId;

    /** 笔记标题 */
    private String title;
    /** Markdown 内容 */
    private String contentMd;
    /** 纯文本内容（方便搜索/摘要） */
    private String contentPlain;

    /** 状态：0 草稿 / 1 发布 / 2 删除 */
    private Short status;
    /** 是否公开 */
    private Boolean isPublic;

    /** 是否删除（逻辑删除） */
    private Boolean isDeleted;

    /** 删除时间 */
    private Date deleteTime;
    /** 移动笔记到新文件夹 */
    public void moveToFolder(Long newFolderId) {
        if (newFolderId != null) this.folderId = newFolderId;
    }

    /** 逻辑删除笔记 */
    public void markDeleted() {
        this.isDeleted = true;
        this.deleteTime = new Date();
    }
}
