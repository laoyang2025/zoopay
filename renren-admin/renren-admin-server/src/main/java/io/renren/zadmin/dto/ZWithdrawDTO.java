package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* z_withdraw
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Data
@Schema(description = "z_withdraw")
public class ZWithdrawDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;
    @Schema(description = "ID")
    private Long id;
    @Schema(description = "机构id")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;

    // 商户请求
    @Schema(description = "商户ID")
    private Long merchantId;
    @Schema(description = "商户名")
    private String merchantName;
    @Schema(description = "商户单号")
    private String orderId;
    @Schema(description = "订单金额")
    private BigDecimal amount;
    @Schema(description = "通知地址")
    private String notifyUrl;
    @Schema(description = "IFSC")
    private String accountIfsc;
    @Schema(description = "银行名称")
    private String accountBank;
    @Schema(description = "账户User")
    private String accountUser;
    @Schema(description = "账号")
    private String accountNo;
    @Schema(description = "提现费率")
    private BigDecimal merchantRate;
    @Schema(description = "提现费率定额")
    private BigDecimal merchantFix;
    @Schema(description = "提现手续费")
    private BigDecimal merchantFee;
    @Schema(description = "提现成本")
    private BigDecimal cost; // 代付只有渠道有成本
    @Schema(description = "提现IP")
    private String ip;
    @Schema(description = "备注")
    private String memo;

    // 凭证与匹配
    @Schema(description = "UTR")
    private String utr;
    @Schema(description = "UPI")
    private String upi;
    @Schema(description = "凭证")
    private String pictures;

    // 状态与处理
    @Schema(description = "处理状态")
    private Integer processStatus;
    @Schema(description = "通知状态")
    private Integer notifyStatus;
    @Schema(description = "通知次数")
    private Integer notifyCount;
    @Schema(description = "通知时间")
    private Date notifyTime;
    @Schema(description = "抢充")
    private Integer claimed;
    @Schema(description = "流水ID")
    private Long logId;  // 银行流水
    @Schema(description = "处理模式")
    private String handleMode;

    // 渠道模式
    @Schema(description = "渠道ID")
    private Long channelId;
    @Schema(description = "渠道")
    private String channelLabel;
    @Schema(description = "渠道单号")
    private String channelOrder;
    @Schema(description = "渠道扣率")
    private BigDecimal channelCostRate;
    @Schema(description = "渠道定额")
    private BigDecimal channelCostFix;

    // 自营卡
    @Schema(description = "卡ID")
    private Long cardId;
    @Schema(description = "卡账户")
    private String cardUser;
    @Schema(description = "卡号")
    private String cardNo;

    // 码农跑分
    @Schema(description = "码农ID")
    private Long antId;
    @Schema(description = "码农")
    private String antName;

    // 代理跑分
    @Schema(description = "代理ID")
    private Long agentId;
    @Schema(description = "代理")
    private String agentName;
    @Schema(description = "卡主ID")
    private Long userId;
    @Schema(description = "卡主")
    private String username;

    //  拓展方
    @Schema(description = "拓展方")
    private Long middleId;
    @Schema(description = "拓展方")
    private String middleName;

}