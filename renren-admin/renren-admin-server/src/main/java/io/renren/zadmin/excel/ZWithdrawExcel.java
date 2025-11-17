package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * z_withdraw
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZWithdrawExcel {
    @ExcelProperty(value = "ID", index = 0)
    private Long id;
    @ExcelProperty(value = "商户名", index = 1)
    private String merchantName;
    @ExcelProperty(value = "商户单号", index = 2)
    private String orderId;
    @ExcelProperty(value = "UTR", index = 3)
    private String utr;
    @ExcelProperty(value = "UPI", index = 4)
    private String upi;
    @ExcelProperty(value = "提现手续费", index = 5)
    private BigDecimal merchantFee;
    @ExcelProperty(value = "订单金额", index = 6)
    private BigDecimal amount;
    @ExcelProperty(value = "银行", index = 7)
    private String accountBank;
    @ExcelProperty(value = "用户", index = 8)
    private String accountUser;
    @ExcelProperty(value = "账号", index = 9)
    private String accountNo;
    @ExcelProperty(value = "IFSC", index = 10)
    private String accountIfsc;
    @ExcelProperty(value = "处理状态", index = 11)
    private Integer processStatus;
}