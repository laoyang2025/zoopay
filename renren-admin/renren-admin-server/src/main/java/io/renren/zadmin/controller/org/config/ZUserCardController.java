package io.renren.zadmin.controller.org.config;

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
import io.renren.dto.SysUserDTO;
import io.renren.service.SysUserService;
import io.renren.zadmin.dto.ZUserCardDTO;
import io.renren.zadmin.excel.ZUserCardExcel;
import io.renren.zadmin.service.ZUserCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
* 卡主卡片
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/org/usercard")
@Tag(name = "zoo_org_usercard")
public class ZUserCardController {
    @Resource
    private ZUserCardService zUserCardService;
    @Resource
    private SysUserService sysUserService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zusercard:page')")
    public Result<PageData<ZUserCardDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ZUserCardDTO> page = zUserCardService.page(params);

        return new Result<PageData<ZUserCardDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zusercard:info')")
    public Result<ZUserCardDTO> get(@PathVariable("id") Long id){
        ZUserCardDTO data = zUserCardService.get(id);

        return new Result<ZUserCardDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zusercard:save')")
    public Result save(@RequestBody ZUserCardDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        zUserCardService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zusercard:update')")
    public Result update(@RequestBody ZUserCardDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zUserCardService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zusercard:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zUserCardService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zusercard:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZUserCardDTO> list = zUserCardService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "z_user_card", list, ZUserCardExcel.class);
    }


    // 代理列表
    @GetMapping("/agents")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zusercard:info')")
    public Result<List<SysUserDTO>> getAgentList() {
        Map<String, Object> params = new HashMap<>();
        params.put("userType", "agent");
        List<SysUserDTO> list = sysUserService.list(params);
        Result<List<SysUserDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }

}