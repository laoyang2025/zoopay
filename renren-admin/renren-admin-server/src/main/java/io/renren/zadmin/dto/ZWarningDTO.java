package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* z_warning
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-15
*/
@Data
@Schema(description = "z_warning")
public class ZWarningDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    @Schema(description = "机构id")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    @Schema(description = "消息类型")
    private String msgType;
    @Schema(description = "消息体")
    private String msg;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}