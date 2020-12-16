package com.tjslzhkj.coupon.executor.impl;

import com.alibaba.fastjson.JSON;
import com.tjslzhkj.coupon.constant.CouponCategory;
import com.tjslzhkj.coupon.constant.RuleFlag;
import com.tjslzhkj.coupon.executor.AbstractExecutor;
import com.tjslzhkj.coupon.executor.RuleExecutor;
import com.tjslzhkj.coupon.vo.GoodsInfo;
import com.tjslzhkj.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <h1>满减 + 折扣优惠券规则执行器</h1>
 *
 * **
 * Yuezejian  Created in 2020/12/15 下午9:58
 */
@Slf4j
@Component
public class ManJianZheKouExecutor extends AbstractExecutor implements RuleExecutor {

    /**
     * <h2>规则类型标记</h2>
     *
     * @return {@link RuleFlag}
     */
    @Override
    public RuleFlag ruleConfig() {
        return RuleFlag.MANJIAN_ZHEKOU;
    }

    /**
     * <h2>校验商品类型与优惠券是否匹配</h2>
     * notify:
     * 1.这里是单品类优惠券校验，多品类自行重写此方法
     * 2.如果想要使用多类优惠券， 则必须要所有的商品类型都包含在内
     * @param settlement
     * @return
     */
    @Override
    protected boolean isGoodsTypeSatisfy(SettlementInfo settlement) {

        log.debug("Check ManJian And ZheKou Is Match Or Not!");
        List<Integer> goodsType = settlement.getGoodsInfos().stream()
                .map(GoodsInfo::getType).collect(Collectors.toList());
        List<Integer> templateGoodsType = new ArrayList<>();
        settlement.getCouponAndTemplateInfos().forEach( ct -> {
            templateGoodsType.addAll(JSON.parseObject(
                    ct.getTemplate().getRule().getUsage().getGoodsType(),
                    List.class
            ));
        });
        // 如果想要使用多类优惠券， 则必须要所有的商品类型都包含在内
        return CollectionUtils.isSubCollection(goodsType, templateGoodsType);
    }

    /**
     * <h2>优惠券规则的计算</h2>
     * @param settlement {@link SettlementInfo} 包含了选择的优惠券
     * @return {@link SettlementInfo} 包含了修正过的结算信息
     */
    @Override
    public SettlementInfo computeRule(SettlementInfo settlement) {

        double goodsSum = retain2Decimals(goodsCostSum(
                settlement.getGoodsInfos()
        ));
        // 实际调用的是子类中已经重写了的 isGoodsTypeSatisfy（) 方法
        // 完成了对商品类型的校验
        SettlementInfo probability = processGoodsTypeNotSatisfy(
                settlement, goodsSum
        );
        if (null != probability) {
            log.debug("ManJian And ZheKou Template Is Not Match To GoodsType");
            return probability;
        }
        SettlementInfo.CouponAndTemplateInfo manJian = null;
        SettlementInfo.CouponAndTemplateInfo zheKou = null;

        for (SettlementInfo.CouponAndTemplateInfo ct :
                settlement.getCouponAndTemplateInfos()) {
            if (CouponCategory.of(ct.getTemplate().getCategory()) == CouponCategory.MANJIAN) {
                manJian = ct;
            } else {
                zheKou = ct;
            }
        }
        assert  null != manJian;
        assert  null != zheKou;

        // 当前的优惠券和满减券如果不能一起使用，清空优惠券，返回商品原价
        if (!isTemplateCanShared(manJian, zheKou)) {
            log.debug("Currency ManJian And ZheKou Can Not Be Used Together");
            settlement.setCost(goodsSum);
            settlement.setCouponAndTemplateInfos(Collections.emptyList());
            return settlement;
        }

        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = new ArrayList<>();
        double manJianBase = (double) manJian.getTemplate().getRule()
                .getDiscount().getBase();
        double manJianQuota = (double) manJian.getTemplate().getRule()
                .getDiscount().getQuota();
        // 最终价格 (先满减再折扣)
        double targetSum = goodsSum;
        // (必须达到满减的基准才可减)
        if (targetSum >= manJianBase) {
            targetSum -= manJianQuota;
            ctInfos.add(manJian);
        }

        // 再计算折扣
        double zheKouQuota = (double) zheKou.getTemplate().getRule()
                .getDiscount().getQuota();
        targetSum *= zheKouQuota * 1.0 / 100;
        ctInfos.add(zheKou);

        settlement.setCouponAndTemplateInfos(ctInfos);
        settlement.setCost(retain2Decimals(
                targetSum > minCost() ? targetSum : minCost()
        ));
        log.debug("Use ManJian And ZheKou Coupon Make Goods Const From {} To {}",
                goodsSum, targetSum);
        return null;
    }

    /**
     * <h2>当前的两张优惠券是否可以共用</h2>
     *  即校验 TemplateRule 中的 weight 是否满足条件
     * @param manJian
     * @param zheKou
     * @return
     */
    private boolean isTemplateCanShared(SettlementInfo.CouponAndTemplateInfo manJian,
                                        SettlementInfo.CouponAndTemplateInfo zheKou) {
        String manJanKey = manJian.getTemplate().getKey()
                + String.format("%04d", manJian.getTemplate().getId());
        String zheKouKey = zheKou.getTemplate().getKey()
                + String.format("%04d", zheKou.getTemplate().getId());
        // 所有可以与当前满减券一起使用的优惠券
        List<String> allSharedKeysForManJian = new ArrayList<>();
        allSharedKeysForManJian.add(manJanKey);
        allSharedKeysForManJian.addAll(JSON.parseObject(
                manJian.getTemplate().getRule().getWeight(),
                List.class
        ));
        // 所有可以与当前折扣优惠券一起使用的优惠券
        List<String> allSharedKeysForZheKou = new ArrayList<>();
        allSharedKeysForZheKou.add(zheKouKey);
        allSharedKeysForManJian.addAll(JSON.parseObject(
                zheKou.getTemplate().getRule().getWeight(),
                List.class
        ));

        // 满足任何一个就可以一起使用
        return CollectionUtils.isSubCollection(
                Arrays.asList(manJanKey,zheKouKey), allSharedKeysForManJian)
                || CollectionUtils.isSubCollection(
                        Arrays.asList(manJanKey,zheKouKey), allSharedKeysForManJian);


    }
}
