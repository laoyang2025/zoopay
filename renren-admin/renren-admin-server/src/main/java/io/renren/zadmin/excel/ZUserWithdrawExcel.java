package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * z_user_withdraw
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZUserWithdrawExcel {
    @ExcelProperty(value = "ID", index = 0)
    private Long id;
    @ExcelProperty(value = "代理名称", index = 1)
    private String agentName;
    @ExcelProperty(value = "卡主名称", index = 2)
    private Long username;
    @ExcelProperty(value = "公户名", index = 3)
    private Long basketUser;
    @ExcelProperty(value = "公户账号", index = 4)
    private Long basketNo;
    @ExcelProperty(value = "提现金额", index = 5)
    private Long amount;
    @ExcelProperty(value = "提现费用", index = 6)
    private Long outFee;
    @ExcelProperty(value = "账户名", index = 7)
    private String accountUser;
    @ExcelProperty(value = "账号", index = 8)
    private String accountNo;
    @ExcelProperty(value = "状态", index = 9)
    private Integer processStatus;
    @ExcelProperty(value = "UTR", index = 10)
    private String utr;
    @ExcelProperty(value = "创建时间", index = 11)
    private Date createDate;
}