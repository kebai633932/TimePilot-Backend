package org.cxk.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import org.cxk.infrastructure.adapter.dao.IFolderDao;
import org.cxk.infrastructure.adapter.dao.po.Folder;
import org.cxk.service.repository.IFolderRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * @author KJH
 * @description 文件夹
 * @create 2025/8/14 20:41
 */
@Repository
@AllArgsConstructor
public class FolderRepository implements IFolderRepository {
    private final IFolderDao folderDao;

    @Override
    public boolean existsByUserIdAndParentIdAndName(Long userId, Long parentId, String name) {
        LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folder::getUserId, userId)
                .eq(Folder::getParentId, parentId)
                .eq(Folder::getName, name)
                .last("LIMIT 1"); // 提高查询效率
        return folderDao.selectCount(wrapper) > 0;
    }

    @Override
    public Optional<Folder> findByFolderId(Long folderId) {
        Folder folder = folderDao.findByFolderId(folderId);
        return Optional.ofNullable(folder);
    }

    @Override
    public void save(Folder folder) {
        if (folder.getFolderId() == null) {
            folderDao.insert(folder);
        } else {
            folderDao.updateById(folder);
        }
    }

    @Override
    public int countByParentId(Long folderId) {
        LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folder::getParentId, folderId);
        return folderDao.selectCount(wrapper).intValue();
    }

    @Override
    public void delete(Folder folder) {
        folderDao.deleteByFolderId(folder.getFolderId());
    }

    @Override
    public Optional<Folder> findByFolderIdAndUserId(Long folderId, Long userId) {
        LambdaQueryWrapper<Folder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Folder::getFolderId, folderId)
                .eq(Folder::getUserId, userId)
                .last("LIMIT 1");
        Folder folder = folderDao.selectOne(wrapper);
        return Optional.ofNullable(folder);
    }
}
