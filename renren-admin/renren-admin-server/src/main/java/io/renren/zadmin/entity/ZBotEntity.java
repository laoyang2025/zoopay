package io.renren.zadmin.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
	import java.util.Date;
	import io.renren.commons.mybatis.entity.BaseEntity;

/**
* 机器人账号
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-18
*/
@Data
@EqualsAndHashCode(callSuper=false)
@TableName("z_bot")
public class ZBotEntity extends BaseEntity {
private static final long serialVersionUID = 1L;

			/**
			* 机构ID
			*/
			@TableField(fill = FieldFill.INSERT)
		private Long deptId;
			/**
			* 聊天群
			*/
		private String chatId;
			/**
			* 服务ID
			*/
		private Long serveId;
			/**
			* 服务名
			*/
		private String serveName;
			/**
			* 服务类型
			*/
		private String serveType;
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