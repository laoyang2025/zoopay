package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* z_user_card
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Data
@Schema(description = "z_user_card")
public class ZUserCardDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    @Schema(description = "代理ID")
    private Long agentId;
    @Schema(description = "代理名称")
    private String agentName;
    @Schema(description = "卡主ID")
    private Long userId;
    @Schema(description = "卡主名称")
    private String username;
    @Schema(description = "启用状态")
    private Integer enabled;
    @Schema(description = "IFSC")
    private String accountIfsc;
    @Schema(description = "银行名称")
    private String accountBank;
    @Schema(description = "账户名称")
    private String accountUser;
    @Schema(description = "账号")
    private String accountNo;
    @Schema(description = "upi")
    private String accountUpi;
    @Schema(description = "info")
    private String accountInfo;
    @Schema(description = "银行余额")
    private BigDecimal bankBalance;

    private String cardCode;
    @Schema(description = "登录id")
    private String loginId;
    @Schema(description = "登录密码")
    private String password;
    @Schema(description = "手机号")
    private String phone;

//    @Schema(description = "登录超时")
//    private Integer initTimeout;
//    @Schema(description = "下载超时")
//    private Integer timeout;
//    @Schema(description = "下载间隔")
//    private Integer gap;
//    @Schema(description = "中途暂停")
//    private Integer hold;

    @Schema(description = "运行配置")
    private String runConfig;


    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

}