package io.renren.zadmin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

import io.renren.commons.mybatis.entity.BaseEntity;

/**
 * z_user_card
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("z_user_card")
public class ZUserCardEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    // 业务字段
    private String deptName;
    private Long agentId;
    private String agentName;
    private Long userId;
    private String username;
    private Integer enabled;
    private String accountIfsc;
    private String accountBank;
    private String accountUser;
    private String accountNo;
    private String accountUpi;
    private String accountInfo;
    private BigDecimal bankBalance;

    private String cardCode;
    private String loginId;
    private String password;
    private String phone;

//    private Integer initTimeout;
//    private Integer timeout;
//    private Integer gap;
//    private Integer hold;

    private String runConfig;
}