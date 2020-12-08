package com.tjslzhkj.coupon.filter;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 限流过滤器
 * **
 * Yuezejian  Created in 2020/11/1 下午10:42
 */
@Slf4j
@Component
@SuppressWarnings("all")
public class RateLimiterFilter extends AbstractZuulFilter{

    /**
     * google guava 令牌桶，每秒可以获取两个令牌，即每秒只能有两个请求打过来，获取不到，就说明已经满了，需要进行限流
     * 可以对某些URI,URL进行限制
     */
    RateLimiter rateLimiter = RateLimiter.create(2.0);
    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();

        if (rateLimiter.tryAcquire()) {
            log.info("get data token success");
            return success();
        } else {
            //对哪个URI进行限流
            log.error("rate limit {}" ,request.getRequestURI());
            return fail(402,"error: rate limit");
        }
    }

    @Override
    public String filterType() {
        return null;
    }

    @Override
    public int filterOrder() {
        return 2;
    }
}
