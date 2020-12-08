package com.tjslzhkj.coupon.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 优惠券的状态信息
 * **
 * Yuezejian  Created in 2020/11/25 下午8:44
 */
@Getter
@AllArgsConstructor
public enum CouponStatus {
    USABLE("可用的",1),
    USED("已使用的",2),
    EXPIRED("过期的（未被使用的）",3);

    /** 优惠券状态描述信息 */
    private String description;

    /** 优惠券状态编码 */
    private Integer code;

    /**
     * 根据 code 获取 CouponStatus
     * @param code
     * @return
     */
    public static CouponStatus of(Integer code) {

        Objects.requireNonNull(code);

        return Stream.of(values())
                .filter( bean -> bean.getCode().equals(code) )
                .findAny()
                .orElseThrow(
                () -> new IllegalArgumentException(code + " not exists")
                );

    }

}
