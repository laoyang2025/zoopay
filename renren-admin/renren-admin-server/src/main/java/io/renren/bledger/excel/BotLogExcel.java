package io.renren.bledger.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;

/**
 * 余额流水
 *
 * @author kernel kernel@qq.com
 * @since 1.0 2024-06-02
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class BotLogExcel {
    @ExcelProperty(value = "用户名", index = 0)
    private String userName;
    @ExcelProperty(value = "旧余额", index = 1)
    private Long oldAmount;
    @ExcelProperty(value = "新余额", index = 2)
    private Long newAmount;
    @ExcelProperty(value = "发生额", index = 3)
    private Long amount;
    @ExcelProperty(value = "事实id", index = 4)
    private Long factId;
    @ExcelProperty(value = "事实类型", index = 5)
    private Integer factType;
    @ExcelProperty(value = "事实简介", index = 6)
    private String factMemo;
}