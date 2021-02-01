package com.tjslzhkj.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 优惠卷分类枚举
 * **
 * Yuezejian  Created in 2020/11/9 下午9:09
 */
@Getter
@AllArgsConstructor
public enum CouponCategory {
    MANJIAN("满减券","001"),
    ZHEKOU("折扣券","002"),
    LIJIAN("立减券","003");

    /**
     * 优惠卷描述（分类）
     */
    private String decription;

    /**
     * 优惠券码
     */
    private String code;


    public static CouponCategory of(String code) {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter( cc -> cc.code.equals(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(code + " not exists!"));
    }
}
