package com.tjslzhkj.coupon.filter;

import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;

/**
 * 表单 Post 请求拦截器抽象基类
 * **
 * Yuezejian  Created in 2020/11/1 下午11:17
 */
public abstract class AbstractPostZuulFilter extends AbstractZuulFilter{
    @Override
    public String filterType() {
        return FilterConstants.POST_TYPE;
    }
}
