-- KEYS[1]：当天剩余预算的 Key
-- KEYS[2]：当天累计花费的 Key
-- ARGV[1]：本次扣费金额，单位为分
-- ARGV[2]：每日预算，单位为分
-- ARGV[3]：Key 的存活时间，单位为秒

local amount = tonumber(ARGV[1])
local balance = redis.call('GET', KEYS[1])

-- 第一次参与竞价时，自动初始化当天余额
if not balance then
    redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[3])
    balance = ARGV[2]
end

-- 余额不足，不扣费
if tonumber(balance) < amount then
    return 0
end

-- 扣减余额，同时累计花费
redis.call('DECRBY', KEYS[1], ARGV[1])
redis.call('INCRBY', KEYS[2], ARGV[1])

-- 给累计花费设置过期时间
redis.call('EXPIRE', KEYS[2], ARGV[3])

return 1