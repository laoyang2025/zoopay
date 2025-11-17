package io.renren.zadmin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
	import java.math.BigDecimal;

/**
* VIEW
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-09-10
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("vh_merchant")
public class VhMerchantEntity {
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
			* 时间
			*/
			@TableField(fill = FieldFill.INSERT)
		private String createDate;
			/**
			* 商户名称
			*/
		private String merchantName;
			/**
			* 机构名称
			*/
		private String deptName;
}