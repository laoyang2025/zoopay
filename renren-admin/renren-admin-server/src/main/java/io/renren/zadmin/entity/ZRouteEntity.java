package io.renren.zadmin.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;

import java.math.BigDecimal;
import java.util.Date;

import io.renren.commons.mybatis.entity.BaseEntity;

/**
 * z_route
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("z_route")
public class ZRouteEntity extends BaseEntity {
    private static final long serialVersionUID = 1L;

    @TableField(fill = FieldFill.INSERT)
    private Long deptId;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Long updater;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateDate;

    // 业务字段
    private String deptName;
    private Long merchantId;
    private String merchantName;
    private Integer enabled;
    private String routeType;
    // 收款|付款路由参数
    private String processMode;
    private Long objectId;
    private String objectName;
    private Integer weight;
    // 收款路由
    private String payCode;
    private BigDecimal bigAmount;
    private BigDecimal chargeRate;
}