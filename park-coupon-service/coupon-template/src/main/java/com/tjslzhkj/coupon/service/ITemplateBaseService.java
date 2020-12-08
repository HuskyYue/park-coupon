package com.tjslzhkj.coupon.service;

import com.tjslzhkj.coupon.entity.CouponTemplate;
import com.tjslzhkj.coupon.exception.CouponException;
import com.tjslzhkj.coupon.vo.CouponTemplateSDK;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 优惠券模板基础（view, delete...）服务定义
 * **
 * Yuezejian  Created in 2020/11/11 下午8:53
 */
@Service
@SuppressWarnings("all")
public interface ITemplateBaseService {
    /**
     * 根据优惠券模板 id 获取优惠券模板信息
     *
     * @param id 模板 id
     * @return {@link CouponTemplate} 优惠券模板实体
     * */
    CouponTemplate buildTemplateInfo(Integer id) throws CouponException;

    /**
     * 查找所有可用的优惠券模板
     *
     * @return {@link CouponTemplateSDK}s
     * */
    List<CouponTemplateSDK> findAllUsableTemplate();

    /**
     * 获取模板 ids 到 CouponTemplateSDK 的映射
     *
     * @param ids 模板 ids
     * @return Map<key: 模板 id， value: CouponTemplateSDK>
     * */
    Map<Integer, CouponTemplateSDK> findIds2TemplateSDK(Collection<Integer> ids);
}
