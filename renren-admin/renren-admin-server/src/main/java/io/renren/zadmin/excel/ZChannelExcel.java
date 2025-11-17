package io.renren.zadmin.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * z_channel
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@Data
@ContentRowHeight(20)
@HeadRowHeight(20)
@ColumnWidth(25)
public class ZChannelExcel {
    @ExcelProperty(value = "收款启用", index = 0)
    private Integer chargeEnabled;
    @ExcelProperty(value = "代付启用", index = 1)
    private Integer withdrawEnabled;
    @ExcelProperty(value = "渠道名", index = 2)
    private String channelName;
    @ExcelProperty(value = "展示名", index = 3)
    private String channelLabel;
    @ExcelProperty(value = "充值扣率", index = 4)
    private BigDecimal chargeRate;
    @ExcelProperty(value = "提现扣率", index = 5)
    private BigDecimal withdrawRate;
    @ExcelProperty(value = "提现定额", index = 6)
    private Long withdrawFix;
    @ExcelProperty(value = "接入商户号", index = 7)
    private String merchantId;
    @ExcelProperty(value = "通道编码", index = 8)
    private String payCode;
    @ExcelProperty(value = "余额详情", index = 9)
    private String balanceMemo;
    @ExcelProperty(value = "收款地址", index = 10)
    private String chargeUrl;
    @ExcelProperty(value = "代付地址", index = 11)
    private String withdrawUrl;
    @ExcelProperty(value = "收款查询", index = 12)
    private String chargeQueryUrl;
    @ExcelProperty(value = "代付查询", index = 13)
    private String withdrawQueryUrl;
    @ExcelProperty(value = "余额查询", index = 14)
    private String balanceUrl;
    @ExcelProperty(value = "白名单", index = 15)
    private String whiteIp;
    @ExcelProperty(value = "创建时间", index = 16)
    private Date createDate;
}