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
@EqualsAndHashCode(callSuper=false)
@TableName("vd_dept_withdraw")
public class VdDeptWithdrawEntity {
private static final long serialVersionUID = 1L;

			/**
			* 成功金额
			*/
		private BigDecimal successAmount;
			/**
			* 手续费
			*/
		private BigDecimal merchantFee;
			/**
			* 成本
			*/
		private BigDecimal cost;
			/**
			* 成功笔数
			*/
		private BigDecimal success;
			/**
			* 失败笔数
			*/
		private BigDecimal fail;
			/**
			* 日期
			*/
			@TableField(fill = FieldFill.INSERT)
		private Date createDate;
			/**
			* 机构名称
			*/
		private String deptName;
}