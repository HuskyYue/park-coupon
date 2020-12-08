package com.tjslzhkj.coupon.vo;

import com.tjslzhkj.coupon.constant.CouponStatus;
import com.tjslzhkj.coupon.constant.PeriodType;
import com.tjslzhkj.coupon.entity.Coupon;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.time.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户优惠券的分类 (根据优惠券状态)
 * **
 * Yuezejian  Created in 2020/12/6 上午6:41
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CouponClassify {

    /** 可以使用的 */
    private List<Coupon> usable;

    /** 已使用的 */
    private List<Coupon> used;

    /** 已过期的 */
    private List<Coupon> expired;

    /**
     *
     * @param coupons
     * @return
     */
    public static CouponClassify classify(List<Coupon> coupons) {
        List<Coupon> usable = new ArrayList<>(coupons.size());
        List<Coupon> used = new ArrayList<>(coupons.size());
        List<Coupon> expired = new ArrayList<>(coupons.size());
        coupons.forEach( c -> {
            //TODO: 根据优惠券过期规则，判断其当前是否已经过期
            boolean isTimeExpire;
            long curTime = new Date().getTime();
            TemplateRule.Expiration expirationRule = c.getTemplateSDK().getRule().getExpiration();
            //PeriodType两种：
            if (expirationRule.equals(
                    PeriodType.REGULAR.getCode()
            )) {
                //1（固定的（固定日期））
                isTimeExpire = expirationRule.getDeadline() <= curTime;
            } else {
                //2（变动的（以领取之日开始计算））
                isTimeExpire = DateUtils.addDays(
                        c.getAssignTime(),
                        expirationRule.getGap()
                ).getTime() <= curTime;
            }

            if (c.getStatus() == CouponStatus.USED) {
                used.add(c);
                //TODO: 优惠券被手动失效，或优惠券已超过有效期
            } else if (c.getStatus() == CouponStatus.EXPIRED
                    || isTimeExpire) {
                expired.add(c);
            } else {
                usable.add(c);
            }
        });
        return new CouponClassify(usable,used,expired);
    }

}
