package io.renren.zadmin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

import io.renren.commons.mybatis.entity.BaseEntity;

/**
 * z_channel
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("z_channel")
public class ZChannelEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    // 业务字段
    private String deptName;
    private Integer chargeEnabled;
    private Integer withdrawEnabled;
    private String channelName;
    private String channelLabel;
    // fee
    private BigDecimal chargeRate;
    private BigDecimal withdrawRate;
    private BigDecimal withdrawFix;
    // balance info
    private String balanceMemo;
    private BigDecimal warningAmount;
    // config
    private String merchantId;
    private String payCode;
    // api URL
    private String chargeUrl;
    private String withdrawUrl;
    private String chargeQueryUrl;
    private String withdrawQueryUrl;
    private String balanceUrl;
    // ext
    private String ext1;
    private String ext2;
    private String ext3;
    // security
    private String publicKey;
    private String privateKey;
    private String platformKey;
    private String whiteIp;
}