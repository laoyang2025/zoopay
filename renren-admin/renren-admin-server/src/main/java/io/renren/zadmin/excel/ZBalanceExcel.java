package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * z_withdraw
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZBalanceExcel {
    @ExcelProperty(value = "归属方", index = 0)
    private Long parentId;
    @ExcelProperty(value = "账户类型", index = 1)
    private String ownerType;
    @ExcelProperty(value = "账户id", index = 2)
    private Long ownerId;
    @ExcelProperty(value = "账户名称", index = 3)
    private String ownerName;
    @ExcelProperty(value = "余额", index = 4)
    private Long balance;
    @ExcelProperty(value = "版本", index = 5)
    private Long version;
    @ExcelProperty(value = "创建时间", index = 6)
    private Date createDate;
    @ExcelProperty(value = "更新时间", index = 7)
    private Date updateDate;
}