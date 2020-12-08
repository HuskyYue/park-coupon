package com.tjslzhkj.coupon.Feign;

import com.tjslzhkj.coupon.Feign.hystrix.SettlementClientHystrix;
import com.tjslzhkj.coupon.vo.BaseResponse;
import com.tjslzhkj.coupon.vo.SettlementInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * <h1>优惠券结算微服务 Feign 接口定义</h1>
 *
 * **
 * Yuezejian  Created in 2020/12/5 下午7:01
 */
@FeignClient(value = "eureka-client-coupon-settlement",
        fallback = SettlementClientHystrix.class)
public interface SettlementClient {

    /**
     * <h2>优惠券计算规则<h2/>
     * @param settlementInfo
     * @return
     */
    @RequestMapping(value = "/coupon-settlement/settlement/compute",
            method = RequestMethod.POST)
    BaseResponse<SettlementInfo> computeRule(@RequestBody SettlementInfo settlementInfo);

}
