package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* z_agent_withdraw
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Data
@Schema(description = "z_agent_withdraw")
public class ZAgentWithdrawDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    @Schema(description = "代理")
    private Long agentId;
    @Schema(description = "代理")
    private String agentName;
    @Schema(description = "对公户ID")
    private Long basketId;
    @Schema(description = "对公户名称")
    private Long basketUser;
    @Schema(description = "对公户账号")
    private Long basketNo;
    @Schema(description = "提现金额")
    private BigDecimal amount;
    @Schema(description = "提现户名")
    private String accountUser;
    @Schema(description = "提现账号")
    private String accountNo;
    @Schema(description = "提现银行")
    private String accountBank;
    @Schema(description = "提现ifsc")
    private String accountIfsc;
    @Schema(description = "状态")
    private Integer processStatus;
    @Schema(description = "utr")
    private String utr;
    @Schema(description = "图片")
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