package com.tjslzhkj.coupon.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * fake 商品信息(商品信息 + 优惠券信息  ->  做核销)
 * **
 * Yuezejian  Created in 2020/11/28 下午6:01
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GoodsInfo {

    /** 商品类型 */
    private Integer type;

    /** 商品价格 */
    private Double price;

    /** 商品数量 */
    private Integer count;

    /** 商品名称 */
    private String name;

    /** 商品描述 */
    private String description;


}
