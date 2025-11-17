package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
* z_card
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@Data
@Schema(description = "z_card")
public class ZCardDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "ID")
    private Long id;
    @Schema(description = "机构ID")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;
    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

    // 业务字段

    @Schema(description = "IFSC")
    private String accountIfsc;
    @Schema(description = "银行名")
    private String accountBank;
    @Schema(description = "账户名")
    private String accountUser;
    @Schema(description = "账号")
    private String accountNo;
    @Schema(description = "UPI")
    private String accountUpi;
    @Schema(description = "info")
    private String accountInfo;
    @Schema(description = "银行余额")
    private BigDecimal bankBalance;
    @Schema(description = "告警金额")
    private BigDecimal warningAmount;
    @Schema(description = "卡代码")
    private String cardCode;
    @Schema(description = "登录id")
    private String loginId;
    @Schema(description = "登录密码")
    private String password;
    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "登录超时")
    private Integer initTimeout;
    @Schema(description = "下载超时")
    private Integer timeout;
    @Schema(description = "下载间隔")
    private Integer gap;
    @Schema(description = "管理等待")
    private Integer adminTimeout;

    @Schema(description = "超时session")
    private Integer sessionTimeout;
    @Schema(description = "自动验证码")
    private Integer autoCaptcha;
    @Schema(description = "自动短信")
    private Integer autoSms;

    @Schema(description = "设备ID")
    private String deviceId;

    @Schema(description = "运行配置")
    private String runConfig;

}