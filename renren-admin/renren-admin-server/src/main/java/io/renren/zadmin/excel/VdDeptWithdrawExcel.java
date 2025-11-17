package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

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
public class VdDeptWithdrawExcel {
    @ExcelProperty(value = "成功金额", index = 0)
    private BigDecimal successAmount;
    @ExcelProperty(value = "手续费", index = 1)
    private BigDecimal merchantFee;
    @ExcelProperty(value = "成本", index = 2)
    private BigDecimal cost;
    @ExcelProperty(value = "成功笔数", index = 3)
    private BigDecimal success;
    @ExcelProperty(value = "失败笔数", index = 4)
    private BigDecimal fail;
    @ExcelProperty(value = "日期", index = 5)
    private Date createDate;
    @ExcelProperty(value = "机构名称", index = 6)
    private String deptName;
}