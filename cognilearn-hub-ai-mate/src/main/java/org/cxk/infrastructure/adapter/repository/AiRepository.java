package org.cxk.infrastructure.adapter.repository;

import com.alibaba.fastjson2.JSON;
import com.pgvector.PGvector;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cxk.domain.model.entity.NoteVectorEntity;
import org.cxk.domain.repository.IAiRepository;
import org.cxk.infrastructure.adapter.dao.INoteVectorDao;
import org.cxk.infrastructure.adapter.dao.po.NoteVector;
import org.cxk.util.RedisKeyPrefix;
import org.redisson.api.RBatch;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import types.exception.BizException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author KJH
 * @description 文件夹
 * @create 2025/8/14 20:41
 */
@Slf4j
@Repository
@AllArgsConstructor
public class AiRepository implements IAiRepository {

    private final INoteVectorDao noteVectorDao;
    private final RedissonClient redissonClient;


    private final TransactionTemplate transactionTemplate;


    @Override
    public void saveNoteEmbedding(List<NoteVectorEntity> noteVectorEntityList) {
        List<NoteVector> noteVectorList=new ArrayList<>();
        if (noteVectorEntityList.isEmpty()) {
            log.error("待处理笔记向量列表为空");
            throw new BizException("待处理笔记向量列表为空");
        }
        if (noteVectorEntityList.get(0).getNoteId()==null) {
            log.error("待处理笔记id为空");
            throw new BizException("待处理笔记id为空");
        }
        if (noteVectorEntityList.get(0).getVersion()==null) {
            log.error("待处理笔记版本号为空");
            throw new BizException("待处理笔记版本号为空");
        }
        Long noteId=noteVectorEntityList.get(0).getNoteId();
        Long version=noteVectorEntityList.get(0).getVersion();
        for(NoteVectorEntity noteVectorEntity:noteVectorEntityList){
            NoteVector noteVector=new NoteVector();
            //1. 构建对象
            noteVector.setNoteId(noteVectorEntity.getNoteId());
            //  租户id 暂时用用户id代替
            noteVector.setTenantId(noteVectorEntity.getUserId());

            noteVector.setEmbedding(new PGvector(noteVectorEntity.getEmbedding()));
            noteVector.setIsDeleted(noteVectorEntity.getIsDeleted());
            noteVector.setDeleteTime(noteVectorEntity.getDeleteTime());

            noteVector.setContent(noteVectorEntity.getContentPlain());
            noteVector.setVersion(noteVectorEntity.getVersion());

            noteVector.setMetadata(noteVectorEntity.getMetadata());
            noteVectorList.add(noteVector);
        }

        transactionTemplate.execute(status -> {
            //如果以前有，则删除;没有则无影响
            noteVectorDao.deleteByNoteId(noteId);
            noteVectorDao.saveNoteEmbeddings(noteVectorList);

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                removeNoteSetCache(noteId);
                            } catch (Exception e) {
                                log.error("删除已经向量化并存储的 noteId 从 Redis Set 缓存异常", e);
                            }
                        }
                    }
            );
            return null;
        });
    }

    @Override
    public void deleteByNoteIds(List<Long> missingNoteIds) {
        noteVectorDao.deleteByNoteIds(missingNoteIds);

        removeNoteSetCache(missingNoteIds);
    }

    /**
     * 添加待向量化的 noteId 到 Redis Set
     */
    public void addNoteSetCache(Long noteId) {
        String cacheKey = RedisKeyPrefix.NOTE_VECTOR_TODO.name();
        redissonClient.getSet(cacheKey).add(noteId);
    }

    /**
     * 删除已经向量化并存储的 noteId 从 Redis Set
     */
    public void removeNoteSetCache(Long noteId) {
        String cacheKey = RedisKeyPrefix.NOTE_VECTOR_TODO.name();
        redissonClient.getSet(cacheKey).remove(noteId);
    }
    //Redis Pipeline 是 一次性批量发送多条命令，避免每条命令都要等待响应
    public void removeNoteSetCache(List<Long> missingNoteIds) {
        String cacheKey = RedisKeyPrefix.NOTE_VECTOR_TODO.name();
        RBatch batch = redissonClient.createBatch();
        RSet<Long> set = redissonClient.getSet(cacheKey);
        for (Long noteId : missingNoteIds) {
            batch.getSet(cacheKey).removeAsync(noteId);
        }
        batch.execute();
    }

    private List<Long> getPendingNoteIdsFromRedis() {
        String cacheKey = RedisKeyPrefix.NOTE_VECTOR_TODO.name();
        var set = redissonClient.<Long>getSet(cacheKey);
        return new ArrayList<>(set.readAll());
    }

    private void removeNoteIdsFromRedis(List<Long> noteIds) {
        String cacheKey = RedisKeyPrefix.NOTE_VECTOR_TODO.name();
        var set = redissonClient.<Long>getSet(cacheKey);
        set.removeAll(noteIds);
    }
}
