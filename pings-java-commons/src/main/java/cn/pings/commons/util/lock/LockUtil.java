package cn.pings.commons.util.lock;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;


/**
 *********************************************************
 ** @desc  ： 分布工锁工具类，需要在spring中注册并传入redisTemplate
 ** @author  Pings
 ** @date    2020/2/16
 ** @version v1.0
 * *******************************************************
 */
public final class LockUtil {

    //**redisTemplate
    private static RedisTemplate<String, Object> redisTemplate;
    private static final String VALUE = "true";

    public LockUtil(@NonNull RedisTemplate<String, Object> template){
        redisTemplate = template;
    }

    /**
     *********************************************************
     ** @desc ：释放分布式锁
     ** @author Pings
     ** @date   2020/2/16
     ** @param  key       键值
     ** @return 是否释放锁
     * *******************************************************
     */
    public static boolean tryUnLock(@NonNull String key){
        Boolean success = redisTemplate.delete(key);
        return success != null && success;
    }

    /**
     *********************************************************
     ** @desc ：获取分布式锁，默认120秒
     ** @author Pings
     ** @date   2020/2/16
     ** @param  key       键值
     ** @param  timeout   超时时间
     ** @param  unit      单位
     ** @return 是否获取到锁
     * *******************************************************
     */
    public static boolean tryGetLock(@NonNull String key, Integer timeout, TimeUnit unit){
        timeout = timeout == null ? 120 : timeout;
        unit = unit == null ? TimeUnit.SECONDS : unit;

        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, VALUE, timeout, unit);
        return success != null && success;
    }

    /**
     *********************************************************
     ** @desc ：获取分布式锁，默认120秒
     ** @author Pings
     ** @date   2020/2/16
     ** @param  key       键值
     ** @return 是否获取到锁
     * *******************************************************
     */
    public static boolean tryGetLock(@NonNull String key){
        return tryGetLock(key, null, null);
    }

    /**
     *********************************************************
     ** @desc ：获取分布式锁，
     *          如果获取成功则执行相关函数，并返回函数结果
     *          如果获取失败，则返回null
     *          执行完成后删除锁
     ** @author Pings
     ** @date   2020/2/16
     ** @param  key       键值
     ** @param  timeout   超时时间
     ** @param  unit      单位
     ** @param  supplier  要执行的函数
     ** @return 成功返回函数结果，失败返回null，
     *          注意：如果函数无返回值，则成功也会返回null
     * *******************************************************
     */
    public static <T> T tryExecuteInLock(@NonNull String key, Integer timeout, TimeUnit unit, Supplier<T> supplier){
        if(tryGetLock(key, timeout, unit)){
            try {
                return supplier.get();
            } finally {
                tryUnLock(key);
            }
        } else {
            return null;
        }
    }

    /**
     *********************************************************
     ** @desc ：获取分布式锁，
     *          如果获取成功则执行相关函数，并返回函数结果
     *          如果获取失败，则返回null
     *          执行完成后删除锁
     ** @author Pings
     ** @date   2020/2/16
     ** @param  key       键值
     ** @param  supplier  要执行的函数
     ** @return 成功返回函数结果，失败返回null，
     *          注意：如果函数无返回值，则成功也会返回null
     * *******************************************************
     */
    public static  <T> T  tryExecuteInLock(@NonNull String key, Supplier<T> supplier){
        return tryExecuteInLock(key, null, null , supplier);
    }
}
