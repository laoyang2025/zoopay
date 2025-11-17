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
* @since 3.0 2024-09-11
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("vd_merchant_withdraw")
public class VdMerchantWithdrawEntity {
private static final long serialVersionUID = 1L;

			/**
			* 成功金额
			*/
		private BigDecimal successAmount;
			/**
			* 手续费
			*/
		private BigDecimal fee;
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
			* 商户名
			*/
		private String merchantName;
			/**
			* 机构名称
			*/
		private String deptName;
			/**
			* 机构id
			*/
			@TableField(fill = FieldFill.INSERT)
		private Long deptId;
}