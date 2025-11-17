package io.renren.zadmin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

import io.renren.commons.mybatis.entity.BaseEntity;

/**
 * z_ant_log
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("z_ant_log")
public class ZAntLogEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    // 业务字段
    private String deptName;
    //
    private Long antId;
    private String antName;
    //
    private Long cardId;
    private String cardUser;
    private String cardNo;
    //
    private String utr;
    private String narration;
    private BigDecimal amount;
    private BigDecimal balance;
    private String flag;
    private String tn;
    //
    private Long chargeId;
    private Integer failCount;
    private Integer matchStatus;

}