package io.renren.zadmin.controller.merchant;

import io.renren.commons.log.annotation.LogOperation;
import io.renren.commons.security.user.MyUserDetail;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.ExcelUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.commons.tools.validator.AssertUtils;
import io.renren.commons.tools.validator.ValidatorUtils;
import io.renren.commons.tools.validator.group.AddGroup;
import io.renren.commons.tools.validator.group.DefaultGroup;
import io.renren.commons.tools.validator.group.UpdateGroup;
import io.renren.zadmin.dto.ZChargeDTO;
import io.renren.zadmin.dto.ZLogDTO;
import io.renren.zadmin.excel.ZChargeExcel;
import io.renren.zadmin.service.ZChargeService;
import io.renren.zapi.ZConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
* 商户充值
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/merchant/charge")
@Tag(name = "zoo_merchant_charge")
public class MerchantChargeController {
    @Resource
    private ZChargeService zChargeService;
    @Resource
    private ZConfig zConfig;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('merchant:zcharge:page')")
    public Result<PageData<ZChargeDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        MyUserDetail user = SecurityUser.getUser();
        params.put("merchantId", user.getId().toString());
        PageData<ZChargeDTO> page = zChargeService.page(params);

        return new Result<PageData<ZChargeDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('merchant:zcharge:info')")
    public Result<ZChargeDTO> get(@PathVariable("id") Long id){
        ZChargeDTO data = zChargeService.get(id);
        return new Result<ZChargeDTO>().ok(data);
    }


    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('merchant:zcharge:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        MyUserDetail user = SecurityUser.getUser();
        params.put("merchantId", user.getId().toString());
        List<ZChargeDTO> list = zChargeService.list(params);

        for (ZChargeDTO dto : list) {
            Date createDate = DateUtils.addMinutes(dto.getCreateDate(), zConfig.getTzMinutes());
            dto.setCreateDate(createDate);
            if(dto.getNotifyTime() != null) {
                Date notifyDate = DateUtils.addMinutes(dto.getNotifyTime(), zConfig.getTzMinutes());
                dto.setNotifyTime(notifyDate);
            }
        }

        ExcelUtils.exportExcelToTarget(response, null, "z_charge", list, ZChargeExcel.class);
    }

}