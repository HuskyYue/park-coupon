package com.tjslzhkj.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

/**
 * 校验请求中传递的token filter
 * ** 
 * Yuezejian  Created in 2020/11/1 下午10:07
 */
@Slf4j
@Component
public class TokenFilter extends AbstractZuulFilter{
    /**
     * 不希望在 run() 方法中有具体的执行细节，所以返回一个相同类型的抽象
     *
     */
    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();
        //打印请求的方法类型和URL
        log.info(String.format("%s request to %s",
                request.getMethod(),request.getRequestURL().toString()));
        Object token = request.getParameter("token");
        if (token == null) {
            log.error("error: token is empty");
            return fail(401,"error: token is empty");
        }
          return success();
    }

    /**
     * to classify a filter by type. Standard types in Zuul are "pre" for pre-routing filtering,
     * "route" for routing to an origin, "post" for post-routing filters, "error" for error handling.
     * We also support a "static" type for static responses see  StaticResponseFilter.
     * Any filterType made be created or added and run by calling FilterProcessor.runFilters(type)
     *
     * @return A String representing that type
     */
    @Override
    public String filterType() {
        return null;
    }

    /**
     * filterOrder() must also be defined for a filter. Filters may have the same  filterOrder if precedence is not
     * important for a filter. filterOrders do not need to be sequential.
     *
     * @return the int order of a filter
     */
    @Override
    public int filterOrder() {
        return 1;
    }
}
