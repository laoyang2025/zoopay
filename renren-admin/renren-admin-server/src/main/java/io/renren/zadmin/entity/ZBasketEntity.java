package io.renren.zadmin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import io.renren.commons.mybatis.entity.BaseEntity;

/**
 * z_basket
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("z_basket")
public class ZBasketEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Long updater;
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Date updateDate;
    private String deptName;

	// 业务字段
    private Integer enabled;
    private String accountUser;
    private String accountNo;
    private String accountBank;
    private String accountIfsc;
}