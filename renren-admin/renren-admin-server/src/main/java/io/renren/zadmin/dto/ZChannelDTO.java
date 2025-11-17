package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* z_channel
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Data
@Schema(description = "z_channel")
public class ZChannelDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    @Schema(description = "收款启用")
    private Integer chargeEnabled;
    @Schema(description = "代付启用")
    private Integer withdrawEnabled;
    @Schema(description = "渠道名")
    private String channelName;
    @Schema(description = "展示名")
    private String channelLabel;
    @Schema(description = "充值扣率")
    private BigDecimal chargeRate;
    @Schema(description = "提现扣率")
    private BigDecimal withdrawRate;
    @Schema(description = "提现定额")
    private BigDecimal withdrawFix;
    @Schema(description = "接入商户号")
    private String merchantId;
    @Schema(description = "通道编码")
    private String payCode;
    @Schema(description = "余额详情")
    private String balanceMemo;
    @Schema(description = "告警金额")
    private BigDecimal warningAmount;
    @Schema(description = "收款地址")
    private String chargeUrl;
    @Schema(description = "代付地址")
    private String withdrawUrl;
    @Schema(description = "收款查询")
    private String chargeQueryUrl;
    @Schema(description = "代付查询")
    private String withdrawQueryUrl;
    @Schema(description = "余额查询")
    private String balanceUrl;
    private String ext1;
    private String ext2;
    private String ext3;
    @Schema(description = "公钥")
    private String publicKey;
    @Schema(description = "私钥 Or md5密钥")
    private String privateKey;
    @Schema(description = "平台公钥")
    private String platformKey;
    @Schema(description = "白名单")
    private String whiteIp;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}