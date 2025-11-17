package io.renren.bledger.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 充值
 *
 * @author kernel kernel@qq.com
 * @since 1.0 2024-06-02
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class BotChargeExcel {
    @ExcelProperty(value = "用户名", index = 0)
    private String userName;
    @ExcelProperty(value = "法币金额", index = 1)
    private Long amount;
    @ExcelProperty(value = "手续费", index = 2)
    private Long fee;
    @ExcelProperty(value = "手续费率", index = 5)
    private BigDecimal feeRate;
}