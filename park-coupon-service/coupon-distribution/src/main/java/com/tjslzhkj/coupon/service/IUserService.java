package com.tjslzhkj.coupon.service;

import com.tjslzhkj.coupon.entity.Coupon;
import com.tjslzhkj.coupon.exception.CouponException;
import com.tjslzhkj.coupon.vo.AcquireTemplateRequest;
import com.tjslzhkj.coupon.vo.CouponTemplateSDK;
import com.tjslzhkj.coupon.vo.SettlementInfo;

import java.util.List;

/**
 * 用户服务相关的接口定义
 * 1. 用户三类状态优惠券消息展示服务
 * 2. 查看用户当前可以领取的优惠券模板
 * 3. 用户领取优惠券服务
 * 4. 用户消费优惠券服务 - coupon-settlement 微服务配合实现
 * **
 * Yuezejian  Created in 2020/11/27 下午7:46
 */
public interface IUserService {

    /**
     * 根据用户 ID 和状态查询优惠券记录
     * @param userId 用户 id
     * @param status 优惠券状态
     * @return {@link Coupon}
     * @throws CouponException
     */
    List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException;

    /**
     * 根据用户 ID 查找当前可以领取的优惠券模板
     * @param userId 用户 ID
     * @return {@link CouponTemplateSDK}
     * @throws CouponException
     */
    List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException;

    /**
     * 用户领取优惠券
     * @param request {@link AcquireTemplateRequest}
     * @return {@link Coupon}
     * @throws CouponException
     */
    Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException;

    /**
     * 结算（核销）优惠券
     * @param info {@link SettlementInfo}
     * @return {@link SettlementInfo}
     * @throws CouponException
     */
    SettlementInfo settlement(SettlementInfo info) throws CouponException;



}
