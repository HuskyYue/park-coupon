package com.tjslzhkj.coupon.service;

import com.alibaba.fastjson.JSON;
import com.tjslzhkj.coupon.exception.CouponException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * 优惠券模板基础服务的测试
 * **
 * Yuezejian  Created in 2020/11/24 下午11:33
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class TemplateBaseTest {


    @Autowired
    private ITemplateBaseService baseService;

    @Test
    public void testBuildTemplateInfo() throws CouponException {
        System.out.println(JSON.toJSONString(baseService.buildTemplateInfo(12)));
        System.out.println(JSON.toJSONString(baseService.buildTemplateInfo(13)));
    }

    @Test
    public void testFindAllUnableTemplate() {
        System.out.println(JSON.toJSONString(baseService.findAllUsableTemplate()));
    }


}
