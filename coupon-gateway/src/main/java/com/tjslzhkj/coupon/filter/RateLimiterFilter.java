package com.tjslzhkj.coupon.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.google.common.util.concurrent.RateLimiter;

import javax.servlet.http.HttpServletRequest;

/**
 * <h1>限流过滤器</h1>
 * Created by Qinyi.
 */
@Component
@SuppressWarnings("all")
public class RateLimiterFilter extends AbstractPreZuulFilter{

    public static final Logger LOG = LoggerFactory.getLogger(RateLimiterFilter.class);

    /** 每秒可以获取到两个令牌 */
    RateLimiter rateLimiter = RateLimiter.create(2.0);

    @Override
    protected Object cRun() {

        HttpServletRequest request = context.getRequest();

        if (rateLimiter.tryAcquire()) {
            LOG.info("get rate token success");
            return success();
        } else {
            LOG.error("rate limit: {}", request.getRequestURI());
            return fail(402, "error: rate limit");
        }
    }

    /**
     * filterOrder() must also be defined for a filter. Filters may have the same  filterOrder if precedence is not
     * important for a filter. filterOrders do not need to be sequential.
     *
     * @return the int order of a filter
     */
    @Override
    public int filterOrder() {
        return 2;
    }
}
