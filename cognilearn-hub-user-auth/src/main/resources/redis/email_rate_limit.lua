-- ====================================================
-- 邮箱频率间隔检查
-- ====================================================
if redis.call('EXISTS', KEYS[1]) == 1 then
    return {-1, redis.call('TTL', KEYS[1])}
end

-- ====================================================
-- 设备频率间隔检查
-- ====================================================
if redis.call('EXISTS', KEYS[4]) == 1 then
    return {-2, redis.call('TTL', KEYS[4])}
end

-- ====================================================
-- 计算时间参数（用于后续检查）
-- ====================================================
local now = tonumber(ARGV[6])
local midnight = now - (now % 86400) + 86400

-- ====================================================
-- 邮箱黑名单检查（检查黑名单是否有邮箱）
-- ====================================================
-- 获取邮箱地址
local email = ARGV[8]
if redis.call('SISMEMBER', KEYS[3], email) == 1 then
    return {-3, midnight - now}
end

-- ====================================================
-- 设备黑名单检查（检查黑名单是否有设备）
-- ====================================================
-- 获取设备ID
local deviceId = ARGV[9]
if redis.call('SISMEMBER', KEYS[6], deviceId) == 1 then
    return {-4, midnight - now}
end

-- ====================================================
-- 邮箱滑动窗口限流检查
-- ====================================================
-- 1. 移除1小时前的记录
redis.call('ZREMRANGEBYSCORE', KEYS[2], 0, now - 3600)

-- 2. 获取当前窗口内的请求数
local emailCount = redis.call('ZCARD', KEYS[2])

-- 3. 检查是否超过限制
if emailCount >= tonumber(ARGV[1]) then
    -- 触发黑名单
    redis.call('SADD', KEYS[3], email)
    redis.call('EXPIREAT', KEYS[3], midnight)
    return {-5, 0}
end

-- ====================================================
-- 设备滑动窗口限流检查
-- ====================================================
-- 1. 移除1小时前的记录
redis.call('ZREMRANGEBYSCORE', KEYS[5], 0, now - 3600)

-- 2. 获取当前窗口内的请求数
local deviceCount = redis.call('ZCARD', KEYS[5])

-- 3. 检查是否超过限制
if deviceCount >= tonumber(ARGV[3]) then
    -- 触发黑名单
    redis.call('SADD', KEYS[6], deviceId)
    redis.call('EXPIREAT', KEYS[6], midnight)
    return {-6, 0}
end

-- ====================================================
-- 系统总量限制检查
-- ====================================================
local dailyTotal = tonumber(redis.call('GET', KEYS[7]) or 0
if dailyTotal >= tonumber(ARGV[5]) then
    return {-7, 0}
end

-- ====================================================
-- 更新邮箱计数
-- ====================================================
redis.call('SETEX', KEYS[1], tonumber(ARGV[2]), 1)  -- 邮箱间隔锁

local newEmailCount = redis.call('ZADD', KEYS[2], now, ARGV[10])         -- 添加当前请求到滑动窗口
-- redis.call('EXPIREAT', KEYS[2], now + 4200)        -- 设置过期时间（1小时+缓冲）

-- ====================================================
-- 更新设备计数
-- ====================================================
redis.call('SETEX', KEYS[4], tonumber(ARGV[4]), 1)  -- 设备间隔锁

local newDeviceCount = redis.call('ZADD', KEYS[5], now, ARGV[10])         -- 添加当前请求到滑动窗口
-- redis.call('EXPIREAT', KEYS[5], now + 4200)        -- 设置过期时间（1小时+缓冲）

-- ====================================================
-- 更新系统总量
-- ====================================================
local dailyTotal = tonumber(redis.call('GET', KEYS[7]) or "0")
if newDailyTotal   == 1 then
    redis.call('EXPIREAT', KEYS[7], midnight)       -- 设置午夜过期
end

-- ====================================================
-- 返回成功结果
-- ====================================================
return {
    1,                  -- 状态码：允许发送
    newEmailCount ,        -- 当前邮箱计数
    newDeviceCount ,       -- 当前设备计数
    newDailyTotal              -- 当前系统总量
}