package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * z_card_log
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZCardLogExcel {
    @ExcelProperty(value = "ID", index = 0)
    private Long id;
    @ExcelProperty(value = "账户名称", index = 1)
    private String cardUser;
    @ExcelProperty(value = "账号", index = 2)
    private String cardNo;
    @ExcelProperty(value = "UTR", index = 3)
    private String utr;
    @ExcelProperty(value = "TN", index = 4)
    private String tn;
    @ExcelProperty(value = "流水描述", index = 5)
    private String narration;
    @ExcelProperty(value = "流水金额", index = 6)
    private Long amount;
    @ExcelProperty(value = "余额", index = 7)
    private Long balance;
    @ExcelProperty(value = "记账方向", index = 8)
    private String flag;
    @ExcelProperty(value = "收款ID", index = 9)
    private Long chargeId;
    @ExcelProperty(value = "失败次数", index = 10)
    private Integer failCount;
}