package com.tjslzhkj.coupon.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Objects;
import java.util.stream.Stream;

/**
 * 有效期类型枚举
 * **
 * Yuezejian  Created in 2020/11/9 下午9:43
 */
@Getter
@AllArgsConstructor
public enum PeriodType {
    REGULAR("固定的（固定日期）",1),
    SHIFT("变动的（以领取之日开始计算）",2);

    /**
     * 有效期类型描述
     */
    private String description;

    /**
     * 有效期类型码
     */
    private Integer code;

    public static PeriodType of(Integer code) {
        Objects.requireNonNull(code);
        return Stream.of(values())
                .filter(pt -> pt.code == code)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException(code + " not exists!"));
    }

}
