package com.tjslzhkj.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.tjslzhkj.coupon.constant.Constant;
import com.tjslzhkj.coupon.constant.CouponStatus;
import com.tjslzhkj.coupon.entity.Coupon;
import com.tjslzhkj.coupon.exception.CouponException;
import com.tjslzhkj.coupon.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Redis 相关的操作服务接口实现 （每一种状态优惠券的使用，都会影响到他自己，也又可能会影响到别的状态）
 * **
 * Yuezejian  Created in 2020/11/28 下午10:01
 */
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService {

    /** redis 客户端，redis 的 key 肯定是 String 类型，而 StringRedisTemplate 是 value 也都是 String 的一个简化 */
    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * According to userId and coupon status find List<Coupon> from Cache
     *
     * @param userId user id
     * @param status coupon status {@link CouponStatus}
     * @return
     */
    @Override
    public List<Coupon> getCacheCoupons(Long userId, Integer status) {
        log.info("Get Coupons From Cache: {}, {}", userId, status);
        String redisKey = status2RedisKey(status,userId);
        List<String> couponStrs = redisTemplate.opsForHash().values(redisKey)
                .stream().map( o -> Objects.toString(o,null)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(couponStrs)) {
            saveEmptyCouponListToCache(userId, Collections.singletonList(status));
            return Collections.emptyList();
        }
        return couponStrs.stream().map(cs -> JSON.parseObject(cs,Coupon.class)).collect(Collectors.toList());
    }

    /**
     * save List<Coupon> which are null to Cache
     * 目的： 避免缓存穿透
     * @param userId user id
     * @param status coupon status
     */
    @Override
    public void saveEmptyCouponListToCache(Long userId, List<Integer> status) {
        log.info("Save empty list to cache for user: {}, status: {}",userId, JSON.toJSONString(status));
        Map<String, String> invalidCouponMap = new HashMap<>();
        invalidCouponMap.put("-1",JSON.toJSONString(Coupon.invalidCoupon()));
        //使用 SessionCallback 把数据命令放入到 Redis 的 pipeline
        //redis 的 pipeline 可以让我们一次性执行多个命令,统一返回结果，而不用每一个命令去返回；
        //redis 本身是单进程单线程的，你发送一个命令，他给你一个返回，然后你才可以发生下一个命令给他。
        // 单线程指的是网络请求模块使用了一个线程（所以不需考虑并发安全性），即一个线程处理所有网络请求，其他模块仍用了多个线程
        //我们都知道Redis有两种持久化的方式，一种是RDB，一种是AOF。
        //拿RDB举例，执行bgsave，就意味着 fork 出一个子进程在后台进行备份。
        //这也就为什么执行完bgsave命令之后，还能对该Redis实例继续其他的操作。
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
               status.forEach( s -> {
                   //TODO: 把用户 ID 和 优惠券使用 status 进行拼接，作为 redisKey
                    String redisKey = status2RedisKey(s,userId);
                    operations.opsForHash().putAll(redisKey,invalidCouponMap);
               });
                return null;
            }
        };
        log.info("Pipeline exe result: {}", JSON.toJSONString(redisTemplate.executePipelined(sessionCallback)));

    }

    /**
     * try to get one coupon code from Cache
     *
     * @param templateId coupon template id
     * @return coupon code
     */
    @Override
    public String tryToAcquireCouponCodeFromCache(Integer templateId) {
        String redisKey = String.format("%s%s",Constant.RedisPrefix.COUPON_TEMPLATE,templateId.toString());
        //优惠券码没有顺序，左边 pop 和右边 pop, 没有影响(取完或不存在时会取出 null )
        String couponCode = redisTemplate.opsForList().leftPop(redisKey);
        log.info("Acquire Coupon Code: {}, {}, {}", templateId, redisKey, couponCode);
        return couponCode;
    }

    /**
     * save coupon to Cache
     *
     * @param userId  user id
     * @param coupons List<Coupon> {@link Coupon}
     * @param status  coupon status
     * @return count of success save
     * @throws CouponException
     */
    @Override
    public Integer addCouponToCache(Long userId, List<Coupon> coupons, Integer status) throws CouponException {
        log.info("Add coupon to cache: {}, {}, {}", userId, JSON.toJSONString(coupons), status);
        Integer result = -1;
        CouponStatus couponStatus = CouponStatus.of(status);
        //TODO: 不同优惠券状态，需要执行不同的营销策略
        switch (couponStatus) {
            case USABLE:
                result = addCouponToCacheForUsable(userId,coupons);
                break;
            case USED:
                result = addCouponToCacheForUsed(userId, coupons);
                break;
            case EXPIRED:
                result = addCouponToCacheForExpired(userId, coupons);
                break;
        }
        return null;
    }

    /**
     * insert usable coupon to Cache
     * @param userId
     * @param coupons
     * @return
     */
    private Integer addCouponToCacheForUsable(Long userId, List<Coupon> coupons) {
        // 如果 status 是 USABLE, 代表是新增的优惠券
        // 只会影响到一个 cache ： USER_COUPON_USABLE
        log.debug("Add Coupon To Cache For Usable");
        Map<String, String> needCacheObject = new HashMap<>();
        coupons.forEach( coupon -> {
            needCacheObject.put(coupon.getId().toString(),JSON.toJSONString(coupon));
        });
        String redisKey = status2RedisKey(
                CouponStatus.USABLE.getCode(), userId
        );
        //TODO: redis 中的 Hash key 不能重复，needCacheObject 对应HashMap ,
        // coupon id 不可能重复，所以直接 putAll 不会有问题
        redisTemplate.opsForHash().putAll(redisKey, needCacheObject);
        log.info("Add {} Coupons TO Cache: {} , {}", needCacheObject.size(), userId, redisKey);
        //TODO: set Expiration Time, 1h - 2h ,random time
        redisTemplate.expire(redisKey, getRandomExpirationTime(1,2) , TimeUnit.SECONDS);
        return needCacheObject.size();
    }

    /**
     * insert used coupon to cache
     * @param userId
     * @param coupons
     * @return
     */
    private Integer addCouponToCacheForUsed(Long userId, List<Coupon> coupons) throws CouponException{
        // 如果 status 是 USED , 代表用户操作是使用当前的优惠券， 影响到两个 Cache
        // USABLE, USED, 使用后，可以状态变为使用状态
        log.debug("Add Coupon To Cache For Used.");
        Map<String, String> needCacheForUsed = new HashMap<>(coupons.size());
        String redisKeyForUsable = status2RedisKey(
                CouponStatus.USABLE.getCode(),userId
        );
        String redisKeyForUsed = status2RedisKey(
                CouponStatus.USED.getCode(),userId
        );
        // TODO: 获取当前用户可用的优惠券
        List<Coupon> curUsableCoupons = getCacheCoupons(
                userId, CouponStatus.USABLE.getCode()
        );
        // TODO: 当前可用的优惠券数量一定大于1（所有用户都会预先放入一个无效的优惠券信息，加上一条有效的，所以数量一定是大于1的）
        assert curUsableCoupons.size() > coupons.size();
        coupons.forEach( c -> {
            needCacheForUsed.put(c.getId().toString(),JSON.toJSONString(c));
        });
        //TODO: 校验当前优惠券参数是否与 Cache 中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());;
        if (!CollectionUtils.isSubCollection(paramIds, curUsableIds)) {
            log.error("CurCoupons Is Not Equal To Cache: {}, {}, {}",userId, JSON.toJSONString(paramIds),
                    JSON.toJSONString(curUsableIds));
            throw new CouponException("CurCoupons Is Not Equal To Cache.");
        }
        List<String> needCleanKey = paramIds.stream()
                .map( i -> i.toString()).collect(Collectors.toList());
        SessionCallback<Object> sessionCallback = new SessionCallback<Object>() {
            @Override
            public  Object execute(RedisOperations operations) throws DataAccessException {
                //1. 已使用的优惠券 Cache 缓存添加
                operations.opsForHash().putAll(
                        redisKeyForUsable, needCacheForUsed
                );
                //2. 可用的优惠券 Cache 需要清理
                operations.opsForHash().delete(
                        redisKeyForUsable, needCleanKey.toArray()
                );
                // 3. 重置过期时间
                operations.expire(
                        redisKeyForUsable,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS);
                operations.expire(
                        redisKeyForUsed,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );

                return null;
            }

        };
        log.info("Pipeline Exe Result: {}",
                JSON.toJSONString(
                        redisTemplate.executePipelined(sessionCallback)));
        return coupons.size();
    }

    /**
     * <h2>将过期优惠券加入到 Cache 中</h2>
     * */
    @SuppressWarnings("all")
    private Integer addCouponToCacheForExpired(Long userId, List<Coupon> coupons)
            throws CouponException {

        // status 是 EXPIRED, 代表是已有的优惠券过期了, 影响到两个 Cache
        // USABLE, EXPIRED

        log.debug("Add Coupon To Cache For Expired.");

        // 最终需要保存的 Cache
        Map<String, String> needCachedForExpired = new HashMap<>(coupons.size());

        String redisKeyForUsable = status2RedisKey(
                CouponStatus.USABLE.getCode(), userId
        );
        String redisKeyForExpired = status2RedisKey(
                CouponStatus.EXPIRED.getCode(), userId
        );

        List<Coupon> curUsableCoupons = getCacheCoupons(
                userId, CouponStatus.USABLE.getCode()
        );

        // 当前可用的优惠券个数一定是大于1的
        assert curUsableCoupons.size() > coupons.size();

        coupons.forEach(c -> needCachedForExpired.put(
                c.getId().toString(),
                JSON.toJSONString(c)
        ));

        // 校验当前的优惠券参数是否与 Cached 中的匹配
        List<Integer> curUsableIds = curUsableCoupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        List<Integer> paramIds = coupons.stream()
                .map(Coupon::getId).collect(Collectors.toList());
        if (!CollectionUtils.isSubCollection(paramIds, curUsableIds)) {
            log.error("CurCoupons Is Not Equal To Cache: {}, {}, {}",
                    userId, JSON.toJSONString(curUsableIds),
                    JSON.toJSONString(paramIds));
            throw new CouponException("CurCoupon Is Not Equal To Cache.");
        }

        List<String> needCleanKey = paramIds.stream()
                .map(i -> i.toString()).collect(Collectors.toList());

        SessionCallback<Objects> sessionCallback = new SessionCallback<Objects>() {
            @Override
            public Objects execute(RedisOperations operations) throws DataAccessException {

                // 1. 已过期的优惠券 Cache 缓存
                operations.opsForHash().putAll(
                        redisKeyForExpired, needCachedForExpired
                );
                // 2. 可用的优惠券 Cache 需要清理
                operations.opsForHash().delete(
                        redisKeyForUsable, needCleanKey.toArray()
                );
                // 3. 重置过期时间
                operations.expire(
                        redisKeyForUsable,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );
                operations.expire(
                        redisKeyForExpired,
                        getRandomExpirationTime(1, 2),
                        TimeUnit.SECONDS
                );

                return null;
            }
        };

        log.info("Pipeline Exe Result: {}",
                JSON.toJSONString(
                        redisTemplate.executePipelined(sessionCallback)
                ));

        return coupons.size();
    }

    /**
     * Get Redis Key According to  status
     * @param status
     * @param userId
     * @return
     */
    private String status2RedisKey(Integer status, Long userId) {
        String redisKey = null;
        CouponStatus couponStatus = CouponStatus.of(status);
        switch (couponStatus) {
            case USABLE:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USABLE, userId);
                break;
            case USED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_USED, userId);
                break;
            case EXPIRED:
                redisKey = String.format("%s%s", Constant.RedisPrefix.USER_COUPON_EXPIRED, userId);
        }
        return redisKey;
    }

    /**
     *  get one Random Expiration Time
     *  缓存雪崩：key 在同一时间失效
     * @param min 最小小时数
     * @param max 最大小时数
     * @return 返回 【min, max】之间的随机秒数
     */
    private long getRandomExpirationTime(Integer min, Integer max) {
        return RandomUtils.nextLong(min * 60 * 60, max * 60 * 60);
    }
}
