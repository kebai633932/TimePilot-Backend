package org.cxk.application.impl;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.cxk.api.dto.FolderNoteDTO;
import org.cxk.api.dto.NoteInfoDTO;
import org.cxk.application.IFolderAppService;
import org.cxk.domain.IFolderService;
import org.cxk.types.exception.BizException;
import org.cxk.util.RedisKeyPrefix;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 文件夹编排实现类
 * 核心策略：
 *  - 对同一用户的目录结构操作，加“用户级分布式锁”保证互斥
 *  - 在锁的保护下执行数据库事务，保证数据一致性
 *  - 事务内完成数据库操作 + 缓存刷新（删除缓存）
 *  - 事务提交后统一释放锁
 */
@Slf4j
@Service
public class FolderAppServiceImpl implements IFolderAppService {
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private IFolderService folderDomainService;
    @Resource
    private TransactionTemplate transactionTemplate;

    /** 获取锁最大等待秒数（防止无限等待） */
    private static final int LOCK_WAIT = 3;
    /** 锁自动释放时间（秒），避免宕机导致死锁 */
    private static final int LOCK_LEASE = 10;

    @Override
    public Long createFolder(Long userId, String name, Long parentId) {
        String lockKey = buildUserFolderLockKey(userId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 获取分布式锁（用户级别），防止并发修改同一用户的文件夹树
            if (!lock.tryLock(LOCK_WAIT, LOCK_LEASE, TimeUnit.SECONDS)) {
                throw new BizException("系统繁忙，请稍后再试");
            }

            // 在锁内执行数据库事务
            return transactionTemplate.execute(status -> {
                // 1. 业务处理
                Long folderId=folderDomainService.createFolder(userId,  name,  parentId);

                // 2. 注册事务提交后的回调，清理缓存
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                try {
                                    clearUserFolderListCache(userId);
                                } catch (Exception e) {
                                    log.error("清理用户文件夹缓存异常", e);
                                }
                            }
                        }
                );
                return folderId;
            });

        } catch (InterruptedException e) {
            // 线程中断处理
            Thread.currentThread().interrupt();
            throw new BizException("系统异常，请稍后重试");
        } finally {
            // 保证最终释放锁（即使事务异常）
            try {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("释放锁异常", e);
            }
        }
    }

    @Override
    public void updateFolder(Long userId, String name, Long folderId, Long newParentId) {
        String lockKey = buildUserFolderLockKey(userId);
        RLock lock = redissonClient.getLock(lockKey);

        try {
            if (!lock.tryLock(LOCK_WAIT, LOCK_LEASE, TimeUnit.SECONDS)) {
                throw new BizException("系统繁忙，请稍后再试");
            }

            transactionTemplate.execute(status -> {
                //1. 业务处理
                folderDomainService.updateFolder( userId,  name,  folderId,  newParentId);

                // 2. 注册事务提交后的回调，清理缓存
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                try {
                                    clearFolderInfoCache(folderId);
                                    clearUserFolderListCache(userId);
                                } catch (Exception e) {
                                    log.error("清理用户文件夹缓存异常", e);
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
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("释放锁异常", e);
            }
        }
    }

    @Override
    public Map<Long, FolderNoteDTO> getFolderMap(Long userId) {
        return folderDomainService.getFolderMap(userId);
    }
    @Override
    public FolderNoteDTO buildFolderTree(Map<Long, FolderNoteDTO> folderNoteDTOMap,List<NoteInfoDTO> rootNotes){
        return folderDomainService.buildFolderTree(folderNoteDTOMap,rootNotes);
    }
    @Override
    public void deleteFolder(Long userId, Long folderId) {
        String userLockKey = buildUserFolderLockKey(userId);
        String folderLockKey = buildFolderLockKey(folderId);

        RLock userLock = redissonClient.getLock(userLockKey);
        RLock folderLock = redissonClient.getLock(folderLockKey);

        // 用 Redisson MultiLock 同时持有多把锁，避免死锁
        RLock multiLock = redissonClient.getMultiLock(userLock, folderLock);

        try {
            if (!multiLock.tryLock(LOCK_WAIT, LOCK_LEASE, TimeUnit.SECONDS)) {
                throw new BizException("系统繁忙，请稍后再试");
            }

            transactionTemplate.execute(status -> {
                // 1. 业务处理
                folderDomainService.deleteFolder( userId, folderId);

                // 4. 删除缓存
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronization() {
                            @Override
                            public void afterCommit() {
                                try {
                                    clearFolderInfoCache(folderId);
                                    clearUserFolderListCache(userId);
                                } catch (Exception e) {
                                    log.error("清理用户文件夹缓存异常", e);
                                }
                            }
                        }
                );
                return null;
            });
//            try {
//                clearFolderInfoCache(folderId);
//                clearUserFolderListCache(userId);
//            } catch (Exception e) {
//                log.error("清理用户文件夹缓存异常", e);
//            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("系统异常，请稍后重试");
        } finally {
            try {
                if (multiLock.isHeldByCurrentThread()) {
                    multiLock.unlock();
                }
            } catch (Exception e) {
                log.error("释放锁异常", e);
            }
        }
    }

    /** 删除用户的文件夹列表缓存（存在才删除） */
    private void clearUserFolderListCache(Long userId) {
        String cacheKey = RedisKeyPrefix.USER_FOLDER_LIST.format(userId);
        redissonClient.getKeys().delete(cacheKey); // key 不存在也安全
    }

    /** 删除单个文件夹信息缓存（存在才删除） */
    private void clearFolderInfoCache(Long folderId) {
        String cacheKey = RedisKeyPrefix.FOLDER_INFO.format(folderId);
        redissonClient.getKeys().delete(cacheKey); // key 不存在也安全
    }

    /** 构造用户目录锁的 Key */
    private String buildUserFolderLockKey(Long userId) {
        return "lock:user:" + userId + ":folderTree";
    }

    private String buildFolderLockKey(Long folderId) { return "lock:user:" + folderId ;
    }
}
