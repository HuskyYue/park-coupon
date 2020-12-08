package com.tjslzhkj.coupon.advice;

import com.tjslzhkj.coupon.annotation.IgnoreResponseAdvice;
import com.tjslzhkj.coupon.vo.BaseResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 统一响应模型的增强（应对多变的特殊返回情况)）
 * **
 * Yuezejian  Created in 2020/11/5 下午9:55
 */
@RestControllerAdvice
public class BaseResponseDataAdvice implements ResponseBodyAdvice<Object> {

    /**
     * 判断是否需要对响应进行处理
     * @param methodParameter 当前controller的声明方法
     * @param aClass
     * @return 返回 true 需要对响应进行处理
     */
    @Override
    @SuppressWarnings("all")
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        //TODO: if currency class marked the @IgnoreResponseAdvice, no handler
        if (methodParameter.getDeclaringClass().isAnnotationPresent(
                IgnoreResponseAdvice.class
        )) {
            return false;
        }

        //TODO: if currency method marked the @IgnoreResponseAdvice, no handler
        if (methodParameter.getMethod().isAnnotationPresent(
                IgnoreResponseAdvice.class
        )) {
            return false;
        }
        //TODO: 对响应进行处理，执行 beforeBodyWrite 方法
        return true;
    }

    /**
     * 如果 supports 返回 true
     * 在 Body 写入响应流之前，进行处理
     * @param o controller 的返回对象
     * @param methodParameter controller 的声明方法
     * @param mediaType
     * @param aClass
     * @param serverHttpRequest
     * @param serverHttpResponse
     * @return
     */
    @Override
    @SuppressWarnings("all")
    public Object beforeBodyWrite(Object o, MethodParameter methodParameter, MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass, ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse) {
        //TODO: 定义最终的返回对象
        BaseResponse<Object> response = new BaseResponse<>(
                0,""
        );
            // if Object o is null, resopnse won't need to set data
        if ( o == null ) {
            return response;
            // if Object o has been BaseResponse, no handler
        } else if ( o instanceof BaseResponse) {
            response = (BaseResponse<Object>) o;
        } else {
            // overthen , set Object o as the data of response
            response.setData(o);
        }
        return response;
    }
}
