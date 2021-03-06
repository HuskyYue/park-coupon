package com.tjslzhkj.coupon.service.impl;

import com.alibaba.fastjson.JSON;
import com.tjslzhkj.coupon.Feign.SettlementClient;
import com.tjslzhkj.coupon.Feign.TemplateClient;
import com.tjslzhkj.coupon.constant.Constant;
import com.tjslzhkj.coupon.constant.CouponStatus;
import com.tjslzhkj.coupon.dao.CouponDao;
import com.tjslzhkj.coupon.entity.Coupon;
import com.tjslzhkj.coupon.exception.CouponException;
import com.tjslzhkj.coupon.service.IRedisService;
import com.tjslzhkj.coupon.service.IUserService;
import com.tjslzhkj.coupon.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tomcat.util.bcel.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <h1>用户相关的接口实现</h1>
 * 所有的操作过程，状态都保存在 Redis 中， 并通过 Kafka 把消息传递到 MySql中
 * 为什么用 Kafka, 而不用 SpringBoot 中的异步？ 保证安全性和一致性
 * **
 * Yuezejian  Created in 2020/12/6 上午7:57
 */
@Slf4j
@Service
@SuppressWarnings("all")
public class UserServiceImpl implements IUserService {

    /** Coupon Dao */
    private final CouponDao couponDao;

    /** Redis 服务 */
    private final IRedisService redisService;

    /** 模板微服务客户端 */
    private final TemplateClient templateClient;

    /** 结算微服务客户端 */
    private final SettlementClient settlementClient;

    /** Kafka 客户端 */
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    public UserServiceImpl(
            CouponDao couponDao,
            IRedisService redisService,
            TemplateClient templateClient,
            SettlementClient settlementClient,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.couponDao = couponDao;
        this.redisService = redisService;
        this.templateClient = templateClient;
        this.settlementClient = settlementClient;
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * <h2>根据用户 ID 和状态查询优惠券记录</h2>
     *
     * @param userId 用户 id
     * @param status 优惠券状态
     * @return {@link Coupon}
     * @throws CouponException
     */
    @Override
    public List<Coupon> findCouponsByStatus(Long userId, Integer status) throws CouponException {
        //从 redis 中查询优惠券信息，查不到时，saveEmptyCouponListToCache
        List<Coupon> curCached = redisService.getCacheCoupons(userId,status);
        //TODO: 如果缓存中有，直接返回；如果缓存中没有，查询数据库 -> 数据库中没有，直接返回null
        // 数据库中有，填充无效优惠券数据,新加入缓存，再返回；
        List<Coupon> preTarget;
        if (CollectionUtils.isNotEmpty(curCached)) {
            log.debug("Coupon Cache Is Not Empty: {}, {}", userId, status);
            preTarget = curCached;
        } else {
            log.debug("Coupon Cache Is Empty, Get Coupon From DB: {}, {}", userId, status);
            List<Coupon> dbCoupons = couponDao.findAllByUserIdAndStatus(
                    userId, CouponStatus.of(status)
            );
            //TODO: if no record in database, just return, one invalid coupon has been put into cache
            if (CollectionUtils.isEmpty(dbCoupons)) {
                log.debug("Current User Dot Have Coupon: {}, {}", userId, status);
                return dbCoupons;
            }
            //TODO: 对于缓存中没有，数据库中有的数据，需要重新放入缓存；需在此填充 dbCoupons 的 templateSDK 字段
            //
            Map<Integer, CouponTemplateSDK> id2TemplateSDK = templateClient.findIds2TemplateSDK(
                    dbCoupons.stream()
                            .map(Coupon::getTemplateId)
                            .collect(Collectors.toList())
            ).getData();
            dbCoupons.forEach(
                    dc -> dc.setTemplateSDK(
                            id2TemplateSDK.get(dc.getTemplateId())
                    )
            );
            // 数据库中存在记录
            preTarget = dbCoupons;
            // 将数据库记录写入 Cache
            redisService.addCouponToCache(userId,preTarget,status);
        }
        //将无效优惠券剔除(preTarget 中可能包含了缓存中放置的无效优惠券，所以需要剔除)
        preTarget = preTarget.stream()
                .filter( p -> p.getId() != -1)
                .collect(Collectors.toList());
        // TODO: 如果当前获取的是可用优惠券，还需要对已过期的优惠券做延迟处理
        if (CouponStatus.of(status) == CouponStatus.USABLE) {
            CouponClassify classify = CouponClassify.classify(preTarget);
            // 如果已过期的优惠券不为空，需要做延时处理
            if (CollectionUtils.isNotEmpty(classify.getExpired())) {
                log.info("Add Expired Coupon TO Cache From FindCouponsByStatus:{}, {}", userId, status);
                // 对已过期优惠券，需要重置缓存
                redisService.addCouponToCache(
                        userId
                        ,classify.getExpired(),
                        CouponStatus.EXPIRED.getCode());
            // TODO: 发送到 Kafka 中做异步处理
            kafkaTemplate.send(
                    Constant.TOPIC,
                    JSON.toJSONString(new CouponKafkaMessage(
                            CouponStatus.EXPIRED.getCode(),
                            classify.getExpired().stream()
                                    .map(Coupon::getId)
                                    .collect(Collectors.toList())
                    ))
            );
            }
            return classify.getUsable();
        }
        return preTarget;
    }

    /**
     * <h2>根据用户 ID 查找当前可以领取的优惠券模板</h2>
     *
     * @param userId 用户 ID
     * @return {@link CouponTemplateSDK}
     * @throws CouponException
     */
    @Override
    public List<CouponTemplateSDK> findAvailableTemplate(Long userId) throws CouponException {
        long curTime = new Date().getTime();
        //TODO: 查出全部可用的优惠券模板
        List<CouponTemplateSDK> templateSDKS = templateClient.findAllUsableTemplate().getData();
        log.debug("Find All Template(From TemplateClient) Count: {}",
                templateSDKS.size());
        //TODO: 过滤过期的优惠券模板
        templateSDKS = templateSDKS.stream().filter(
                t -> t.getRule().getExpiration().getDeadline() > curTime
        ).collect(Collectors.toList());
        log.info("Find Usable Template Count: {}", templateSDKS.size());
        //TODO: 通过用户id,显示可见的优惠券模板。当领取次数超过 limitation 时，设置模板为不可用。
        //key 是 TemplateId
        //Pair 中的 left 是 Template limitation, right 是优惠券模板
        Map<Integer, Pair<Integer, CouponTemplateSDK>> limit2Template =
                new HashMap<>(templateSDKS.size());
        templateSDKS.forEach(
                t -> limit2Template.put(
                        t.getId(),
                        Pair.of(t.getRule().getLimitation(),t)
                )
        );
        //TODO: result 即为用户可以领取的优惠券信息
        List<CouponTemplateSDK> result = new ArrayList<>(limit2Template.size());
        //TODO: 获取到用户可用的优惠券记录
        List<Coupon> userUsableCoupons = findCouponsByStatus(
                userId, CouponStatus.USABLE.getCode()
        );
        log.debug("Current User Has Usable Coupons: {}, {}", userId,
                userUsableCoupons.size());
        // 通过模板 id , 获取可用优惠券
        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));
        //  根据 Template 的 Rule 判断是否可以领取优惠券模板
        //TODO: 比较用户领取记录中，该优惠券模板已经被领取的优惠券数量，是否超过了优惠券领取数量限制
        limit2Template.forEach((k, v) -> {
            int limitation = v.getLeft();
            CouponTemplateSDK templateSDK = v.getRight();
            if (templateId2Coupons.containsKey(k)
                    && templateId2Coupons.get(k).size() >= limitation) {
                //超了限制，不允许领取
                return;
            }
            //将模板添加到返回结果中
            result.add(templateSDK);
        });


        return result;
    }

    /**
     * <h2>用户领取优惠券</h2>
     *
     * @param request {@link AcquireTemplateRequest}
     * @return {@link Coupon}
     * @throws CouponException
     */
    @Override
    public Coupon acquireTemplate(AcquireTemplateRequest request) throws CouponException {

        //TODO: 从模板微服务中获取当前用户可以领取的模板
        Map<Integer, CouponTemplateSDK> id2Template = templateClient.findIds2TemplateSDK(
                Collections.singletonList(
                        request.getTemplateSDK().getId())
        ).getData();

        //优惠券模板是必须存在的
        if (id2Template.size() <= 0) {
            log.error("Can Not Acquire Template From TemplateClient: {}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Template From TemplateClient");

        }

        //用户是否可以领取这张优惠券
        //TODO: 查询用户当前可用的优惠券
        List<Coupon> userUsableCoupons = findCouponsByStatus(
                request.getUserId(), CouponStatus.USABLE.getCode()
        );

        Map<Integer, List<Coupon>> templateId2Coupons = userUsableCoupons.stream()
                .collect(Collectors.groupingBy(Coupon::getTemplateId));

        //TODO: 判断当前可用的优惠券的模板是否依旧有效，当前领取数量是否在模板限制数量之内
        if (templateId2Coupons.containsKey(request.getTemplateSDK().getId())
                && templateId2Coupons.get(request.getTemplateSDK().getId()).size()
                >= request.getTemplateSDK().getRule().getLimitation() ) {
            log.error("Exceed Template Assign Limitation: {}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Exceed Template Assign Limitation");
        }
        //尝试获取优惠券码
        String couponCode = redisService.tryToAcquireCouponCodeFromCache(
                request.getTemplateSDK().getId()
        );
        if (StringUtils.isEmpty(couponCode)) {
            log.error("Can Not Acquire Coupon Code: {}",
                    request.getTemplateSDK().getId());
            throw new CouponException("Can Not Acquire Coupon Code");
        }

        Coupon newCoupon = new Coupon(
                request.getTemplateSDK().getId(), request.getUserId(),
                couponCode, CouponStatus.USABLE
        );
        newCoupon = couponDao.save(newCoupon);

        //// 填充 Coupon 对象的优惠券模板信息（CouponTemplateSDK）, 一定要在放入缓存之前去填充
        newCoupon.setTemplateSDK(request.getTemplateSDK());

        //放入 redis 缓存
        redisService.addCouponToCache(
                request.getUserId(),
                Collections.singletonList(newCoupon),
                CouponStatus.USABLE.getCode()
        );
        return newCoupon;
    }

    /**
     * <h2>结算（核销）优惠券</h2>
     * 这里需要注意,规则相关的处理需要由 Settlement 系统去做，当前系统仅需要做业务处理过程（检验过程）
     *
     * @param info {@link SettlementInfo}
     * @return {@link SettlementInfo}
     * @throws CouponException
     */
    @Override
    public SettlementInfo settlement(SettlementInfo info) throws CouponException {

        // 当没有传递优惠券时，直接返回商品总价
        List<SettlementInfo.CouponAndTemplateInfo> ctInfos = info.getCouponAndTemplateInfos();
        if (CollectionUtils.isEmpty(ctInfos)) {
            log.info("Empty Coupon For Settle.");
            double goodsSum = 0.0;
            for (GoodsInfo gi : info.getGoodsInfos()) {
                goodsSum += gi.getPrice() * gi.getCount();
            }
            // 没有优惠券也就不存在优惠券的核销, SettlementInfo 其他的字段不需要修改
            info.setCost(retain2Decimals(goodsSum));
        }
        // 校验传递的优惠券是否是用户自己的
        //TODO: 判断用于结算的优惠券是否为该用户可用优惠券的子集，
        // 是的话把优惠券信息提出来，放入 List<Coupon> 中
        List<Coupon> coupons = findCouponsByStatus(
                info.getUserId(),CouponStatus.USABLE.getCode()
        );
        Map<Integer, Coupon> id2Coupon = coupons.stream()
                .collect(Collectors.toMap(
                        Coupon::getId,
                        Function.identity()
                ));
        if (MapUtils.isEmpty(id2Coupon) || !CollectionUtils.isSubCollection(
                ctInfos.stream()
                        .map(SettlementInfo
                                .CouponAndTemplateInfo::getId)
                                .collect(Collectors.toList()),
                            id2Coupon.keySet()
        )) {
            log.info("{}", id2Coupon.keySet());
            log.info("{}", ctInfos.stream()
                    .map(SettlementInfo.CouponAndTemplateInfo::getId)
                    .collect(Collectors.toList()));
            log.error("User Coupon Has Some Problem, It Is Not SubCollection" +
                    "Of Coupons!");
            throw new CouponException("User Coupon Has Some Problem, " +
                    "It Is Not SubCollection Of Coupons!");
        }
        log.debug("Current Settlement Coupons Is User's: {}", ctInfos.size());

        List<Coupon> settleCoupons = new ArrayList<>(ctInfos.size());
        ctInfos.forEach(ci -> settleCoupons.add(id2Coupon.get(ci.getId())));

        //通过结算微服务获取结算信息
        SettlementInfo processedInfo =
                settlementClient.computeRule(info).getData();

        if (processedInfo.getEmploy() && CollectionUtils.isNotEmpty(
                processedInfo.getCouponAndTemplateInfos()
        )) {
            log.info("Settle User Coupon: {}, {}", info.getUserId(),
                    JSON.toJSONString(settleCoupons));
            //更新 db
            kafkaTemplate.send(
                    Constant.TOPIC,
                    JSON.toJSONString(new CouponKafkaMessage(
                            CouponStatus.USED.getCode(),
                            settleCoupons.stream().map(Coupon::getId).collect(Collectors.toList())
                    ))
            );
        }
        return processedInfo;
    }

    /**
     * <h2>保留两位小数</h2>
     * @param value
     * @return
     */
    private double retain2Decimals(double value) {
        // BigDecimal.ROUND_HALF_UP 代表四舍五入
        return new BigDecimal(value)
                .setScale(2, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
    }

}
