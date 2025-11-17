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
public class VdCardExcel {
    @ExcelProperty(value = "BigDecimal", index = 0)
    private BigDecimal successAmount;
    @ExcelProperty(value = "BigDecimal", index = 1)
    private BigDecimal success;
    @ExcelProperty(value = "BigDecimal", index = 2)
    private BigDecimal fail;
    @ExcelProperty(value = "BigDecimal", index = 3)
    private BigDecimal successRate;
    @ExcelProperty(value = "Date", index = 4)
    private Date createDate;
    @ExcelProperty(value = "卡账号", index = 5)
    private String cardNo;
    @ExcelProperty(value = "卡户名", index = 6)
    private String cardUser;
}