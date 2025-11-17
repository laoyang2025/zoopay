package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.util.Date;

/**
 * 机器人账号
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-18
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZBotExcel {
    @ExcelProperty(value = "聊天群", index = 0)
    private String chatId;
    @ExcelProperty(value = "服务ID", index = 1)
    private Long serveId;
    @ExcelProperty(value = "服务名", index = 2)
    private String serveName;
    @ExcelProperty(value = "服务类型", index = 3)
    private String serveType;
    @ExcelProperty(value = "创建时间", index = 4)
    private Date createDate;
}