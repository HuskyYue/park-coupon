package com.tjslzhkj.coupon.executor.impl;

import com.tjslzhkj.coupon.constant.RuleFlag;
import com.tjslzhkj.coupon.executor.AbstractExecutor;
import com.tjslzhkj.coupon.executor.RuleExecutor;
import com.tjslzhkj.coupon.vo.CouponTemplateSDK;
import com.tjslzhkj.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <h1>折扣优惠券结算规则执行器</h1>
 * **
 * Yuezejian  Created in 2020/12/14 下午9:51
 */
@Slf4j
@Component
public class ZheKouExecutor extends AbstractExecutor implements RuleExecutor {
    /**
     * <h2>规则类型标记</h2>
     *
     * @return {@link RuleFlag}
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.ZHEKOU;
    }

    /**
     * <h2>优惠券规则的计算</h2>
     *
     * @param settlement {@link SettlementInfo} 包含了选择的优惠券
     * @return {@link SettlementInfo} 包含了修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {
        // 计算总价
        double goodsSum = retain2Decimals(goodsCostSum(
                        settlement.getGoodsInfos()
                ));
        SettlementInfo probability = processGoodsTypeNotSatisfy(
                settlement, goodsSum
        );
        if (null != probability) {
            log.debug("ZheKou template is not match goodsType!");
            return probability;
        }

        // 折扣优惠券可以直接使用， 没有使用门槛
        CouponTemplateSDK templateSDK = settlement.getCouponAndTemplateInfos().get(0).getTemplate();
        double quota = (double) templateSDK.getRule().getDiscount().getQuota();
        // 计算使用优惠券之后的价格
        double afterQuota = goodsSum * (quota * 1.0 / 100);
        settlement.setCost(
                retain2Decimals(afterQuota > minCost() ? afterQuota : minCost())
        );
        log.debug("User ZheKou coupon make coupon cost from {} to {}",
                goodsSum, settlement.getCost());
        return settlement;
    }
}
