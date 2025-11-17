package io.renren.bledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* 充值
*
* @author kernel kernel@qq.com
* @since 1.0 2024-06-02
*/
@Data
@Schema(description = "充值")
public class BotChargeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "用户ID")
    private Long userId;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "法币金额")
    private Long amount;
    @Schema(description = "手续费")
    private Long fee;
    @Schema(description = "手续费率")
    private BigDecimal feeRate;
    @Schema(description = "删除标识  0：未删除    1：删除")
    private Integer delFlag;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}