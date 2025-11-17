package io.renren.bledger.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.renren.commons.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 余额流水
 *
 * @author kernel kernel@qq.com
 * @since 1.0 2024-06-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bot_log")
public class BotLogEntity extends BaseEntity {
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
     * 旧余额
     */
    private Long oldAmount;
    /**
     * 新余额
     */
    private Long newAmount;
    /**
     * 发生额
     */
    private Long amount;
    /**
     * 事实id
     */
    private Long factId;
    /**
     * 事实类型
     */
    private Integer factType;
    /**
     * 事实简介
     */
    private String factMemo;
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