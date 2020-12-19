package com.tjslzhkj.coupon.controller;

import com.alibaba.fastjson.JSON;
import com.netflix.discovery.converters.Auto;
import com.tjslzhkj.coupon.exception.CouponException;
import com.tjslzhkj.coupon.executor.ExecutorManager;
import com.tjslzhkj.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <h1>结算服务 Controller</h1>
 * **
 * Yuezejian  Created in 2020/12/18 上午5:33
 */
@Slf4j
@RestController
public class SettlementController {

    /** 结算规则执行管理器 */
    private final ExecutorManager executorManager;

    @Autowired
    public SettlementController(ExecutorManager executorManager) {
        this.executorManager = executorManager;
    }

    /**
     * url:
     * <h2>优惠券结算</h2>
     * @param settlement {@link SettlementInfo}
     * @return
     * @throws CouponException
     */
    @PostMapping("/settlement/compute")
    public SettlementInfo computeRule(SettlementInfo settlement)
            throws CouponException {
        log.info("settlement: {}", JSON.toJSONString(settlement));
        return executorManager.computeRule(settlement);
    }
}
