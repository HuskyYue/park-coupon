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
public class TokenFilter extends AbstractPreZuulFilter{
    /**
     * 判断 token 是否为空
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
