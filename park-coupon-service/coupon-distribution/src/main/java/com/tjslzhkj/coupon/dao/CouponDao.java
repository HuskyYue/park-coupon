package com.tjslzhkj.coupon.dao;

import com.tjslzhkj.coupon.constant.CouponStatus;
import com.tjslzhkj.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 优惠券 Dao 接口定义
 * **
 * Yuezejian  Created in 2020/11/25 下午11:02
 */
@Service
public interface CouponDao extends JpaRepository<Coupon,Integer> {

    /**
     * 根据userId + 优惠券状态查找优惠券记录
     * @param userId
     * @param status
     * @return
     */
    List<Coupon> findAllByUserIdAndStatus(Long userId, CouponStatus status);
}
