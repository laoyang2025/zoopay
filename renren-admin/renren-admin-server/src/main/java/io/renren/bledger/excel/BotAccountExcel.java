package io.renren.bledger.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 机器人账号
 *
 * @author kernel kernel@qq.com
 * @since 1.0 2024-06-02
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class BotAccountExcel {
    @ExcelProperty(value = "用户名", index = 0)
    private String userName;
    @ExcelProperty(value = "账户余额", index = 1)
    private Long balance;
    @ExcelProperty(value = "USD汇率", index = 2)
    private BigDecimal usdRate;
    @ExcelProperty(value = "手续费率", index = 3)
    private BigDecimal feeRate;
    @ExcelProperty(value = "飞机密钥", index = 4)
    private String botKey;
    @ExcelProperty(value = "飞机管理员", index = 5)
    private String botAdmin;
}