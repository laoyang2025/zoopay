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
import io.renren.zadmin.dto.VhMerchantDTO;
import io.renren.zadmin.excel.VhMerchantExcel;
import io.renren.zadmin.service.VhMerchantService;
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
@RequestMapping("zoo/org/vhmerchant")
@Tag(name = "VIEW")
public class VhMerchantController {
    @Resource
    private VhMerchantService vhMerchantService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:vhmerchant:page')")
    public Result<PageData<VhMerchantDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<VhMerchantDTO> page = vhMerchantService.page(params);

        return new Result<PageData<VhMerchantDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:vhmerchant:info')")
    public Result<VhMerchantDTO> get(@PathVariable("id") Long id){
        VhMerchantDTO data = vhMerchantService.get(id);

        return new Result<VhMerchantDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:vhmerchant:save')")
    public Result save(@RequestBody VhMerchantDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        vhMerchantService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:vhmerchant:update')")
    public Result update(@RequestBody VhMerchantDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        vhMerchantService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:vhmerchant:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        vhMerchantService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:vhmerchant:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        params.put(Constant.LIMIT, "1000000");
        List<VhMerchantDTO> list = vhMerchantService.page(params).getList();

        ExcelUtils.exportExcelToTarget(response, null, "VIEW", list, VhMerchantExcel.class);
    }

}