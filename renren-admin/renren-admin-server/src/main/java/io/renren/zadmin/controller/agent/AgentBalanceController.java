package io.renren.zadmin.controller.agent;

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
import io.renren.zapi.ZooConstant;
import io.renren.zadmin.dto.ZBalanceDTO;
import io.renren.zadmin.excel.ZBalanceExcel;
import io.renren.zadmin.service.ZBalanceService;
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
* z_withdraw
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/agent/balance")
@Tag(name = "zoo_agent_balance")
public class AgentBalanceController {
    @Resource
    private ZBalanceService zBalanceService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('zagent:zbalance:page')")
    public Result<PageData<ZBalanceDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        MyUserDetail user = SecurityUser.getUser();
        params.put("ownerType", ZooConstant.OWNER_TYPE_AGENT);
        params.put("ownerId", user.getId().toString());

        PageData<ZBalanceDTO> page = zBalanceService.page(params);

        return new Result<PageData<ZBalanceDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('zagent:zbalance:info')")
    public Result<ZBalanceDTO> get(@PathVariable("id") Long id){
        ZBalanceDTO data = zBalanceService.get(id);

        return new Result<ZBalanceDTO>().ok(data);
    }


    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('zagent:zbalance:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZBalanceDTO> list = zBalanceService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "z_withdraw", list, ZBalanceExcel.class);
    }

}