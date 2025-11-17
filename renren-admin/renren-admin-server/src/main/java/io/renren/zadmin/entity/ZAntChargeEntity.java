package io.renren.zadmin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

import io.renren.commons.mybatis.entity.BaseEntity;

/**
 * z_charge
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("z_ant_charge")
public class ZAntChargeEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    @TableField(fill = FieldFill.INSERT_UPDATE)

    // 业务字段
    private Date updateDate;
    private String deptName;
    private Long antId;
    private Long antName;
    private BigDecimal amount;
    private String assignType;
    private Long basketId;
    private Long withdrawId;
    private String utr;
    private String pictures;
    private Integer processStatus;
    private Integer settleFlag;


}