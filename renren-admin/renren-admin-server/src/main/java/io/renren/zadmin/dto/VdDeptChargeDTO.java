package io.renren.zadmin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* VIEW
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-09-10
*/
@Data
@Schema(description = "VIEW")
public class VdDeptChargeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "成功金额")
    private BigDecimal successAmount;
    @Schema(description = "手续费")
    private BigDecimal merchantFee;
    @Schema(description = "成功笔数")
    private BigDecimal success;
    @Schema(description = "失败笔数")
    private BigDecimal fail;
    @Schema(description = "成功率")
    private BigDecimal successRate;
    @Schema(description = "渠道成功")
    private BigDecimal channelCost;
    @Schema(description = "日期")
    private Date createDate;
    @Schema(description = "机构名称")
    private String deptName;
    @Schema(description = "机构id")
    private Long deptId;

}