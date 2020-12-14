package com.tjslzhkj.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * <h1>优惠券结算微服务启动入口</h1>
 * **
 * Yuezejian  Created in 2020/12/13 下午9:11
 */
@EnableEurekaClient
@SpringBootApplication
public class SettlementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettlementApplication.class, args);
    }
}
