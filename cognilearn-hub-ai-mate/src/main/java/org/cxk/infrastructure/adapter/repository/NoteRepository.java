package org.cxk.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.domain.model.entity.FolderEntity;
import org.cxk.domain.model.entity.NoteEntity;
import org.cxk.infrastructure.adapter.dao.IFolderDao;
import org.cxk.domain.repository.INoteRepository;
import org.cxk.infrastructure.adapter.dao.INoteDao;
import org.cxk.infrastructure.adapter.dao.po.Folder;
import org.cxk.infrastructure.adapter.dao.po.Note;
import org.cxk.util.RedisKeyPrefix;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import types.exception.BizException;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author KJH
 * @description
 * @create 2025/8/16 19:30
 */
@Slf4j
@Repository
@AllArgsConstructor
public class NoteRepository implements INoteRepository {
    private final INoteDao noteDao;
    private final IFolderDao folderDao;
    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;
    @Override
    public int countByParentId(Long folderId) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getFolderId, folderId);
        return noteDao.selectCount(wrapper).intValue();
    }

    @Override
    public void save(NoteEntity noteEntity) {
        //1. 类型转换
        Note note=new Note();
        note.setNoteId(noteEntity.getNoteId());
        note.setUserId(noteEntity.getUserId());
        note.setFolderId(noteEntity.getFolderId());
        note.setTitle(noteEntity.getTitle());
        note.setContentMd(noteEntity.getContentMd());
        note.setContentPlain(noteEntity.getContentPlain());
        note.setStatus(noteEntity.getStatus());
        note.setIsPublic(noteEntity.getIsPublic());
        note.setIsDeleted(noteEntity.getIsDeleted());
        note.setDeleteTime(noteEntity.getDeleteTime());

        Long folderId=noteEntity.getFolderId();
        Long userId=noteEntity.getUserId();
        String folderLockKey = buildFolderLockKey(noteEntity.getFolderId());
        RLock folderLock = redissonClient.getLock(folderLockKey);

        try {
            if (!folderLock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new BizException("系统繁忙，请稍后再试");
            }

            transactionTemplate.execute(status -> {
                // 2. 检查是否有父文件夹
                if(null==folderDao.findByFolderIdAndUserId(folderId,userId)){
                    status.setRollbackOnly();
                    throw new BizException("目标文件夹不存在或无权限");
                }
                // 3. 插入笔记到数据库表
                noteDao.insert(note);

                // 4. 注册事务提交后的回调，清理缓存
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                try {
                                    clearUserNoteListCache(userId);
                                } catch (Exception e) {
                                    log.error("清理用户笔记缓存异常", e);
                                }
                            }
                        }
                );
                return null;
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("系统异常，请稍后重试");
        } finally {
            try {
                if (folderLock.isHeldByCurrentThread()) {
                    folderLock.unlock();
                }
            } catch (Exception e) {
                log.error("释放锁异常", e);
            }
        }
    }

    @Override
    public Optional<NoteEntity> findByNoteIdAndUserId(Long noteId, Long userId) {
        Note note = noteDao.findByNoteIdAndUserId(noteId, userId);

        return Optional.ofNullable(note)
                .map(n -> NoteEntity.builder()
                        .noteId(n.getNoteId())
                        .userId(n.getUserId())
                        .folderId(n.getFolderId())
                        .title(n.getTitle())
                        .contentMd(n.getContentMd())
                        .contentPlain(n.getContentPlain())
                        .status(n.getStatus())
                        .isPublic(n.getIsPublic())
                        .isDeleted(n.getIsDeleted())
                        .deleteTime(n.getDeleteTime())
                        .build()
                );
    }


    @Override
    public void update(NoteEntity noteEntity) {
        //1. 类型转换
        Note note=new Note();
        note.setNoteId(noteEntity.getNoteId());
        note.setUserId(noteEntity.getUserId());

        note.setFolderId(noteEntity.getFolderId());
        note.setTitle(noteEntity.getTitle());
        note.setContentMd(noteEntity.getContentMd());
        note.setContentPlain(noteEntity.getContentPlain());
        note.setStatus(noteEntity.getStatus());
        note.setIsPublic(noteEntity.getIsPublic());
        note.setIsDeleted(noteEntity.getIsDeleted());
        note.setDeleteTime(noteEntity.getDeleteTime());

        Long folderId=noteEntity.getFolderId();
        Long userId=noteEntity.getUserId();
        Long noteId=noteEntity.getNoteId();
        String folderLockKey = buildFolderLockKey(noteEntity.getFolderId());
        RLock folderLock = redissonClient.getLock(folderLockKey);

        try {
            if (!folderLock.tryLock(3, 10, TimeUnit.SECONDS)) {
                throw new BizException("系统繁忙，请稍后再试");
            }

            transactionTemplate.execute(status -> {
                // 2. 检查是否有父文件夹
                if(null==folderDao.findByFolderIdAndUserId(folderId,userId)){
                    status.setRollbackOnly();
                    throw new BizException("目标文件夹不存在或无权限");
                }
                // 3. 变更笔记到数据库表
                noteDao.updateByNoteId(note);

                // 4. 注册事务提交后的回调，清理缓存
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                try {
                                    clearUserNoteListCache(userId);
                                    clearNoteInfoCache(noteId);
                                } catch (Exception e) {
                                    log.error("清理用户笔记缓存异常", e);
                                }
                            }
                        }
                );
                return null;
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("系统异常，请稍后重试");
        } finally {
            try {
                if (folderLock.isHeldByCurrentThread()) {
                    folderLock.unlock();
                }
            } catch (Exception e) {
                log.error("释放锁异常", e);
            }
        }
    }


    /** 删除用户的笔记列表缓存（存在才删除） */
    private void clearUserNoteListCache(Long userId) {
        String cacheKey = RedisKeyPrefix.USER_NOTE_LIST.format(userId);
        redissonClient.getKeys().delete(cacheKey); // key 不存在也安全
    }

    /** 删除单个笔记信息缓存（存在才删除） */
    private void clearNoteInfoCache(Long noteId) {
        String cacheKey = RedisKeyPrefix.NOTE_INFO.format(noteId);
        redissonClient.getKeys().delete(cacheKey); // key 不存在也安全
    }

    /** 构造用户目录锁的 Key */
    private String buildUserFolderLockKey(Long userId) {
        return "lock:user:" + userId + ":folderTree";
    }

    private String buildFolderLockKey(Long folderId) {
        return "lock:user:" + folderId ;
    }
}
