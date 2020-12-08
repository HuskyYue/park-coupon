package com.tjslzhkj.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 分发目标枚举
 * **
 * Yuezejian  Created in 2020/11/9 下午9:39
 */
@Getter
@AllArgsConstructor
public enum DistributeTarget {
    SINGLE("单用户",1),
    MULTI("多用户",2);

    /**
     * 分发类型描述
     */
    private String description;

    /**
     * 分发类型码
     */
    private Integer code;

    public static DistributeTarget of(Integer code) {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(dt -> dt.code == code)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(code + " not exits!"));

    }
}
