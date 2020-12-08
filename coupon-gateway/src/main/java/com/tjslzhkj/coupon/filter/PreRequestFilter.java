package com.tjslzhkj.coupon.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 在过滤器中存储客户端发起请求的时间戳
 * **
 * Yuezejian  Created in 2020/11/1 下午10:55
 */
@Slf4j
@Component
public class PreRequestFilter extends AbstractPreZuulFilter{
    @Override
    protected Object cRun() {
        context.set("startTime" ,System.currentTimeMillis());
        return success();
    }

    /**
     * 最高优先级，进入前先记录时间戳
     * @return
     */
    @Override
    public int filterOrder() {
        return 0;
    }
}
