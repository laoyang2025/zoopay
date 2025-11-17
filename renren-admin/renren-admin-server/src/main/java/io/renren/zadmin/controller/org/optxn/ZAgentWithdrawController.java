package io.renren.zadmin.controller.org.optxn;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.log.annotation.LogOperation;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.Result;
import io.renren.commons.tools.utils.ExcelUtils;
import io.renren.commons.tools.validator.AssertUtils;
import io.renren.commons.tools.validator.ValidatorUtils;
import io.renren.commons.tools.validator.group.AddGroup;
import io.renren.commons.tools.validator.group.DefaultGroup;
import io.renren.commons.tools.validator.group.UpdateGroup;
import io.renren.zadmin.dto.ZAgentWithdrawDTO;
import io.renren.zadmin.entity.ZAgentChargeEntity;
import io.renren.zadmin.excel.ZAgentWithdrawExcel;
import io.renren.zadmin.service.ZAgentWithdrawService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
* 代理提现
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/org/agentwithdraw")
@Tag(name = "zoo_org_agentwithdraw")
public class ZAgentWithdrawController {
    @Resource
    private ZAgentWithdrawService zAgentWithdrawService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zagentwithdraw:page')")
    public Result<PageData<ZAgentWithdrawDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ZAgentWithdrawDTO> page = zAgentWithdrawService.page(params);

        return new Result<PageData<ZAgentWithdrawDTO>>().ok(page);
    }


    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zagentwithdraw:info')")
    public Result<ZAgentWithdrawDTO> get(@PathVariable("id") Long id){
        ZAgentWithdrawDTO data = zAgentWithdrawService.get(id);

        return new Result<ZAgentWithdrawDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zagentwithdraw:save')")
    public Result save(@RequestBody ZAgentWithdrawDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        zAgentWithdrawService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zagentwithdraw:update')")
    public Result update(@RequestBody ZAgentWithdrawDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zAgentWithdrawService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zagentwithdraw:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zAgentWithdrawService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zagentwithdraw:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZAgentWithdrawDTO> list = zAgentWithdrawService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "z_agent_withdraw", list, ZAgentWithdrawExcel.class);
    }

    ///////////////////////////

    @PreAuthorize("hasAuthority('sys:zagentwithdraw:verify')")
    @GetMapping("verify")
    public Result verify(@RequestParam("id") Long id){
        return null;
    }


}