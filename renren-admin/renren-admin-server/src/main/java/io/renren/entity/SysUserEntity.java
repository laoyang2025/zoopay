/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.renren.commons.mybatis.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户管理
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("sys_user")
public class SysUserEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String username;
    private String password;
    private String realName;
    private String headUrl;
    private Integer gender;
    private String email;
    private String mobile;
    private Integer superAdmin;
    private Integer superTenant;
    private Long tenantCode;
    private Integer status;
    private String remark;
    private Long deptId;
    private String deptName;

    /**
     * 删除标识  0：未删除    1：删除
     */
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    /**
     * totp
     */
    private int totpStatus;
    private String  totpKey;

    /**
     * 'agent, user, ant, operation, merchant',
     */
    String userType;

    /**
     * user_type = agent
     */
    Long deposit;          // '保证金',
    BigDecimal agentRate;  // '代理点位',
    String accountIfsc;    // 'IFSC',
    String accountBank;    // '银行名称',
    String accountUser;    // '账户名称',
    String accountNo;      // '账号',
    /**
     * user_type = user 卡主
     */
    Long agentId;         // 在哪个代理下
    Long shareId;         // 出借额度
    String agentName;     // 在哪个代理下
    BigDecimal userRate;  // 給到卡主点位
    /**
     * user_type = ant
     */
    String signIp;            // 注册IP
    Long p1;                  // 父节点
    Long p2;                  // 爷节点
    String rcode;             // 推荐码
    String memo;              // 备注
    BigDecimal totalCharge;         // 充值总额
    BigDecimal totalIncome;         // 充值收入
    BigDecimal totalCollect;        // 总体接单
    BigDecimal totalC1;             // 一级代理佣金
    BigDecimal totalC2;             // 二级代理佣金
    Long c1;                  // 一级代理数量
    Long c2;                  // 二级代理数量
    /**
     * user_type = merchant
     */
    Integer dev;              // 0:开发模式, 1:生产模式
    BigDecimal withdrawRate;  // 商户代付点位
    BigDecimal withdrawFix;   // 提现定额
    BigDecimal chargeMax;           // 最大充值金额
    BigDecimal chargeMin;           // 最小充值金额
    BigDecimal withdrawMax;         // 最大代付金额
    BigDecimal withdrawMin;         // 最小代付金额
    String secretKey;         // 密钥
    String whiteIp;           // 白名单
    Integer autoWithdraw;     // 自动代付
    Long middleId; //
    String middleName; //
}
