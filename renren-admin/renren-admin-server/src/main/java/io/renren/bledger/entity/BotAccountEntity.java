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
 * 机器人账号
 *
 * @author kernel kernel@qq.com
 * @since 1.0 2024-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bot_account")
public class BotAccountEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 机构ID
     */
    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
    /**
     * ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 账户余额
     */
    private Long balance;
    private Long version;
    /**
     * USD汇率
     */
    private BigDecimal usdRate;
    /**
     * 手续费率
     */
    private BigDecimal feeRate;
    /**
     * group
     */
    private String botChat;
    /**
     * 飞机管理员
     */
    private String botAdmin;
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