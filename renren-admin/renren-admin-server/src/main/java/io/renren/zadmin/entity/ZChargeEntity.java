package io.renren.zadmin.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

import io.renren.commons.mybatis.entity.BaseEntity;

/**
 * z_charge
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("z_charge")
public class ZChargeEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;
    private String deptName;

    // 商户请求信息(12)
    private Long merchantId;
    private String merchantName;
    private String payCode;
    private BigDecimal merchantRate;
    private String orderId;
    private BigDecimal amount;
    private BigDecimal realAmount;
    private BigDecimal merchantPrincipal;
    private BigDecimal merchantFee;
    private String callbackUrl;
    private String notifyUrl;
    private String memo;
    private String ip;

    // 匹配与凭证
    private String utr;
    private String tn;
    private String upi;
    private String pictures;

    // 处理状态
    private Integer processStatus;
    private Date notifyTime;
    private Integer notifyStatus;
    private Integer notifyCount;
    private Integer settleFlag;
    private String handleMode;

    // 自营卡模式
    private Long cardId;
    private String cardUser;
    private String cardNo;

    // 渠道处理模式下(5)
    private String channelLabel;
    private Long channelId;
    private BigDecimal channelRate;
    private BigDecimal channelCost;
    private String channelOrder;

    // 代理跑分模式下
    private Long agentId;
    private String agentName;
    private BigDecimal agentRate;
    private BigDecimal agentShare;
    private Long userId;
    private String username;
    private BigDecimal userRate;
    private Long userCardId;
    private String userCardUser;
    private String userCardNo;

    // 码农跑分模式下: 9
    private Long antId;
    private String antName;
    private Long antCardId;
    private String antCardUser;
    private String antCardNo;
    private Long antP1Id;
    private Long antP2Id;
    private BigDecimal antP1Rate;
    private BigDecimal antP2Rate;

    //  拓展方
    private Long middleId;
    private String middleName;

}