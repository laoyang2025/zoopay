package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.math.BigDecimal;

/**
 * VIEW
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-09-10
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class VhMerchantExcel {
    @ExcelProperty(value = "成功金额", index = 0)
    private BigDecimal successAmount;
    @ExcelProperty(value = "成功笔数", index = 1)
    private BigDecimal success;
    @ExcelProperty(value = "失败笔数", index = 2)
    private BigDecimal fail;
    @ExcelProperty(value = "成功率", index = 3)
    private BigDecimal successRate;
    @ExcelProperty(value = "时间", index = 4)
    private String createDate;
    @ExcelProperty(value = "商户名称", index = 5)
    private String merchantName;
    @ExcelProperty(value = "机构名称", index = 6)
    private String deptName;
}