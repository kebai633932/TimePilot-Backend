package org.cxk.trigger.job;

import com.xxl.job.core.handler.annotation.XxlJob;
import io.micrometer.core.annotation.Timed;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.cxk.domain.IAiService;
import org.cxk.util.RedisKeyPrefix;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author KJH
 * @description
 * @create 2025/8/22 10:18
 */
@Slf4j
@Component()
public class NoteVectorizationJob {

    @Resource
    private IAiService aiService;
//    @Resource
//    private ThreadPoolExecutor executor;
    @Resource
    private RedissonClient redissonClient;

    // ==========================================
    // 任务1：增量向量化（Redis）
    // 每60分钟执行一次
    // ==========================================
    @Timed(value = "NoteVectorizationRedisJob", description = "Redis增量笔记向量化任务")
    @XxlJob("NoteVectorizationRedisJob")
    public void execFromRedis() {
        RLock lock = redissonClient.getLock("job:note-vectorization-redis");
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(3, 0, TimeUnit.SECONDS);
            if (!isLocked) {
                log.info("跳过执行 Redis 增量任务：已有实例在运行");
                return;
            }

            List<Long> noteIds = getPendingNoteIdsFromRedis();
            if (noteIds.isEmpty()) {
                log.info("Redis中无待向量化笔记");
                return;
            }

            try {
                log.info("开始Redis向量化 noteIds={}", noteIds);
                aiService.vectorizeNote(noteIds);
                removeNoteIdsFromRedis(noteIds);
                log.info("完成Redis向量化 noteIds={}", noteIds);
            } catch (Exception e) {
                log.error("Redis向量化失败 noteIds={}", noteIds, e);
                // TODO: 可以放入失败重试队列
            }
        } catch (Exception e) {
            log.error("Redis增量任务执行异常", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ==========================================
    // 任务2：全量扫描数据库（可优化为对今天在线的用户做扫描）
    // 每天执行一次
    // ==========================================
    @Timed(value = "NoteVectorizationDbJob", description = "数据库全量笔记向量化任务")
    @XxlJob("NoteVectorizationDbJob")
    public void execFromDb() {
        RLock lock = redissonClient.getLock("job:note-vectorization-db");
        boolean isLocked = false;
        try {
            isLocked = lock.tryLock(5, 0, TimeUnit.SECONDS);
            if (!isLocked) {
                log.info("跳过执行 DB 全量任务：已有实例在运行");
                return;
            }

            // 从数据库查询需要向量化的笔记，例如按 version 或 update_time
            List<Long> noteIds = getPendingNoteIdsFromDb();
            if (noteIds.isEmpty()) {
                log.info("数据库中无待向量化笔记");
                return;
            }

            try {
                log.info("开始DB向量化 noteIds={}", noteIds);
//                aiService.vectorizeNote(noteIds);
                log.info("完成DB向量化 noteIds={}", noteIds);
            } catch (Exception e) {
                log.error("DB向量化失败 noteIds={}", noteIds, e);
            }

        } catch (Exception e) {
            log.error("DB全量任务执行异常", e);
        } finally {
            if (lock.isLocked() && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    // ==========================================
    // Redis操作方法
    // ==========================================

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

    // ==========================================
    // 数据库操作方法
    // ==========================================
    private List<Long> getPendingNoteIdsFromDb() {
        // TODO: 调用 NoteDao 或 Repository 查询未向量化或版本号不一致的笔记
        // 例如：SELECT note_id FROM note WHERE version > vector_version
        return new ArrayList<>();
    }
}
