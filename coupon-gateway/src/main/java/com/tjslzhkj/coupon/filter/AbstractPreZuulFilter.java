package com.tjslzhkj.coupon.filter;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * 前置拦截器抽象类
 */
public abstract class AbstractPreZuulFilter extends AbstractZuulFilter{
    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;
    }
}
