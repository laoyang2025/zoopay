package io.renren.zadmin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.renren.commons.tools.utils.DateUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 机器人账号
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-18
*/
@Data
@Schema(description = "机器人账号")
public class ZBotDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "聊天群")
    private String chatId;
    @Schema(description = "服务ID")
    private Long serveId;
    @Schema(description = "服务名")
    private String serveName;
    @Schema(description = "服务类型")
    private String serveType;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}