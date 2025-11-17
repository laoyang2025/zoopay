package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * z_warning
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-15
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZWarningExcel {
    @ExcelProperty(value = "消息类型", index = 0)
    private String msgType;
    @ExcelProperty(value = "消息体", index = 1)
    private String msg;
    @ExcelProperty(value = "创建时间", index = 2)
    private Date createDate;
}