package com.tjslzhkj.coupon.vo;

import com.tjslzhkj.coupon.constant.PeriodType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/**
 * 优惠券规则对象定义
 * **
 * Yuezejian  Created in 2020/11/9 下午9:52
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TemplateRule {

    /**
     * 优惠券过期规则
     */
    private Expiration expiration;

    /**
     * 优惠券折扣规则
     */
    private Discount discount;

    /**
     * 优惠券领取数量限制
     */
    private Integer limitation;

    /**
     * 优惠券使用范围： 地域 + 商品类型
     */
    private Usage usage;

    /**
     * 权重（可用和那些优惠券叠加使用，同一优惠券不能叠加使用）：list[] 优惠券唯一编码
     */
    private String weight;

    /**
     * 校验功能
     * */
    public boolean validate() {

        return expiration.validate() && discount.validate()
                && limitation > 0 && usage.validate()
                && StringUtils.isNotEmpty(weight);
    }

    /**
     * 有效期限规则
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Expiration {
        /**
         * 有效期规则， 对应 PeriodType 的 code 字段
         */
        private Integer period;

        /**
         * 有效期间隔： 只对变动有效期有效
         */
        private Integer gap;

        /**
         * 优惠券模板的失效日期， 两类规则都有效
         */
        private Long deadline;

       public boolean validate() {
            return null != PeriodType.of(period) && gap > 0 && deadline > System.currentTimeMillis();
        }

    }

    /**
     * 折扣， 需要与类型配合使用
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Discount {
        /**
         * 额度：满减（20), 折扣（85）
         */
        private Integer quota;

        /**
         * 基准，需要满多少才可用
         */
        private Integer base;

        boolean validate() {
            return quota > 0 && base > 0;
        }

    }

    /**
     * 使用范围
     */
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Usage {
        /**
         * 省份
         */
        private String province;
        /**
         * 城市
         */
        private String city;
        /**
         * 商品类型
         */
        private String goodsType;

        boolean validate() {
            return StringUtils.isNotBlank(province) &&
                    StringUtils.isNotBlank(city) &&
                    StringUtils.isNotBlank(goodsType);
        }
    }
}
