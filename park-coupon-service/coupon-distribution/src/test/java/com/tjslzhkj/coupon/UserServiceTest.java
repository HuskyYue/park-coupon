package com.tjslzhkj.coupon;

import com.alibaba.fastjson.JSON;
import com.tjslzhkj.coupon.constant.CouponStatus;
import com.tjslzhkj.coupon.entity.Coupon;
import com.tjslzhkj.coupon.exception.CouponException;
import com.tjslzhkj.coupon.service.IUserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <h1>用户服务功能测试用例</h1>
 * **
 * Yuezejian  Created in 2020/12/13 上午10:14
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class UserServiceTest {

    private Long fakeUserId = 20001L;

    @Autowired
    private IUserService userService;

    @Test
    public void testFindCouponByStatus() throws CouponException {
        System.out.println(JSON.toJSONString(
                userService.findCouponsByStatus(
                        fakeUserId,
                        CouponStatus.USABLE.getCode()
                )
        ));
        System.out.println(JSON.toJSONString(
                userService.findCouponsByStatus(
                        fakeUserId,
                        CouponStatus.USED.getCode()
                )
        ));
        System.out.println(JSON.toJSONString(
                userService.findCouponsByStatus(
                        fakeUserId,
                        CouponStatus.EXPIRED.getCode()
                )
        ));
    }

    @Test
    public void testFindAvailableTemplate() throws CouponException {

        System.out.println(JSON.toJSONString(
                userService.findAvailableTemplate(fakeUserId)
        ));
    }

}
