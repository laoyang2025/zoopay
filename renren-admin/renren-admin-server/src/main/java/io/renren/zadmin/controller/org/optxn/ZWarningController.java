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
import io.renren.zadmin.dto.ZWarningDTO;
import io.renren.zadmin.excel.ZWarningExcel;
import io.renren.zadmin.service.ZWarningService;
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
* z_warning
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-15
*/
@RestController
@RequestMapping("zoo/org/warning")
@Tag(name = "zoo_org_warning")
public class ZWarningController {
    @Resource
    private ZWarningService zWarningService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zwarning:page')")
    public Result<PageData<ZWarningDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ZWarningDTO> page = zWarningService.page(params);
        return new Result<PageData<ZWarningDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zwarning:info')")
    public Result<ZWarningDTO> get(@PathVariable("id") Long id){
        ZWarningDTO data = zWarningService.get(id);
        return new Result<ZWarningDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zwarning:save')")
    public Result save(@RequestBody ZWarningDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);
        zWarningService.save(dto);
        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zwarning:update')")
    public Result update(@RequestBody ZWarningDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);
        zWarningService.update(dto);
        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zwarning:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");
        zWarningService.delete(ids);
        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zwarning:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZWarningDTO> list = zWarningService.list(params);
        ExcelUtils.exportExcelToTarget(response, null, "z_warning", list, ZWarningExcel.class);
    }

}