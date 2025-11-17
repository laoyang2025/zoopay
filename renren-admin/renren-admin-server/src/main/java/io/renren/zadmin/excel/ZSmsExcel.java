package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * z_sms
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-09-10
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZSmsExcel {
    @ExcelProperty(value = "Long", index = 0)
    private Long id;
    @ExcelProperty(value = "String", index = 1)
    private String content;
    @ExcelProperty(value = "String", index = 2)
    private String phone;
    @ExcelProperty(value = "创建时间", index = 3)
    private Date createDate;
}