-- KEYS[1] = 间隔Key (格式: email:interval:{email})
-- KEYS[2] = 小时计数Key (格式: email:hourly:{email})
-- KEYS[3] = 每日总量Key (格式: email:daily_total)
-- KEYS[4] = 黑名单Key (格式: email:blacklist:{email})

-- ARGV[1] = 每小时最大发送次数
-- ARGV[2] = 间隔秒数
-- ARGV[3] = 每日最大总量
-- ARGV[4] = 当前时间戳(秒)
-- ARGV[5] = 黑名单过期时间(秒)

-- 1. 频率限制检查
if redis.call('EXISTS', KEYS[1]) == 1 then
    return {-1, redis.call('TTL', KEYS[1])}
end

-- 2. 黑名单检查
if redis.call('EXISTS', KEYS[4]) == 1 then
    return {-2, redis.call('TTL', KEYS[4])}
end

-- 3. 小时计数检查
local hourCount = redis.call('INCR', KEYS[2])
if hourCount == 1 then
    redis.call('EXPIREAT', KEYS[2], tonumber(ARGV[4]) + 3600)
end

if hourCount > tonumber(ARGV[1]) then
    redis.call('SETEX', KEYS[4], tonumber(ARGV[5]), 1)
    return {-3, tonumber(ARGV[5])}
end

-- 4. 每日总量检查
local dailyTotal = redis.call('INCR', KEYS[3])
if dailyTotal == 1 then
    redis.call('EXPIREAT', KEYS[3], tonumber(ARGV[4]) + 86400)
end

if dailyTotal > tonumber(ARGV[3]) then
    return {-4, 0}
end

-- 5. 设置发送间隔
redis.call('SETEX', KEYS[1], tonumber(ARGV[2]), 1)

-- 6. 返回成功
return {1, hourCount, dailyTotal}