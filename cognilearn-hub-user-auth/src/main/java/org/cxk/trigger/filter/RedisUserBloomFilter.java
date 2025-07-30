package org.cxk.trigger.filter;

import org.cxk.service.repository.IUserRepository;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

/**
 * @author KJH
 * @description redis 分布式布隆过滤器
 * @create 2025/7/30 11:41
 */
@Component
public class RedisUserBloomFilter {

    private static final String BLOOM_FILTER_KEY = "bloom:user";

    private final RBloomFilter<String> bloomFilter;
    @Resource
    IUserRepository userRepository;
    public RedisUserBloomFilter(RedissonClient redissonClient) {
        this.bloomFilter = redissonClient.getBloomFilter(BLOOM_FILTER_KEY);
    }

    /**
     * 初始化布隆过滤器（在系统启动后加载用户）
     */
    @PostConstruct
    public void init() {
        // 如果未初始化，则初始化
        if (!bloomFilter.isExists()) {
            bloomFilter.tryInit(10000L, 0.01); // 预估最多 1 万用户，误判率 1%
        }

        // 加载已有用户（从数据库） 填充布隆过滤器
         List<String> usernames = userRepository.getAllUsernames();
         usernames.forEach(bloomFilter::add);
    }

    public void add(String username) {
        bloomFilter.add(username);
    }

    public boolean mightContain(String username) {
        return bloomFilter.contains(username);
    }
}