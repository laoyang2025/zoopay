package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* z_user_charge
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Data
@Schema(description = "z_user_charge")
public class ZUserChargeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    @Schema(description = "代理ID")
    private Long agentId;
    @Schema(description = "代理名称")
    private String agentName;
    @Schema(description = "卡主ID")
    private Long userId;
    @Schema(description = "卡主名称")
    private String username;
    @Schema(description = "充值金额")
    private BigDecimal amount;
    @Schema(description = "充值点位")
    private BigDecimal chargeRate;
    @Schema(description = "收入")
    private BigDecimal fee;
    @Schema(description = "分配类型")
    private String assignType;
    @Schema(description = "公户ID")
    private Long basketId;
    @Schema(description = "代付ID")
    private Long withdrawId;
    @Schema(description = "账户名")
    private String accountUser;
    @Schema(description = "账号")
    private String accountNo;
    @Schema(description = "银行")
    private String accountBank;
    @Schema(description = "IFSC")
    private String accountIfsc;
    @Schema(description = "状态")
    private Integer processStatus;
    @Schema(description = "UTR")
    private String utr;
    @Schema(description = "凭证")
    private String pictures;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}