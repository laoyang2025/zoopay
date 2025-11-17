package io.renren.bledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
* 余额流水
*
* @author kernel kernel@qq.com
* @since 1.0 2024-06-02
*/
@Data
@Schema(description = "余额流水")
public class BotLogDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "ID")
    private Long userId;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "旧余额")
    private Long oldAmount;
    @Schema(description = "新余额")
    private Long newAmount;
    @Schema(description = "发生额")
    private Long amount;
    @Schema(description = "事实id")
    private Long factId;
    @Schema(description = "事实类型")
    private Integer factType;
    @Schema(description = "事实简介")
    private String factMemo;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}