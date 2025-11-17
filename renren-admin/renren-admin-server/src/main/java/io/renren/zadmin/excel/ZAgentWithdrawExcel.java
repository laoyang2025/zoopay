package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * z_agent_withdraw
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZAgentWithdrawExcel {
    @ExcelProperty(value = "ID", index = 0)
    private Long id;
    @ExcelProperty(value = "代理", index = 1)
    private String agentName;
    @ExcelProperty(value = "对公户名称", index = 2)
    private Long basketUser;
    @ExcelProperty(value = "对公户账号", index = 3)
    private Long basketNo;
    @ExcelProperty(value = "提现金额", index = 4)
    private Long amount;
    @ExcelProperty(value = "提现户名", index = 5)
    private String accountUser;
    @ExcelProperty(value = "提现账号", index = 6)
    private String accountNo;
    @ExcelProperty(value = "状态", index = 7)
    private Integer processStatus;
    @ExcelProperty(value = "utr", index = 8)
    private String utr;
    @ExcelProperty(value = "创建时间", index = 9)
    private Date createDate;
}