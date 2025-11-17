package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* z_basket
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Data
@Schema(description = "z_basket")
public class ZBasketDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "机构id")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    @Schema(description = "启用")
    private Integer enabled;
    @Schema(description = "公户户名")
    private String accountUser;
    @Schema(description = "公户账号")
    private String accountNo;
    @Schema(description = "公户银行")
    private String accountBank;
    @Schema(description = "IFSC")
    private String accountIfsc;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}