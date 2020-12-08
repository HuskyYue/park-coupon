package com.tjslzhkj.coupon.Feign.hystrix;

import com.tjslzhkj.coupon.Feign.TemplateClient;
import com.tjslzhkj.coupon.vo.BaseResponse;
import com.tjslzhkj.coupon.vo.CouponTemplateSDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * <h1>优惠券模板 Feign 接口的熔断降级策略
 * **
 * Yuezejian  Created in 2020/12/5 下午7:14
 */
@Slf4j
@Component
public class TemplateClientHystrix implements TemplateClient {
    /**
     * <h2>查找所有可用的优惠券模板</h2>
     */
    @Override
    public BaseResponse<List<CouponTemplateSDK>> findAllUsableTemplate() {
        log.error("[eureka-client-coupon-template] findAllUsableTemplate " +
                "request error");
        BaseResponse response = new BaseResponse<>(-1,
                "[eureka-client-coupon-template] findAllUsableTemplate request error");
        response.setData(Collections.emptyList());
        return response;
    }

    /**
     * <h2>获取模板 ids 到 CouponTemplateSDK 的映射</h2>
     *
     * @param ids 优惠券模板 id
     * @return
     */
    @Override
    public BaseResponse<Map<Integer, CouponTemplateSDK>> findIds2TemplateSDK(
            Collection<Integer> ids) {
        log.error("[eureka-client-coupon-template] findIds2TemplateSDK " +
                "request error");
        BaseResponse response = new BaseResponse(-1,
                "[eureka-client-coupon-template] findIds2TemplateSDK request error");
        response.setData(new HashMap<>());
        return response;
    }
}
