package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * z_charge
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZAntChargeExcel {
    @ExcelProperty(value = "ID", index = 0)
    private Long id;
    @ExcelProperty(value = "码农ID", index = 1)
    private Long antId;
    @ExcelProperty(value = "码农", index = 2)
    private Long antName;
    @ExcelProperty(value = "充值金额", index = 3)
    private BigDecimal amount;
    @ExcelProperty(value = "充值方式", index = 4)
    private String assignType;
    @ExcelProperty(value = "对公账户id", index = 5)
    private Long basketId;
    @ExcelProperty(value = "提现ID", index = 6)
    private Long withdrawId;
    @ExcelProperty(value = "UTR", index = 7)
    private String utr;
    @ExcelProperty(value = "凭证", index = 8)
    private String pictures;
    @ExcelProperty(value = "处理状态", index = 9)
    private Integer processStatus;
    @ExcelProperty(value = "清算标志", index = 10)
    private Integer settleFlag;
    @ExcelProperty(value = "创建时间", index = 11)
    private Date createDate;
}