package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* z_withdraw
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Data
@Schema(description = "z_withdraw")
public class ZBalanceDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    @Schema(description = "机构id")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    @Schema(description = "归属方")
    private Long parentId;
    @Schema(description = "归属方ID")
    private String parentName;
    @Schema(description = "账户类型")
    private String ownerType;
    @Schema(description = "账户id")
    private Long ownerId;
    @Schema(description = "账户名称")
    private String ownerName;
    @Schema(description = "余额")
    private BigDecimal balance;
    @Schema(description = "版本")
    private Long version;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}