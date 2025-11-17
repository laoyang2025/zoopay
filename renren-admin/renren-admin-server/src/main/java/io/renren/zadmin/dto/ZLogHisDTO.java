package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* z_log
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Data
@Schema(description = "z_log")
public class ZLogHisDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    @Schema(description = "机构id")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    @Schema(description = "余额ID")
    private Long balanceId;
    @Schema(description = "账户类型")
    private String ownerType;
    @Schema(description = "账户id")
    private Long ownerId;
    @Schema(description = "账户名称")
    private String ownerName;
    @Schema(description = "事实ID")
    private Long factId;
    @Schema(description = "事实类型")
    private Integer factType;
    @Schema(description = "事实金额")
    private BigDecimal factAmount;
    @Schema(description = "事实说明")
    private String factMemo;
    @Schema(description = "旧余额")
    private BigDecimal oldBalance;
    @Schema(description = "新余额")
    private BigDecimal newBalance;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}