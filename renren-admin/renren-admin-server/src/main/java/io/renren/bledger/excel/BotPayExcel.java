package io.renren.bledger.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * 付款
 *
 * @author kernel kernel@qq.com
 * @since 1.0 2024-06-02
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class BotPayExcel {
    @ExcelProperty(value = "用户名", index = 0)
    private String userName;
    @ExcelProperty(value = "USD金额", index = 1)
    private Long usdAmount;
}