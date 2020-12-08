package com.tjslzhkj.coupon.converter;

import com.alibaba.fastjson.JSON;
import com.tjslzhkj.coupon.vo.TemplateRule;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * 优惠券模板规则属性转换器
 * **
 * Yuezejian  Created in 2020/11/10 下午9:02
 */
@Converter
public class RuleConverter implements AttributeConverter<TemplateRule,String> {
    @Override
    public String convertToDatabaseColumn(TemplateRule rule) {
        return JSON.toJSONString(rule);
    }

    @Override
    public TemplateRule convertToEntityAttribute(String rule) {
        return JSON.parseObject(rule, TemplateRule.class);
    }
}
