package com.tjslzhkj.coupon.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

/**
 * 自定义过滤器抽象基础类
 * **
 * Yuezejian  Created in 2020/10/27 下午11:07
 */
public abstract class AbstractZuulFilter extends ZuulFilter {

    // 用于在过滤器之间传递消息，数据保存在每个 ThreadLocal 中
    //上下文，extends ConcurrentHashMap<String, Object>
    RequestContext context;

    public static final String NEXT = "next";

    /**
     * a "true" return from this method means that the run() method should be invoked
     *
     * @return true if the run() method should be invoked. false will not invoke the run() method
     */
    @Override
    public boolean shouldFilter() {

        RequestContext ctx = RequestContext.getCurrentContext();
        return (boolean) ctx.getOrDefault(NEXT, true);
    }

    /**
     * if shouldFilter() is true, this method will be invoked. this method is the core method of a ZuulFilter
     *
     * @return Some arbitrary artifact may be returned. Current implementation ignores it.
     * @throws ZuulException if an error occurs during execution.
     */
    @Override
    public Object run() throws ZuulException {
        context = RequestContext.getCurrentContext();
        return cRun();
    }

    /**
     * 不希望在 run() 方法中有具体的执行细节，所以返回一个相同类型的抽象
     */
    protected abstract Object cRun();

    Object fail(int code,String msg) {
        context.set(NEXT,false);
        context.setSendZuulResponse(false);
        context.getResponse().setContentType("text/html;charset=UTF-8");
        context.setResponseStatusCode(code);
        context.setResponseBody(String.format("{\"result\":\"%s\"}",msg));
        return null;
    }

    Object success() {
        context.set(NEXT,true);
        return null;
    }
}
