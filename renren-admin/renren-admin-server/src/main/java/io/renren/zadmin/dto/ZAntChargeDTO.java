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
public class ZAntChargeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    @Schema(description = "码农ID")
    private Long antId;
    @Schema(description = "码农")
    private Long antName;
    @Schema(description = "充值金额")
    private BigDecimal amount;
    @Schema(description = "充值方式")
    private String assignType;
    @Schema(description = "对公账户id")
    private Long basketId;
    @Schema(description = "提现ID")
    private Long withdrawId;
    @Schema(description = "UTR")
    private String utr;
    @Schema(description = "凭证")
    private String pictures;
    @Schema(description = "处理状态")
    private Integer processStatus;
    @Schema(description = "清算标志")
    private Integer settleFlag;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}