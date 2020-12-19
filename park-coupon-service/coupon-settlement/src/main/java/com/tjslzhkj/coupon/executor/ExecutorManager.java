package com.tjslzhkj.coupon.executor;

import com.tjslzhkj.coupon.constant.CouponCategory;
import com.tjslzhkj.coupon.constant.RuleFlag;
import com.tjslzhkj.coupon.exception.CouponException;
import com.tjslzhkj.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>优惠券结算规则管理器</h1>
 *  即根据用户的请求（SettlementInfo）找到对应的 Executor, 去做结算
 *  BeanPostProcessor: Bean 后置处理器
 * **
 * Yuezejian  Created in 2020/12/16 下午9:31
 */
@Slf4j
@Component
@SuppressWarnings("all")
public class ExecutorManager implements BeanPostProcessor {

    /** <h2>规则执行器映射</h2> */
    private static Map<RuleFlag, RuleExecutor> executorIndex =
            new HashMap<>(RuleFlag.values().length);

    /** <h2>优惠券结算规则计算入口（注意：必须保证传递进来的优惠券个数 >= 1）</h2>  */
    public SettlementInfo computeRule(SettlementInfo settlement)
            throws CouponException {

        SettlementInfo result = null;

        // TODO: 单类优惠券
        if (settlement.getCouponAndTemplateInfos().size() == 1) {

            // 获取优惠券的类别
            CouponCategory category = CouponCategory.of(
                    settlement.getCouponAndTemplateInfos().get(0)
                            .getTemplate().getCategory()
            );
            switch (category) {
                case MANJIAN:
                    result = executorIndex.get(RuleFlag.MANJIAN)
                            .computeRule(settlement);
                    break;
                case LIJIAN:
                    result = executorIndex.get(RuleFlag.LIJIAN)
                            .computeRule(settlement);
                    break;
                case ZHEKOU:
                    result = executorIndex.get(RuleFlag.ZHEKOU)
                            .computeRule(settlement);
                    break;
            }
        } else {
            // TODO: 处理多类优惠券
            // 先获取到结算中使用到的多种优惠券类别
            List<CouponCategory> categories = new ArrayList<>(
                    settlement.getCouponAndTemplateInfos().size()
            );
            settlement.getCouponAndTemplateInfos().forEach( ct -> categories.add(
                    CouponCategory.of(ct.getTemplate().getCategory())
            ));
            if (categories.size() !=2) {
                throw new CouponException("Not Support For More Template Category");
            } else {
                if (categories.contains(CouponCategory.MANJIAN)
                        && categories.contains(CouponCategory.ZHEKOU)) {
                    result = executorIndex.get(RuleFlag.MANJIAN_ZHEKOU)
                            .computeRule(settlement);
                } else {
                    throw new CouponException("Not Support For Other Template Cagegory");
                }
            }
        }
        return result;
    }

    /** <h2>在 bean 初始化之前去执行（before）</h2> */
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName)
            throws BeansException {

        if (!(bean instanceof RuleExecutor)) {
            return bean;
        }
        RuleExecutor executor = (RuleExecutor) bean;
        RuleFlag ruleFlag = executor.ruleConfig();

        if (executorIndex.containsKey(ruleFlag)) {
            throw new IllegalStateException("There is already " +
                    "an executor for rule flag:" + ruleFlag);
        }
        log.info("Load executor for RuleFlag");
        executorIndex.put(ruleFlag, executor);

        return null;
    }

    /** 在 bean 初始化之后去执行（after） */
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName)
            throws BeansException {
        return bean;
    }
}
