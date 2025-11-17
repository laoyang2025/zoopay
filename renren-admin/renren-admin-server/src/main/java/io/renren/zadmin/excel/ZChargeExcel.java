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
public class ZChargeExcel {
    @ExcelProperty(value = "ID", index = 0)
    private Long id;
    @ExcelProperty(value = "商户ID", index = 1)
    private Long merchantId;
    @ExcelProperty(value = "商户名称", index = 2)
    private String merchantName;
    @ExcelProperty(value = "收款扣率", index = 3)
    private BigDecimal merchantRate;
    @ExcelProperty(value = "商户单号", index = 4)
    private String orderId;
    @ExcelProperty(value = "充值金额", index = 5)
    private BigDecimal amount;
    @ExcelProperty(value = "实际金额", index = 6)
    private BigDecimal realAmount;
    @ExcelProperty(value = "商户本金", index = 7)
    private BigDecimal merchantPrincipal;
    @ExcelProperty(value = "手续费", index = 8)
    private BigDecimal merchantFee;
    private String ip;
    @ExcelProperty(value = "UPI", index = 9)
    private String upi;
    @ExcelProperty(value = "UTR", index = 10)
    private String utr;
    @ExcelProperty(value = "TN", index = 11)
    private String tn;
    @ExcelProperty(value = "状态", index = 12)
    private Integer processStatus;
    @ExcelProperty(value = "通知时间", index = 13)
    private Date notifyTime;
    @ExcelProperty(value = "通知状态", index = 14)
    private Integer notifyStatus;
    @ExcelProperty(value = "通知次数", index = 15)
    private Integer notifyCount;
    @ExcelProperty(value = "创建时间", index = 16)
    private Date createDate;
}