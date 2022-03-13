package com.max.gmall0822.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class RedisUtil {
    private JedisPool jedisPool;

    public void initJedisPool(String host, int port, int database) {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        // 总数 : 與併發有關
        jedisPoolConfig.setMaxTotal(200);

        // 如果到最大数，设置等待
        jedisPoolConfig.setBlockWhenExhausted(true);

        // 获取连接时等待的最大毫秒
        jedisPoolConfig.setMaxWaitMillis(1000);

        // 原本尖峰時請求200個,之後沒那麼多了,維護連接是消耗資源的,所以需進行釋放,
        // jedisPoolConfig.setMinIdle(10) : 但至少保留10個連接
        // jedisPoolConfig.setMaxIdle(10) : 最多保留10,200個連進來後,沒那麼多個了,可以趕緊釋放成10個
        // Min,Max設成一樣10,表示Redis會一直想辦法將連線數維持到10
        // 最少剩余数
        jedisPoolConfig.setMinIdle(10);

        // 最大剩余数
        jedisPoolConfig.setMaxIdle(10);

        // 在获取连接时，检查是否有效 : 因為有可能Tomcat那邊的連接對象已經斷掉
        jedisPoolConfig.setTestOnBorrow(true);

        // 创建连接池
        jedisPool = new JedisPool(jedisPoolConfig, host, port, 2 * 1000);

    }

    public Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }


}

