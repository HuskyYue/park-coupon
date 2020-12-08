package com.tjslzhkj.coupon.advice;

import com.tjslzhkj.coupon.exception.CouponException;
import com.tjslzhkj.coupon.vo.BaseResponse;
import com.tjslzhkj.coupon.vo.StatusCode;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理
 * **
 * Yuezejian  Created in 2020/11/9 下午8:18
 */
@RestControllerAdvice
public class GlobalExceptionAdvice {

    /**
     * 对 CouponException 进行统一处理
     */
    @ExceptionHandler(value = CouponException.class)
    public BaseResponse<String> handlerCouponException(HttpServletRequest req, CouponException e) {
        BaseResponse<String> response = new BaseResponse<>(StatusCode.Fail);
        response.setData(e.getMessage());
        return response;
    }
}
