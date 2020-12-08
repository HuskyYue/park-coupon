package com.tjslzhkj.coupon.Feign.hystrix;

import com.tjslzhkj.coupon.Feign.SettlementClient;
import com.tjslzhkj.coupon.vo.BaseResponse;
import com.tjslzhkj.coupon.vo.SettlementInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * 结算微服务熔断策略实现
 * **
 * Yuezejian  Created in 2020/12/5 下午7:43
 */
@Slf4j
@Component
public class SettlementClientHystrix implements SettlementClient {
    /**
     * <h2>优惠券计算规则<h2/>
     *
     * @param info {@link SettlementInfo}
     * @return
     */
    @Override
    public BaseResponse<SettlementInfo> computeRule(SettlementInfo info) {
        log.error("[eureka-client-coupon-settlement] computeRule request error");
        //TODO:是否使结算生效, 即核销
        info.setEmploy(false);
        //TODO: 设置无效结算金额，标识当前结算微服务不可以
        info.setCost(-1.0);
        BaseResponse response = new BaseResponse<>(-1,
                "[eureka-client-coupon-settlement] computeRule request error");
        response.setData(info);
        return response;
    }
}
