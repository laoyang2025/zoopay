package io.renren.bledger.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.renren.commons.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 充值
 *
 * @author kernel kernel@qq.com
 * @since 1.0 2024-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bot_charge")
public class BotChargeEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 机构ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 法币金额
     */
    private Long amount;
    /**
     * 手续费
     */
    private Long fee;
    /**
     * 手续费率
     */
    private BigDecimal feeRate;
    /**
     * 删除标识  0：未删除    1：删除
     */
    private Integer delFlag;
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