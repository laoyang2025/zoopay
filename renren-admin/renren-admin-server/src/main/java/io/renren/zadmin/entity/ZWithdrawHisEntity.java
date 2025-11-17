package io.renren.zadmin.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.renren.commons.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * z_withdraw
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("z_withdraw_his")
public class ZWithdrawHisEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;
    private String deptName;

    // 商户请求(15)
    private Long merchantId;
    private String merchantName;
    private String orderId;
    private BigDecimal amount;
    private String notifyUrl;
    private String accountIfsc;
    private String accountBank;
    private String accountUser;
    private String accountNo;
    private BigDecimal merchantRate;
    private BigDecimal merchantFix;
    private BigDecimal merchantFee;
    private BigDecimal cost; // 代付只有渠道有成本
    private String ip;
    private String memo;

    // 凭证与匹配(3)
    private String utr;
    private String upi;
    private String pictures;

    // 状态与处理(7)
    private Integer processStatus;
    private Integer notifyStatus;
    private Integer notifyCount;
    private Date notifyTime;
    private Integer claimed;
    private Long logId;  // 银行流水
    private String handleMode;

    // 渠道模式(5)
    private Long channelId;
    private String channelLabel;
    private String channelOrder;
    private BigDecimal channelCostRate;
    private BigDecimal channelCostFix;

    // 自营卡(3)
    private Long cardId;
    private String cardUser;
    private String cardNo;

    // 码农跑分(2)
    private Long antId;
    private String antName;

    // 代理跑分(4)
    private Long agentId;
    private String agentName;
    private Long userId;
    private String username;

}