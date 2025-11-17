package io.renren.zadmin.controller.org.optxn;

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
import io.renren.zadmin.dto.ZUserWithdrawDTO;
import io.renren.zadmin.excel.ZUserWithdrawExcel;
import io.renren.zadmin.service.ZUserWithdrawService;
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
* 卡主提现
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/org/userwithdraw")
@Tag(name = "zoo_org_userwithdraw")
public class ZUserWithdrawController {
    @Resource
    private ZUserWithdrawService zUserWithdrawService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zuserwithdraw:page')")
    public Result<PageData<ZUserWithdrawDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ZUserWithdrawDTO> page = zUserWithdrawService.page(params);

        return new Result<PageData<ZUserWithdrawDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zuserwithdraw:info')")
    public Result<ZUserWithdrawDTO> get(@PathVariable("id") Long id){
        ZUserWithdrawDTO data = zUserWithdrawService.get(id);

        return new Result<ZUserWithdrawDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zuserwithdraw:save')")
    public Result save(@RequestBody ZUserWithdrawDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        zUserWithdrawService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zuserwithdraw:update')")
    public Result update(@RequestBody ZUserWithdrawDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zUserWithdrawService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zuserwithdraw:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zUserWithdrawService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zuserwithdraw:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZUserWithdrawDTO> list = zUserWithdrawService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "z_user_withdraw", list, ZUserWithdrawExcel.class);
    }

    @PreAuthorize("hasAuthority('sys:zuserwithdraw:verify')")
    @GetMapping("verify")
    public Result verify(@RequestParam("id") Long id){
        return null;
    }
}