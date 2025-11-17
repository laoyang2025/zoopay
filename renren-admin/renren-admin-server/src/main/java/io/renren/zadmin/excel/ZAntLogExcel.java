package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * z_ant_log
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZAntLogExcel {
    @ExcelProperty(value = "码农名称", index = 0)
    private String antName;
    @ExcelProperty(value = "卡户名", index = 1)
    private String cardUser;
    @ExcelProperty(value = "卡号", index = 2)
    private String cardNo;
    @ExcelProperty(value = "UTR", index = 3)
    private String utr;
    @ExcelProperty(value = "流水金额", index = 4)
    private Long amount;
    @ExcelProperty(value = "余额", index = 5)
    private Long balance;
    @ExcelProperty(value = "记账方向", index = 6)
    private String flag;
    @ExcelProperty(value = "TN", index = 7)
    private String tn;
    @ExcelProperty(value = "匹配收款ID", index = 8)
    private Long chargeId;
    @ExcelProperty(value = "失败次数", index = 9)
    private Integer failCount;
    @ExcelProperty(value = "创建时间", index = 10)
    private Date createDate;
}