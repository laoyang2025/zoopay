package io.renren.bledger.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* 机器人账号
*
* @author kernel kernel@qq.com
* @since 1.0 2024-06-02
*/
@Data
@Schema(description = "机器人账号")
public class BotAccountDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "ID")
    private Long userId;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "账户余额")
    private Long balance;
    @Schema(description = "version")
    private Long version;
    @Schema(description = "USD汇率")
    private BigDecimal usdRate;
    @Schema(description = "手续费率")
    private BigDecimal feeRate;
    @Schema(description = "飞机密钥")
    private String botChat;
    @Schema(description = "飞机管理员")
    private String botAdmin;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}