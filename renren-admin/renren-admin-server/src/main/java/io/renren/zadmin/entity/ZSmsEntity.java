package io.renren.zadmin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

import io.renren.commons.mybatis.entity.BaseEntity;

/**
 * z_sms
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-09-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("z_sms")
public class ZSmsEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 机构id
     */
    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
    /**
     * 机构名称
     */
    private String deptName;
    private String content;
    private String phone;
	private String deviceId;
    private String md5;

    private String utr;
    private BigDecimal Amount;
    private Integer matchStatus;
    private Integer failCount;
    Long cardId;
    private Long chargeId;

    /**
     * 更新者
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;
}