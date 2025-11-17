package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * z_user_log
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZUserLogExcel {
    @ExcelProperty(value = "代理", index = 0)
    private String agentName;
    @ExcelProperty(value = "卡主", index = 1)
    private String username;
    @ExcelProperty(value = "卡户名", index = 2)
    private String cardUser;
    @ExcelProperty(value = "卡号", index = 3)
    private String cardNo;
    @ExcelProperty(value = "UTR", index = 4)
    private String utr;
    @ExcelProperty(value = "TN", index = 5)
    private String tn;
    @ExcelProperty(value = "流水金额", index = 6)
    private Long amount;
    @ExcelProperty(value = "余额", index = 7)
    private Long balance;
    @ExcelProperty(value = "记账方向", index = 8)
    private String flag;
    @ExcelProperty(value = "收款ID", index = 9)
    private Long chargeId;
    @ExcelProperty(value = "匹配失败", index = 10)
    private Integer failCount;
    @ExcelProperty(value = "创建时间", index = 11)
    private Date createDate;
    @ExcelProperty(value = "更新者", index = 12)
    private Long updater;
}