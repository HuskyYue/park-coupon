package com.tjslzhkj.coupon.executor;

import com.alibaba.fastjson.JSON;
import com.tjslzhkj.coupon.vo.GoodsInfo;
import com.tjslzhkj.coupon.vo.SettlementInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>规则执行器抽象类，定义通用方法</h1>
 * **
 * Yuezejian  Created in 2020/12/13 下午9:30
 */
public abstract class AbstractExecutor {

    /**
     * <h2>校验商品类型与优惠券是否匹配</h2>
     * notify:
     * 1.这里是单品类优惠券校验，多品类自行重栽此方法
     * 2.商品只要有一个优惠券要求的商品类型去匹配即可，因为商品可能有多个类型， 而优惠券是根据商品类型匹配用的
     *
     * @param settlement
     * @return
     */
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement) {

        // 获取结算信息中商品类型
        List<Integer> goodsType = settlement.getGoodsInfos().stream()
                .map(GoodsInfo::getType)
                .collect(Collectors.toList());

        // 获取优惠券列表中，所有优惠券可用于的商品类型
        List<Integer> templateGoodsType = JSON.parseObject(
                settlement.getCouponAndTemplateInfos()
                        .get(0).getTemplate().getRule()
                        .getUsage().getGoodsType(),
                List.class
        );

        // 存在交集即可，判断商品类型是否有可用的优惠券
        return CollectionUtils.isNotEmpty(
                CollectionUtils.intersection(goodsType, templateGoodsType)
        );

    }

    /**
     * <h2>处理商品类型与优惠券限制不匹配的情况</h2>
     * @param settlement {@link SettlementInfo} 用户传递的结算信息
     * @param goodsSum 商品总价
     * @return {@link SettlementInfo} 已经修改过的结算信息
     */
    protected SettlementInfo processGoodsTypeNotSatisfy(
            SettlementInfo settlement, double goodsSum
    ) {
        boolean isGoodsTypeSatisfy = isGoodsTypeSatisfy(settlement);

        // 没有可用优惠券，总金额就是最终金额，清空优惠券列表
        if (!isGoodsTypeSatisfy) {
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        // 有可用优惠券时，返回 null
        return null;

    }

    /**
     *  计算总价
     * @param goodsInfos {@link GoodsInfo} 结算信息中的商品信息列表
     * @return
     */
    protected double goodsCostSum(List<GoodsInfo> goodsInfos) {
        return goodsInfos.stream().mapToDouble(
                g -> g.getPrice() * g.getCount()
        ).sum();
    }

    /**
     * 保留2位小数
     * @param val
     * @return
     */
    protected double retain2Decimals(double val) {
        return new BigDecimal(val).setScale(
                2, BigDecimal.ROUND_HALF_UP
        ).doubleValue();
    }

    /**
     * 最小支付费用 （优惠力度大时，可能只需要支付0.01，我们为其设置最小支付额度）
     * @return
     */
    protected double minCost() {
        return 0.1;
    }
}
