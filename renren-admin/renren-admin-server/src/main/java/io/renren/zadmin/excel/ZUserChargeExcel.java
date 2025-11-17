package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.util.Date;

/**
 * z_user_charge
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZUserChargeExcel {
    @ExcelProperty(value = "代理名称", index = 0)
    private String agentName;
    @ExcelProperty(value = "卡主名称", index = 1)
    private String username;
    @ExcelProperty(value = "充值金额", index = 2)
    private Long amount;
    @ExcelProperty(value = "收入", index = 3)
    private Long fee;
    @ExcelProperty(value = "分配类型", index = 4)
    private String assignType;
    @ExcelProperty(value = "公户ID", index = 5)
    private Long basketId;
    @ExcelProperty(value = "代付ID", index = 6)
    private Long withdrawId;
    @ExcelProperty(value = "账户名", index = 7)
    private String accountUser;
    @ExcelProperty(value = "账号", index = 8)
    private String accountNo;
    @ExcelProperty(value = "银行", index = 9)
    private String accountBank;
    @ExcelProperty(value = "IFSC", index = 10)
    private String accountIfsc;
    @ExcelProperty(value = "状态", index = 11)
    private Integer processStatus;
    @ExcelProperty(value = "UTR", index = 12)
    private String utr;
    @ExcelProperty(value = "创建时间", index = 13)
    private Date createDate;
}