package io.renren.zadmin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import io.renren.commons.mybatis.entity.BaseEntity;

/**
 * z_warning
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("z_warning")
public class ZWarningEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    // 业务字段
    private String deptName;
    private String msgType;
    private String msg;
}