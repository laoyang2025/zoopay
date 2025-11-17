package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * z_agent_charge
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZAgentChargeExcel {
    @ExcelProperty(value = "ID", index = 0)
    private Long id;
    @ExcelProperty(value = "代理名称", index = 1)
    private String agentName;
    @ExcelProperty(value = "充值金额", index = 2)
    private Long amount;
    @ExcelProperty(value = "对公户ID", index = 3)
    private Long basketId;
    @ExcelProperty(value = "账号名称", index = 4)
    private String accountUser;
    @ExcelProperty(value = "账号", index = 5)
    private String accountNo;
    @ExcelProperty(value = "银行名称", index = 6)
    private String accountBank;
    @ExcelProperty(value = "IFSC", index = 7)
    private String accountIfsc;
    @ExcelProperty(value = "状态", index = 8)
    private Integer processStatus;
    @ExcelProperty(value = "UTR", index = 9)
    private String utr;
    @ExcelProperty(value = "String", index = 10)
    private String pictures;
    @ExcelProperty(value = "创建时间", index = 11)
    private Date createDate;
}