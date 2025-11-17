package io.renren.zadmin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

import io.renren.commons.mybatis.entity.BaseEntity;

/**
 * z_user_charge
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("z_user_charge")
public class ZUserChargeEntity extends BaseEntity {
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
    private BigDecimal amount;
    private BigDecimal chargeRate;
    private BigDecimal fee;
    private String assignType;
    private Long basketId;
    private Long withdrawId;
    private String accountUser;
    private String accountNo;
    private String accountBank;
    private String accountIfsc;
    private Integer processStatus;
    private String utr;
    private String pictures;
}