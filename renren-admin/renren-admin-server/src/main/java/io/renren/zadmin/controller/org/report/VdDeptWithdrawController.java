package io.renren.zadmin.controller.org.report;

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
import io.renren.zadmin.dto.VdDeptWithdrawDTO;
import io.renren.zadmin.excel.VdDeptWithdrawExcel;
import io.renren.zadmin.service.VdDeptWithdrawService;
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
* VIEW
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-09-10
*/
@RestController
@RequestMapping("zoo/org/vddeptwithdraw")
@Tag(name = "VIEW")
public class VdDeptWithdrawController {
    @Resource
    private VdDeptWithdrawService vdDeptWithdrawService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:vddeptwithdraw:page')")
    public Result<PageData<VdDeptWithdrawDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<VdDeptWithdrawDTO> page = vdDeptWithdrawService.page(params);

        return new Result<PageData<VdDeptWithdrawDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:vddeptwithdraw:info')")
    public Result<VdDeptWithdrawDTO> get(@PathVariable("id") Long id){
        VdDeptWithdrawDTO data = vdDeptWithdrawService.get(id);

        return new Result<VdDeptWithdrawDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:vddeptwithdraw:save')")
    public Result save(@RequestBody VdDeptWithdrawDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        vdDeptWithdrawService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:vddeptwithdraw:update')")
    public Result update(@RequestBody VdDeptWithdrawDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        vdDeptWithdrawService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:vddeptwithdraw:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        vdDeptWithdrawService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:vddeptwithdraw:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        params.put(Constant.LIMIT, "1000000");
        List<VdDeptWithdrawDTO> list = vdDeptWithdrawService.page(params).getList();
        ExcelUtils.exportExcelToTarget(response, null, "VIEW", list, VdDeptWithdrawExcel.class);
    }

}