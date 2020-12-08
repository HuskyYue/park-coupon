package com.tjslzhkj.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.tjslzhkj.coupon.constant.Constant;
import com.tjslzhkj.coupon.constant.CouponStatus;
import com.tjslzhkj.coupon.dao.CouponDao;
import com.tjslzhkj.coupon.entity.Coupon;
import com.tjslzhkj.coupon.service.IKafkaService;
import com.tjslzhkj.coupon.vo.CouponKafkaMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Kafka 相关的服务接口实现
 * @Component spring 容器扫描到之后，再去通知 kafka
 * **
 * Yuezejian  Created in 2020/11/30 下午9:40
 */
@Slf4j
@Component
public class KafkaServiceImpl implements IKafkaService {

    @Autowired
    CouponDao couponDao;

    /**
     * 消费优惠券 Kafka 消息
     * @param record {@link ConsumerRecord}
     */
    @Override
    @KafkaListener(topics = {Constant.TOPIC}, groupId = "park-coupon-1")
    public void consumeCouponKafkaMessage(ConsumerRecord<?, ?> record) {
        Optional<?> kafkaMessage = Optional.ofNullable(record.value());
        if (kafkaMessage.isPresent()) {
            Object message = kafkaMessage.get();
            CouponKafkaMessage couponInfo = JSON.parseObject(
                    message.toString(),CouponKafkaMessage.class
            );
            log.info("Receive CouponKafkaMessage: {}", message.toString());
            CouponStatus status = CouponStatus.of(couponInfo.getStatus());

            switch (status) {
                case USABLE:
                    break;
                case USED:
                    processUsedCoupons(couponInfo, status);
                    break;
                case EXPIRED:
                    processExpiredCoupons(couponInfo, status);
                    break;
            }
        }
    }

    /**
     * 处理已使用的用户优惠券
     * @param kafkaMessage {@link CouponKafkaMessage}
     * @param status {@link CouponStatus}
     */
    private void processUsedCoupons(CouponKafkaMessage kafkaMessage,
                                    CouponStatus status) {
        //TODO: 优惠力度大的优惠券，用户才会使用，此时可以给用户发送短信
        processCouponsByStatus(kafkaMessage,status);

    }

    /**
     * 处理已过期的用户优惠券
     * @param kafkaMessage {@link CouponKafkaMessage}
     * @param status {@link CouponStatus}
     */
    private void processExpiredCoupons(CouponKafkaMessage kafkaMessage,
                                    CouponStatus status) {
        //TODO: 用户忘记了或优惠力度不是那么大，给用户发送推送
        processCouponsByStatus(kafkaMessage,status);

    }

    /**
     *  根据状态处理优惠券信息
     *  @param kafkaMessage {@link CouponKafkaMessage}
     *  @param status {@link CouponStatus}
     */
    private void processCouponsByStatus(
            CouponKafkaMessage kafkaMessage,
            CouponStatus status
    ) {
        List<Coupon> coupons = couponDao.findAllById(
                kafkaMessage.getIds()
        );
        if (CollectionUtils.isEmpty(coupons)
                || coupons.size() != kafkaMessage.getIds().size()) {
            log.error("Can Not Find Right Coupon Info: {}",
                    JSON.toJSONString(kafkaMessage));
            // TODO: 发送邮件
            return;
        }
        coupons.forEach(c -> c.setStatus(status));
        log.info("CouponKafkaMessage Op Coupon Count: {}",
                couponDao.saveAll(coupons).size());
    }
}
