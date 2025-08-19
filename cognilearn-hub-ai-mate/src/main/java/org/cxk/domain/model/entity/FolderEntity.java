package org.cxk.domain.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author KJH
 * @description
 * @create 2025/8/18 15:44
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FolderEntity {
    /** 文件夹ID */
    private Long folderId;
    /** 用户ID */
    private Long userId;

    /** 父文件夹ID */
    private Long parentId;
    /** 文件夹名称 */
    private String name;

    /** 删除时间 */
    private Date deleteTime;
}
