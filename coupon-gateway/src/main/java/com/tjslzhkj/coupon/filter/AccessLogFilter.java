package com.tjslzhkj.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * 网关请求日志打印拦截器
 *
 * Yuezejian  Created in 2020/11/1 下午11:20
 */
@Slf4j
@Component
public class AccessLogFilter extends AbstractPostZuulFilter {
    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();
        //从 PreRequestFilter 中获取请求开始时间戳
        Long startTime = (Long) context.get("startTime");
        String uri = request.getRequestURI();
        long duration = System.currentTimeMillis() - startTime;
        //从网关通过的请求，都会打印日志记录
        log.info("uri: {}, duration: {}", uri,duration);
        return success();
    }

    /**
     * SEND_RESPONSE_FILTER_ORDER默认1000
     * @return
     */
    @Override
    public int filterOrder() {
        return FilterConstants.SEND_RESPONSE_FILTER_ORDER - 1;
    }
}
