package io.renren.zadmin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
* z_route
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-12
*/
@Data
@Schema(description = "z_route")
public class ZRouteDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    @Schema(description = "机构id")
    private Long deptId;
    @Schema(description = "机构名称")
    private String deptName;

    @Schema(description = "启用")
    private Integer enabled;
    @Schema(description = "商户ID")
    private Long merchantId;
    @Schema(description = "商户名")
    private String merchantName;
    @Schema(description = "商户名")
    private String routeType;

    // 收款付款路由参数
    @Schema(description = "路由模式")
    private String processMode;
    @Schema(description = "目标ID")
    private Long objectId;
    @Schema(description = "目标名称")
    private String objectName;
    @Schema(description = "收款权重")
    private Integer weight;

    // 收款参数
    @Schema(description = "支付编码")
    private String payCode;
    @Schema(description = "收款扣率")
    private BigDecimal chargeRate;
    @Schema(description = "大金额")
    private BigDecimal bigAmount;


    @Schema(description = "创建者")
    private Long creator;
    @Schema(description = "创建时间")
    private Date createDate;
    @Schema(description = "更新者")
    private Long updater;
    @Schema(description = "更新时间")
    private Date updateDate;

    @Data
    public static class RouteObject {
        Long objectId;
        String objectName;
    }

    private List<RouteObject> objectList;
}