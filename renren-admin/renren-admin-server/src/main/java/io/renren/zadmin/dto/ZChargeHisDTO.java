package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* z_charge
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Data
@Schema(description = "z_charge")
public class ZChargeHisDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    @Schema(description = "机构id")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

    // 商户请求信息
    @Schema(description = "商户ID")
    private Long merchantId;
    @Schema(description = "商户名称")
    private String merchantName;
    @Schema(description = "支付编码")
    private String payCode;
    @Schema(description = "收款扣率")
    private BigDecimal merchantRate;
    @Schema(description = "商户单号")
    private String orderId;
    @Schema(description = "充值金额")
    private BigDecimal amount;
    @Schema(description = "实际金额")
    private BigDecimal realAmount;
    @Schema(description = "商户本金")
    private BigDecimal merchantPrincipal;
    @Schema(description = "手续费")
    private BigDecimal merchantFee;
    @Schema(description = "跳转URL")
    private String callbackUrl;
    @Schema(description = "通知URL")
    private String notifyUrl;
    @Schema(description = "充值IP")
    private String ip;


    // 匹配与凭证
    @Schema(description = "UTR")
    private String utr;
    @Schema(description = "TN")
    private String tn;
    @Schema(description = "UPI")
    private String upi;
    @Schema(description = "凭证图片")
    private String pictures;


    // 处理状态
    @Schema(description = "状态")
    private Integer processStatus;
    @Schema(description = "通知时间")
    private Date notifyTime;
    @Schema(description = "通知状态")
    private Integer notifyStatus;
    @Schema(description = "通知次数")
    private Integer notifyCount;
    @Schema(description = "清算标志")
    private Integer settleFlag;
    @Schema(description = "处理模式")
    private String handleMode;

    // 自营卡模式
    @Schema(description = "自营卡ID")
    private Long cardId;
    @Schema(description = "自营卡用户")
    private String cardUser;
    @Schema(description = "自营卡号")
    private String cardNo;


    // 渠道处理模式下
    @Schema(description = "渠道")
    private String channelLabel;
    @Schema(description = "渠道id")
    private Long channelId;
    @Schema(description = "渠道成本扣率")
    private BigDecimal channelRate;
    @Schema(description = "渠道成本")
    private BigDecimal channelCost;
    @Schema(description = "渠道单号")
    private String channelOrder;
    @Schema(description = "代理id")

    // 代理跑分模式下
    private Long agentId;
    @Schema(description = "代理名称")
    private String agentName;
    @Schema(description = "代理扣率")
    private BigDecimal agentRate;
    @Schema(description = "出借额度")
    private BigDecimal agentShare;
    @Schema(description = "卡主id")
    private Long userId;
    @Schema(description = "卡主名称")
    private String username;
    @Schema(description = "卡主卡id")
    private Long userCardId;
    @Schema(description = "码农卡户名")
    private String userCardUser;
    @Schema(description = "码农卡ID")
    private String userCardNo;
    @Schema(description = "码农id")

    // 码农跑分模式下: 9
    private Long antId;
    @Schema(description = "码农名")
    private String antName;
    @Schema(description = "码农卡id")
    private Long antCardId;
    @Schema(description = "码农卡户名")
    private String antCardUser;
    @Schema(description = "码农卡号")
    private String antCardNo;
    @Schema(description = "码农p1")
    private Long antP1Id;
    @Schema(description = "码农p2")
    private Long antP2Id;
    @Schema(description = "码农p1rate")
    private BigDecimal antP1Rate;
    @Schema(description = "码农p2rate")
    private BigDecimal antP2Rate;

    //  拓展方
    @Schema(description = "拓展方")
    private Long middleId;
    @Schema(description = "拓展方")
    private String middleName;

}