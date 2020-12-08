package com.tjslzhkj.coupon.service;

import com.tjslzhkj.coupon.entity.Coupon;
import com.tjslzhkj.coupon.exception.CouponException;

import java.util.List;

/**
 * Redis 操作服务接口定义
 * 1.用户的三个状态的优惠券 Cache 的相关操作
 * 2.优惠券模板生成的优惠码 Cache 操作
 * **
 * Yuezejian  Created in 2020/11/25 下午11:10
 */
public interface IRedisService {

    /**
     * According to userId and coupon status find List<Coupon> from Cache
     * @param userId user id
     * @param status coupon status {@link com.tjslzhkj.coupon.constant.CouponStatus}
     * @return
     */
    List<Coupon> getCacheCoupons(Long userId, Integer status);

    /**
     * save List<Coupon> which are null to Cache
     * @param userId user id
     * @param status coupon status
     */
    void saveEmptyCouponListToCache(Long userId, List<Integer> status);

    /**
     * try to get one coupon code from Cache
     * @param templateId coupon template id
     * @return coupon code
     */
    String tryToAcquireCouponCodeFromCache(Integer templateId);

    /**
     * save coupon to Cache
     * @param userId user id
     * @param coupons List<Coupon> {@link Coupon}
     * @param status coupon status
     * @return
     * @throws CouponException
     */
    Integer addCouponToCache(Long userId, List<Coupon> coupons,Integer status) throws CouponException;
}
