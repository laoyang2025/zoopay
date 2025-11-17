/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.renren.commons.tools.utils.DateUtils;
import io.renren.commons.tools.validator.group.AddGroup;
import io.renren.commons.tools.validator.group.DefaultGroup;
import io.renren.commons.tools.validator.group.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 用户管理
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@Schema(description = "用户管理")
public class SysUserDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "id")
    @Null(message = "{id.null}", groups = AddGroup.class)
    @NotNull(message = "{id.require}", groups = UpdateGroup.class)
    private Long id;

    @Schema(description = "用户名", required = true)
//    @NotBlank(message = "{sysuser.username.require}", groups = DefaultGroup.class)
    private String username;

    @Schema(description = "密码")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "{sysuser.password.require}", groups = AddGroup.class)
    private String password;

    @Schema(description = "姓名", required = true)
//    @NotBlank(message = "{sysuser.realname.require}", groups = DefaultGroup.class)
    private String realName;

    @Schema(description = "头像")
    private String headUrl;

    @Schema(description = "性别   0：男   1：女    2：保密", required = true)
    @Range(min = 0, max = 2, message = "{sysuser.gender.range}", groups = DefaultGroup.class)
    private Integer gender;

    @Schema(description = "邮箱", required = true)
//    @NotBlank(message = "{sysuser.email.require}", groups = DefaultGroup.class)
//    @Email(message = "{sysuser.email.error}", groups = DefaultGroup.class)
    private String email;

    @Schema(description = "手机号", required = true)
//    @NotBlank(message = "{sysuser.mobile.require}", groups = DefaultGroup.class)
    private String mobile;

    @Schema(description = "部门ID", required = true)
//    @NotNull(message = "{sysuser.deptId.require}", groups = DefaultGroup.class)
    private Long deptId;

    @Schema(description = "超级管理员   0：否   1：是")
    @Range(min = 0, max = 1, message = "{sysuser.superadmin.range}", groups = DefaultGroup.class)
    private Integer superAdmin;

    @Schema(description = "状态  0：停用    1：正常", required = true)
    @Range(min = 0, max = 1, message = "{sysuser.status.range}", groups = DefaultGroup.class)
    private Integer status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @JsonFormat(pattern = DateUtils.DATE_TIME_PATTERN)
    private Date createDate;

    @Schema(description = "角色ID列表")
    private List<Long> roleIdList;

    @Schema(description = "岗位ID列表")
    private List<Long> postIdList;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "租户编码")
    private Long tenantCode;

    // 补充的机构信息
    @Schema(description = "币种")
    private String currency;
    @Schema(description = "时区")
    private String timezone;
    @Schema(description = "运营模式")
    private String processMode;
    @Schema(description = "充值点位")
    private BigDecimal antChargeRate;
    @Schema(description = "提现点位")
    private BigDecimal outRate;
    @Schema(description = "一级分佣")
    private BigDecimal c1Rate;
    @Schema(description = "二级分佣")
    private BigDecimal c2Rate;

    //
    @Schema(description = "totpKey")
    private String totpKey;
    @Schema(description = "totpStatus")
    private int totpStatus;

    @Schema(description = "用户类型")
    String userType;   //    varchar(16) comment 'agent, user, ant, operation, merchant',
    /**
     * user_type = agent
     */
    @Schema(description = "代理保证金")
    Long deposit;       // bigint comment '保证金',
    @Schema(description = "代理点位")
    BigDecimal agentRate; //     decimal(4, 4) comment '代理点位',
    @Schema(description = "代理卡ifsc")
    String accountIfsc;  // varchar(15) comment 'IFSC',
    @Schema(description = "代理卡银行")
    String accountBank;   //varchar(64) comment '银行名称',
    @Schema(description = "代里卡户名")
    String accountUser;   //varchar(64) comment '账户名称',
    @Schema(description = "代理卡号")
    String accountNo;     //varchar(32) comment '账号',
    /**
     * user_type = user
     */
    @Schema(description = "agnetId")
    Long agentId; //       bigint comment '',
    @Schema(description = "shareId")
    Long shareId;
    @Schema(description = "代理名称")
    String agentName; //     varchar(32) comment '',
    @Schema(description = "卡主点位")
    BigDecimal userRate; //      decimal(4, 4) comment '充值点位',
    /**
     * user_type = ant
     */
    @Schema(description = "注册Ip")
    String signIp; //        varchar(16) comment '注册IP',
    @Schema(description = "父")
    Long p1;       //      bigint comment '父节点',
    @Schema(description = "爷")
    Long p2;      //       bigint comment '爷节点',
    @Schema(description = "推荐码")
    String rcode;//          varchar(32) comment '推荐码',
    @Schema(description = "备注")
    String memo; //          varchar(128) comment '备注',
    @Schema(description = "充值总额")
    BigDecimal totalCharge; //   bigint comment '充值总额',
    @Schema(description = "收入总额")
    BigDecimal totalIncome; //    bigint comment 'income',
    @Schema(description = "接单总额")
    BigDecimal totalCollect; //  bigint comment '总体接单',
    @Schema(description = "一级佣金")
    BigDecimal totalC1; //       bigint comment '一级代理佣金',
    @Schema(description = "二进佣金")
    BigDecimal totalC2; //       bigint comment '二级代理佣金',
    @Schema(description = "一级推荐数")
    Long c1; //             bigint comment '一级代理数量',
    @Schema(description = "二级推荐数")
    Long c2; //             bigint comment '二级代理数量',
    /**
     * user_type = merchant
     */
    @Schema(description = "开发模式")
    Integer dev; //            int comment '0:开发模式, 1:生产模式',
    @Schema(description = "提现点位")
    BigDecimal withdrawRate;
    @Schema(description = "提现定额")
    BigDecimal withdrawFix;
    @Schema(description = "最大充值额")
    BigDecimal chargeMax; //     bigint comment '最大充值金额',
    @Schema(description = "最小充值额")
    BigDecimal chargeMin; //  bigint comment '最小充值金额',
    @Schema(description = "最大代付额")
    BigDecimal withdrawMax; //  bigint comment '最大代付金额',
    @Schema(description = "最小代付额")
    BigDecimal withdrawMin; //   bigint comment '最小代付金额',
    @Schema(description = "密钥")
    String secretKey;     // varchar(128) comment '密钥',
    @Schema(description = "白名单")
    String whiteIp;       // varchar(128) comment '白名单',
    @Schema(description = "自动代付")
    Integer autoWithdraw; //  int          null comment '自动代付',
    @Schema(description = "拓展方")
    Long middleId; //
    @Schema(description = "拓展方")
    String middleName; //

    // for balance
    BigDecimal balance;
    BigDecimal agentShare;
}