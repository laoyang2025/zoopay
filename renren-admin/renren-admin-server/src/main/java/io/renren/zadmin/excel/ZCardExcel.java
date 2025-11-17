package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * z_card
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZCardExcel {
    @ExcelProperty(value = "ID", index = 0)
    private Long id;
    @ExcelProperty(value = "收款启用", index = 1)
    private Integer chargeEnabled;
    @ExcelProperty(value = "代付启用", index = 2)
    private Integer withdrawEnabled;
    @ExcelProperty(value = "IFSC", index = 3)
    private String accountIfsc;
    @ExcelProperty(value = "银行名", index = 4)
    private String accountBank;
    @ExcelProperty(value = "账户名", index = 5)
    private String accountUser;
    @ExcelProperty(value = "账号", index = 6)
    private String accountNo;
    @ExcelProperty(value = "创建时间", index = 7)
    private Date createDate;
}