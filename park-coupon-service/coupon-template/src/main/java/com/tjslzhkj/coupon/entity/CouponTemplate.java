package com.tjslzhkj.coupon.entity;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tjslzhkj.coupon.converter.CouponCategoryConverter;
import com.tjslzhkj.coupon.converter.DistributeTargetConverter;
import com.tjslzhkj.coupon.converter.ProductLineConverter;
import com.tjslzhkj.coupon.converter.RuleConverter;
import com.tjslzhkj.coupon.serialization.CouponTemplateSerialize;
import com.tjslzhkj.coupon.constant.CouponCategory;
import com.tjslzhkj.coupon.constant.DistributeTarget;
import com.tjslzhkj.coupon.constant.ProductLine;
import com.tjslzhkj.coupon.vo.TemplateRule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 优惠券模板实体类： 基础属性 + 规则属性
 * @JsonSerialize(using = CouponTemplateSerialize.class) 自定义序列化器
 *  @CreatedDate jap自动填充当前时间
 * **
 * Yuezejian  Created in 2020/11/10 下午8:14
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "coupon_template")
@JsonSerialize(using = CouponTemplateSerialize.class)
public class CouponTemplate implements Serializable {
    /**
     * 自增主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * 是否可用
     */
    @Column(name = "available", nullable = false)
    private Boolean available;

    /**
     * 是否过期
     */
    @Column(name = "expired", nullable = false)
    private Boolean expired;

    /**
     * 优惠券名称
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 优惠券logo
     */
    @Column(name = "logo", nullable = false)
    private String logo;

    /**
     * 优惠券描述
     */
    @Column(name = "description", nullable = false)
    private String description;


    /**
     * 优惠券分类
     */
    @Column(name = "category", nullable = false)
    @Convert(converter = CouponCategoryConverter.class)
    private CouponCategory category;

    /**
     * 产品线
     */
    @Column(name = "product_line", nullable = false)
    @Convert(converter = ProductLineConverter.class)
    private ProductLine productLine;

    /**
     * 数量
     */
    @Column(name = "coupon_count", nullable = false)
    private Integer couponCount;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "create_time", nullable = false)
    private Date createTime;

    /**
     * 创建用户
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** 优惠券模板的编码 */
    @Column(name = "template_key", nullable = false)
    private String key;

    /** 目标用户 */
    @Column(name = "target", nullable = false)
    @Convert(converter = DistributeTargetConverter.class)
    private DistributeTarget target;

    /** 优惠券规则 */
    @Column(name = "rule", nullable = false)
    @Convert(converter = RuleConverter.class)
    private TemplateRule rule;

    /**
     * <h2>自定义构造函数</h2>
     * */
    public CouponTemplate(String name, String logo, String desc, String category,
                          Integer productLine, Integer count, Long userId,
                          Integer target, TemplateRule rule) {

        this.available = false;
        this.expired = false;
        this.name = name;
        this.logo = logo;
        this.description = desc;
        this.category = CouponCategory.of(category);
        this.productLine = ProductLine.of(productLine);
        this.couponCount = count;
        this.userId = userId;
        // 优惠券模板唯一编码 = 4(产品线和类型) + 8(日期: 20190101) + id(扩充为4位)
        this.key = productLine.toString() + category +
                new SimpleDateFormat("yyyyMMdd").format(new Date());
        this.target = DistributeTarget.of(target);
        this.rule = rule;
    }


}
