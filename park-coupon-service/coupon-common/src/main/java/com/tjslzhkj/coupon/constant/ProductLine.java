package com.tjslzhkj.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 产品线枚举
 * **
 * Yuezejian  Created in 2020/11/9 下午9:24
 */
@Getter
@AllArgsConstructor
public enum ProductLine {
    DONGTINGPARK("洞庭路停车场",1),
    NANQIAOPARK("南桥路停车场",2);

    /**
     * 产品线描述
     */
    private String description;

    /**
     * 产品线码
     */
    private Integer code;

    public static ProductLine of(Integer code) {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(pl -> pl.code == code)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(code + " not exits!"));

    }
}
