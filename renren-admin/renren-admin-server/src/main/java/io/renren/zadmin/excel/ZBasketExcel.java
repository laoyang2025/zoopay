package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * z_basket
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZBasketExcel {
    @ExcelProperty(value = "启用", index = 0)
    private Integer enabled;
    @ExcelProperty(value = "公户户名", index = 1)
    private String accountUser;
    @ExcelProperty(value = "公户账号", index = 2)
    private String accountNo;
    @ExcelProperty(value = "公户银行", index = 3)
    private String accountBank;
    @ExcelProperty(value = "IFSC", index = 4)
    private String accountIfsc;
    @ExcelProperty(value = "创建时间", index = 5)
    private Date createDate;
}