package com.tjslzhkj.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.servlet.http.HttpServletRequest;

/**
 * 校验请求中的 Token
 */

@Component
public class TokenFilter extends AbstractPreZuulFilter {
    public static final Logger LOG = LoggerFactory.getLogger(TokenFilter.class);
    @Override
    protected Object cRun() {
        HttpServletRequest request = context.getRequest();
        LOG.info(String.format("%s request to %s",
                request.getMethod(), request.getRequestURL().toString()));

        Object token = request.getParameter("token");
        if (null == token) {
            LOG.error("error: token is empty");
            return fail(401, "error: token is empty");
        }

        return success();
    }

    @Override
    public int filterOrder() {
        return 1;
    }
}
