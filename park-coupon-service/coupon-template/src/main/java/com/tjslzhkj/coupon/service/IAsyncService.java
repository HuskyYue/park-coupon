package com.tjslzhkj.coupon.service;

import com.tjslzhkj.coupon.entity.CouponTemplate;
import org.springframework.stereotype.Service;

/**
 * 异步服务接口定义
 * **
 * Yuezejian  Created in 2020/11/11 下午8:43
 */
@Service
public interface IAsyncService {

    /**
     * 根据模板异步的创建优惠券码
     * @param template
     */
    void asyncConstructCouponByTemplate(CouponTemplate template);

}
