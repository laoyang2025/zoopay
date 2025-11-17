/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.commons.security.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户信息
 *
 * @author Mark sunlightcs@gmail.com
 */
@Data
public class MyUserDetail implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String realName;
    private String headUrl;
    private Integer gender;
    private String email;
    private String mobile;
    private Long deptId;
    private String password;
    private Integer status;
    private Integer superAdmin;
    private Integer superTenant;
    private Long tenantCode;
    private List<Long> deptIdList;

    // 部分机构信息填充
    private String apiDomain;
    private String currency;
    private String timezone;
    private String deptName;
    private String processMode;
    private BigDecimal antChargeRate;
    private BigDecimal outRate;
    private BigDecimal c1Rate;
    private BigDecimal c2Rate;

    /**/
    private String userType;
    private String totpKey;
    private Integer totpStatus;

    /**
     * user_type = agent
     */
    Long deposit;       // bigint comment '保证金',
    BigDecimal agentRate; //     decimal(4, 4) comment '代理点位',
    String accountIfsc;  // varchar(15) comment 'IFSC',
    String accountBank;   //varchar(64) comment '银行名称',
    String accountUser;   //varchar(64) comment '账户名称',
    String accountNo;     //varchar(32) comment '账号',
    /**
     * user_type = user
     */
    Long agentId; //       bigint comment '',
    String agentName; //     varchar(32) comment '',
    BigDecimal userRate; //      decimal(4, 4) comment '充值点位',
    /**
     * user_type = ant
     */
    String signIp; //        varchar(16) comment '注册IP',
    Long p1;       //      bigint comment '父节点',
    Long p2;      //       bigint comment '爷节点',
    String rcode;//          varchar(32) comment '推荐码',
    String memo; //          varchar(128) comment '备注',
    Long totalCharge; //   bigint comment '充值总额',
    Long totalIncome; //    bigint comment 'income',
    Long totalCollect; //  bigint comment '总体接单',
    Long totalC1; //       bigint comment '一级代理佣金',
    Long totalC2; //       bigint comment '二级代理佣金',
    Long c1; //             bigint comment '一级代理数量',
    Long c2; //             bigint comment '二级代理数量',
    /**
     * user_type = merchant
     */
    Integer dev; //            int comment '0:开发模式, 1:生产模式',
    BigDecimal chargeRate;
    BigDecimal withdrawRate;
    BigDecimal withdrawFix;
    Long chargeMax;       // 最大充值金额',
    Long chargeMin;       // 最小充值金额',
    Long withdrawMax;     // 最大代付金额',
    Long withdrawMin;     // 最小代付金额',
    String secretKey;     // 密钥',
    String whiteIp;       // 白名单
    String chargeCards;   // 走哪些卡
    String chargeAgent;   // 走哪些代理
    String withdrawCards; // 走哪些卡
    String withdrawAgent; // 走哪些代理
    Integer autoWithdraw; // 自动代付


    // UserDetails接口要求数据
    private boolean isAccountNonExpired = true;
    private boolean isAccountNonLocked = true;
    private boolean isCredentialsNonExpired = true;
    private boolean isEnabled = true;
    private Set<String> authoritySet;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authoritySet.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toSet());
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.isAccountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isAccountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.isCredentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.isEnabled;
    }

}