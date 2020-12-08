package com.tjslzhkj.coupon;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * 网关应用启动入口
 * **
 * @EnableZuulProxy 标识了当前应用是 Zuul Server
 * @SpringCloudApplication 组合了 SpringBoot应用 + 服务发现 + 熔断
 * Yuezejian  Created in 2020/10/27 下午10:52
 */
@EnableZuulProxy
@SpringCloudApplication
public class ZuulGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZuulGatewayApplication.class,args);
    }
}
