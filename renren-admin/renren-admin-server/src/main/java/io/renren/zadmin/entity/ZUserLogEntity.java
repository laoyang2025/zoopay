package io.renren.zadmin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

import io.renren.commons.mybatis.entity.BaseEntity;

/**
 * z_user_log
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("z_user_log")
public class ZUserLogEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 机构ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    //
    private String deptName;
    //
    private Long agentId;
    private String agentName;
    private Long userId;
    private String username;
    //
    private Long cardId;
    private String cardUser;
    private String cardNo;
    //
    private String utr;
    private String tn;
    private String narration;
    private BigDecimal amount;
    private BigDecimal balance;
    private String flag; // 记账方向: plus, minus
    //
    private Long chargeId;
    private Integer failCount;
    private Integer matchStatus;

}