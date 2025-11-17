package io.renren.bledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 付款
*
* @author kernel kernel@qq.com
* @since 1.0 2024-06-02
*/
@Data
@Schema(description = "付款")
public class BotPayDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "ID")
    private Long userId;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "USD金额")
    private Long amount;
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