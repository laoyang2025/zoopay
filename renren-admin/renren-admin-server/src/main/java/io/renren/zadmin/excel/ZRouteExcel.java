package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * z_route
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-12
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZRouteExcel {
    @ExcelProperty(value = "商户ID", index = 0)
    private Long merchantId;
    @ExcelProperty(value = "商户名", index = 1)
    private String merchantName;
    @ExcelProperty(value = "路由模式", index = 2)
    private String processMode;
    @ExcelProperty(value = "目标ID", index = 3)
    private Long objectId;
    @ExcelProperty(value = "目标名称", index = 4)
    private String objectName;
    @ExcelProperty(value = "收款权重", index = 5)
    private Integer weight;
    @ExcelProperty(value = "付款权重", index = 6)
    private Integer withdrawweight;
    @ExcelProperty(value = "创建者", index = 7)
    private Long creator;
}