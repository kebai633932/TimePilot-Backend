package org.cxk.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.domain.model.entity.NoteEntity;
import org.cxk.domain.repository.INoteRepository;
import org.cxk.infrastructure.adapter.dao.IFolderDao;
import org.cxk.infrastructure.adapter.dao.INoteDao;
import org.cxk.infrastructure.adapter.dao.po.Note;
import org.cxk.util.RedisKeyPrefix;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import types.exception.BizException;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author KJH
 * @description todo 异常抛出的类型需要学习，修改
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
        note.setVersion(noteEntity.getVersion());
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
                        .version(n.getVersion())
                        .build()
                );
    }
    @Override
    public void move(NoteEntity noteEntity) {
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
                // 3. 移动笔记到数据库表
                noteDao.moveByNoteId(note);

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
            throw new BizException("移动笔记业务，系统异常，请稍后重试");
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
        note.setVersion(noteEntity.getVersion());

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

    @Override
    public List<NoteEntity> findByUserId(Long userId) {
        // 缓存 key
        String userNoteListKey = RedisKeyPrefix.USER_NOTE_LIST.format(userId);

        // 1. 先从缓存读用户的笔记 ID 列表
        RList<Long> rList = redissonClient.getList(userNoteListKey);
        List<Long> noteIds = rList.readAll();
        List<NoteEntity> noteEntityList = new ArrayList<>();
        List<Long> missedNoteIds = new ArrayList<>();

        if (noteIds != null && !noteIds.isEmpty()) {
            for (Long noteId : noteIds) {
                String noteInfoKey = RedisKeyPrefix.NOTE_INFO.format(noteId);
                RMap<String, Object> map = redissonClient.getMap(noteInfoKey);
                if (!map.isEmpty()) {
                    NoteEntity noteEntity = new NoteEntity();
                    noteEntity.setNoteId((Long) map.get("noteId"));
                    noteEntity.setFolderId((Long) map.get("folderId"));
                    noteEntity.setTitle((String) map.get("title"));
                    noteEntityList.add(noteEntity);
                } else {
                    missedNoteIds.add(noteId);
                }
            }

            // 缓存缺失，去数据库补充
            if (!missedNoteIds.isEmpty()) {
                LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(Note::getUserId, userId);
                wrapper.in(Note::getNoteId, missedNoteIds);
                List<Note> dbNotes = noteDao.selectList(wrapper);

                for (Note note : dbNotes) {
                    NoteEntity entity = toEntity(note);
                    //todo
                    noteEntityList.add(entity);

                    // 写单个 note 缓存
                    refreshNoteInfoCache(entity);
                }
            }
            return noteEntityList;
        }

        // 2. 缓存没数据 → 读数据库
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);
        List<Note> dbNotes = noteDao.selectList(wrapper);

        for (Note note : dbNotes) {
            noteEntityList.add(toEntity(note));
        }

        // 3. 更新缓存：用户笔记列表 & 单个笔记
        refreshUserNoteListCache(userId, noteEntityList.stream()
                .map(NoteEntity::getNoteId)
                .collect(Collectors.toList()));
        noteEntityList.forEach(this::refreshNoteInfoCache);

        return noteEntityList;
    }

    @Override
    public List<NoteEntity> findByNoteIds(List<Long> batchIds) {
        if (batchIds == null || batchIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 查询数据库
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Note::getNoteId, batchIds);
        List<Note> notes = noteDao.selectList(wrapper);

        // 2. 转换为领域实体
        return notes.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }

    private NoteEntity toEntity(Note note) {
        if (note == null) {
            return null;
        }
        return NoteEntity.builder()
                .noteId(note.getNoteId())
                .userId(note.getUserId())
                .folderId(note.getFolderId())
                .title(note.getTitle())
                .contentMd(note.getContentMd())
                .contentPlain(note.getContentPlain())
                .status(note.getStatus())
                .isPublic(note.getIsPublic())
                .isDeleted(note.getIsDeleted())
                .deleteTime(note.getDeleteTime())
                .version(note.getVersion())
                .build();
    }

    /** 创建/更新用户的笔记列表缓存 */
    private void refreshUserNoteListCache(Long userId, List<Long> noteIds) {
        String cacheKey = RedisKeyPrefix.USER_NOTE_LIST.format(userId);
        redissonClient.getKeys().delete(cacheKey);
        if (noteIds != null && !noteIds.isEmpty()) {
            redissonClient.getList(cacheKey).addAll(noteIds);
        }
    }

    /** 创建/更新单个笔记信息缓存 */
    private void refreshNoteInfoCache(NoteEntity noteEntity) {
        String cacheKey = RedisKeyPrefix.NOTE_INFO.format(noteEntity.getNoteId());
        redissonClient.getKeys().delete(cacheKey);
        Map<String, Object> info = new HashMap<>();
        info.put("noteId", noteEntity.getNoteId());
        info.put("folderId", noteEntity.getFolderId());
        info.put("title", noteEntity.getTitle());
        redissonClient.getMap(cacheKey).putAll(info);
    }
    /** 添加笔记到待向量化集合 */
    private void addNoteSetCache( Long noteId) {
        String cacheKey = String.valueOf(RedisKeyPrefix.NOTE_VECTOR_TODO);
        redissonClient.getSet(cacheKey).add(noteId);
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
