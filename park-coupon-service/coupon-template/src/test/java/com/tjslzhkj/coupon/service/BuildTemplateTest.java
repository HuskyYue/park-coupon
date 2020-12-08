package com.tjslzhkj.coupon.service;

import com.alibaba.fastjson.JSON;
import com.tjslzhkj.coupon.vo.TemplateRequest;
import com.tjslzhkj.coupon.constant.CouponCategory;
import com.tjslzhkj.coupon.constant.DistributeTarget;
import com.tjslzhkj.coupon.constant.PeriodType;
import com.tjslzhkj.coupon.constant.ProductLine;
import com.tjslzhkj.coupon.vo.TemplateRule;
import org.apache.commons.lang.time.DateUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * 构造优惠券模板服务测试
 * **
 * Yuezejian  Created in 2020/11/19 下午11:47
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class BuildTemplateTest {

    @Autowired
    private IBuildTemplateService buildTemplateService;

    @Test
    public void testBuildTemplate() throws Exception {
        System.out.println(JSON.toJSONString(buildTemplateService.buildTemplate(fakeTemplateRequest())));
        Thread.sleep(5000);
    }

    private TemplateRequest fakeTemplateRequest() {
        TemplateRequest request = new TemplateRequest();
        request.setName("优惠券模板-" + new Date().getTime());
        request.setLogo("http://www.park.com");
        request.setDesc("生态城智慧停车场优惠券模板");
        request.setCategory(CouponCategory.MANJIAN.getCode());
        request.setProductLine(ProductLine.DONGTINGPARK.getCode());
        request.setCount(10000);
        request.setUserId(10000L);
        request.setTarget(DistributeTarget.SINGLE.getCode());
        TemplateRule rule = new TemplateRule();
        rule.setExpiration(new TemplateRule.Expiration(PeriodType.SHIFT.getCode(),1,
                DateUtils.addDays(new Date(),60).getTime()));
        rule.setDiscount(new TemplateRule.Discount(5,1));
        rule.setLimitation(1);
        rule.setUsage(new TemplateRule.Usage("天津市","滨海新区",JSON.toJSONString(Arrays.asList("公共交通","运输服务"))));
        rule.setWeight(JSON.toJSONString(Collections.EMPTY_LIST));
        request.setRule(rule);
        return request;
    }
}
