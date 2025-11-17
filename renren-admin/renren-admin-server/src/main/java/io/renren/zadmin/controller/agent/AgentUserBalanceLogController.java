package io.renren.zadmin.controller.agent;

import io.renren.commons.log.annotation.LogOperation;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.ExcelUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.commons.tools.validator.AssertUtils;
import io.renren.commons.tools.validator.ValidatorUtils;
import io.renren.commons.tools.validator.group.AddGroup;
import io.renren.commons.tools.validator.group.DefaultGroup;
import io.renren.commons.tools.validator.group.UpdateGroup;
import io.renren.zadmin.dto.ZLogDTO;
import io.renren.zadmin.excel.ZLogExcel;
import io.renren.zadmin.service.ZLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
* 卡主余额流水
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/agent/userbalancelog")
@Tag(name = "zoo_agent_userbalancelog")
public class AgentUserBalanceLogController {
    @Resource
    private ZLogService zLogService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('zagent:zlog:page')")
    public Result<PageData<ZLogDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ZLogDTO> page = zLogService.page(params);

        return new Result<PageData<ZLogDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('zagent:zlog:info')")
    public Result<ZLogDTO> get(@PathVariable("id") Long id){
        ZLogDTO data = zLogService.get(id);

        return new Result<ZLogDTO>().ok(data);
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('zagent:zlog:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZLogDTO> list = zLogService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "z_log", list, ZLogExcel.class);
    }

}