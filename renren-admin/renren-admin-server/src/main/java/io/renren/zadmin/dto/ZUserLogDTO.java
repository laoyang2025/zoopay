package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* z_user_log
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Data
@Schema(description = "z_user_log")
public class ZUserLogDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    //////////////////////////////////////////////////
    @Schema(description = "agentId")
    private Long agentId;
    @Schema(description = "代理")
    private String agentName;
    @Schema(description = "卡主ID")
    private Long userId;
    @Schema(description = "卡主")
    private String username;
    //////////////////////////////////////////////////
    @Schema(description = "卡ID")
    private Long cardId;
    @Schema(description = "卡户名")
    private String cardUser;
    @Schema(description = "卡号")
    private String cardNo;
    @Schema(description = "UTR")
    private String utr;
    @Schema(description = "TN")
    private String tn;
    @Schema(description = "流水描述")
    private String narration;
    @Schema(description = "流水金额")
    private BigDecimal amount;
    @Schema(description = "余额")
    private BigDecimal balance;
    @Schema(description = "记账方向")
    private String flag;
    //////////////////////////////////////////////////
    @Schema(description = "收款ID")
    private Long chargeId;
    @Schema(description = "匹配失败")
    private Integer failCount;
    //////////////////////////////////////////////////
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}