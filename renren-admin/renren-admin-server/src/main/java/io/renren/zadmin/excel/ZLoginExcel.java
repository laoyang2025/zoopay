package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * z_login
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZLoginExcel {
    @ExcelProperty(value = "Long", index = 0)
    private Long ownerId;
    @ExcelProperty(value = "用户所属", index = 1)
    private String ownerName;
    @ExcelProperty(value = "用户类型", index = 2)
    private String userType;
    @ExcelProperty(value = "用户id", index = 3)
    private Long userId;
    @ExcelProperty(value = "用户名", index = 4)
    private String username;
    @ExcelProperty(value = "登录IP", index = 5)
    private String loginIp;
    @ExcelProperty(value = "创建者", index = 6)
    private Long creator;
    @ExcelProperty(value = "创建时间", index = 7)
    private Date createDate;
}