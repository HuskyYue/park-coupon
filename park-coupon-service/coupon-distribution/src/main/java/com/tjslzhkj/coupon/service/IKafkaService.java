package com.tjslzhkj.coupon.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * Kafka 相关的服务接口定义
 * **
 * Yuezejian  Created in 2020/11/27 下午7:43
 */
public interface IKafkaService {

    /**
     * 消费优惠券 Kafka 消息
     * @param record
     */
    void consumeCouponKafkaMessage(ConsumerRecord<?,?> record);
}
