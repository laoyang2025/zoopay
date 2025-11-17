package io.renren.zadmin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

/**
 * VIEW
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-09-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("vd_charge_card")
public class VdChargeCardEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 成功金额
     */
    private BigDecimal successAmount;
    /**
     * 成功笔数
     */
    private BigDecimal success;
    /**
     * 失败笔数
     */
    private BigDecimal fail;
    /**
     * 成功率
     */
    private BigDecimal successRate;
    /**
     * 日期
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createDate;
    /**
     * 商户名称
     */
    private String merchantName;
    /**
     * 卡账号
     */
    private String cardNo;
    /**
     * 卡户名
     */
    private String cardUser;
    /**
     * 机构名称
     */
    private String deptName;
    private Long deptId;
}