package io.renren.zadmin.controller.ant;

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
import io.renren.zadmin.dto.ZAntLogDTO;
import io.renren.zadmin.excel.ZAntLogExcel;
import io.renren.zadmin.service.ZAntLogService;
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
* z_ant_log
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/ant/antlog")
@Tag(name = "zoo_ant_antlog")
public class AntLogController {
    @Resource
    private ZAntLogService zAntLogService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('zant:zantlog:page')")
    public Result<PageData<ZAntLogDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        MyUserDetail user = SecurityUser.getUser();
        params.put("antId", user.getId().toString());
        PageData<ZAntLogDTO> page = zAntLogService.page(params);

        return new Result<PageData<ZAntLogDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('zant:zantlog:info')")
    public Result<ZAntLogDTO> get(@PathVariable("id") Long id){
        ZAntLogDTO data = zAntLogService.get(id);

        return new Result<ZAntLogDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('zant:zantlog:save')")
    public Result save(@RequestBody ZAntLogDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        zAntLogService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('zant:zantlog:update')")
    public Result update(@RequestBody ZAntLogDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zAntLogService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('zant:zantlog:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zAntLogService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('zant:zantlog:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZAntLogDTO> list = zAntLogService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "z_ant_log", list, ZAntLogExcel.class);
    }

}