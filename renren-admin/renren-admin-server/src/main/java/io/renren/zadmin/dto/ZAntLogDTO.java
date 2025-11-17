package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* z_ant_log
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Data
@Schema(description = "z_ant_log")
public class ZAntLogDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    /////////////////////////////////////////////////////
    @Schema(description = "码农ID")
    private Long antId;
    @Schema(description = "码农名称")
    private String antName;
    /////////////////////////////////////////////////////
    @Schema(description = "卡ID")
    private Long cardId;
    @Schema(description = "卡户名")
    private String cardUser;
    @Schema(description = "卡号")
    private String cardNo;
    /////////////////////////////////////////////////////
    @Schema(description = "UTR")
    private String utr;
    @Schema(description = "流水描述")
    private String narration;
    @Schema(description = "流水金额")
    private BigDecimal amount;
    @Schema(description = "余额")
    private BigDecimal balance;
    @Schema(description = "记账方向")
    private String flag;
    @Schema(description = "TN")
    private String tn;
    /////////////////////////////////////////////////////
    @Schema(description = "匹配收款ID")
    private Long chargeId;
    @Schema(description = "失败次数")
    private Integer failCount;
    /////////////////////////////////////////////////////
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;
}