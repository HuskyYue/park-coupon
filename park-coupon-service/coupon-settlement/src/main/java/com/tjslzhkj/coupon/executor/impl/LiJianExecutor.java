package com.tjslzhkj.coupon.executor.impl;

import com.tjslzhkj.coupon.constant.RuleFlag;
import com.tjslzhkj.coupon.executor.AbstractExecutor;
import com.tjslzhkj.coupon.executor.RuleExecutor;
import com.tjslzhkj.coupon.vo.CouponTemplateSDK;
import com.tjslzhkj.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <h1>立减优惠券结算规则执行器</h1>
 * **
 * Yuezejian  Created in 2020/12/14 下午10:13
 */
@Slf4j
@Component
public class LiJianExecutor extends AbstractExecutor implements RuleExecutor {
    /**
     * <h2>规则类型标记</h2>
     *
     * @return {@link RuleFlag}
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.LIJIAN;
    }

    /**
     * <h2>优惠券规则的计算</h2>
     *
     * @param settlement {@link SettlementInfo} 包含了选择的优惠券
     * @return {@link SettlementInfo} 包含了修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        double goodsSum = retain2Decimals(
                goodsCostSum(settlement.getGoodsInfos())
        );
        SettlementInfo probability = processGoodsTypeNotSatisfy(
                settlement, goodsSum
        );
        if (null != probability) {
            log.debug("LiJian template is not match to goodsType!");
            return probability;
        }

        // 立减优惠券直接使用，没有门槛
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos().get(0).getTemplate();
        double quota = (double) templateSDK.getRule().getDiscount().getQuota();
        double afterQuota = goodsSum - quota;
        // 计算使用优惠券后的金额
        settlement.setCost(retain2Decimals(
                afterQuota > minCost() ? afterQuota : minCost()
        ));
        log.debug("Use LiJian Coupon Make Goods Cost From {} To {}",
                goodsSum, settlement.getCost());
        return settlement;
    }
}
