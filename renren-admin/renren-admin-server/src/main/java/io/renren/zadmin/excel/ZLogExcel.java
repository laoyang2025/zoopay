package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * z_log
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZLogExcel {
    @ExcelProperty(value = "余额ID", index = 0)
    private Long balanceId;
    @ExcelProperty(value = "账户名称", index = 1)
    private String ownerName;
    @ExcelProperty(value = "事实ID", index = 2)
    private Long factId;
    @ExcelProperty(value = "事实类型", index = 3)
    private Integer factType;
    @ExcelProperty(value = "事实金额", index = 4)
    private BigDecimal factAmount;
    @ExcelProperty(value = "事实说明", index = 5)
    private String factMemo;
    @ExcelProperty(value = "旧余额", index = 6)
    private BigDecimal oldBalance;
    @ExcelProperty(value = "新余额", index = 7)
    private BigDecimal newBalance;
    @ExcelProperty(value = "版本变化", index = 8)
    private String mutation;
    @ExcelProperty(value = "创建时间", index = 9)
    private Date createDate;
}