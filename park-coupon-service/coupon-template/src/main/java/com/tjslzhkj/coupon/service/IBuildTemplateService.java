package com.tjslzhkj.coupon.service;

import com.tjslzhkj.coupon.entity.CouponTemplate;
import com.tjslzhkj.coupon.vo.TemplateRequest;
import com.tjslzhkj.coupon.exception.CouponException;
import org.springframework.stereotype.Service;

/**
 * 构建优惠券模板接口定义
 * **
 * Yuezejian  Created in 2020/11/10 下午10:08
 */
@Service
public interface IBuildTemplateService {

    /**
     * <h2>创建优惠券模板</h2>
     * @param request {@link TemplateRequest} 模板信息请求对象
     * @return {@link CouponTemplate} 优惠券模板实体
     * */
    CouponTemplate buildTemplate(TemplateRequest request)
            throws CouponException;
}
