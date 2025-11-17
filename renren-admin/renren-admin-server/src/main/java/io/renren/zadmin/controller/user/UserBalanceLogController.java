package io.renren.zadmin.controller.user;

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
* 卡主账变记录
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/user/balancelog")
@Tag(name = "zoo_user_balancelog")
public class UserBalanceLogController {
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
    @PreAuthorize("hasAuthority('zuser:zlog:page')")
    public Result<PageData<ZLogDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        MyUserDetail user = SecurityUser.getUser();
        params.put("ownerType", ZooConstant.OWNER_TYPE_USER);
        params.put("ownerId", user.getId().toString());
        PageData<ZLogDTO> page = zLogService.page(params);

        return new Result<PageData<ZLogDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('zuser:zlog:info')")
    public Result<ZLogDTO> get(@PathVariable("id") Long id){
        ZLogDTO data = zLogService.get(id);

        return new Result<ZLogDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('zuser:zlog:save')")
    public Result save(@RequestBody ZLogDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        zLogService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('zuser:zlog:update')")
    public Result update(@RequestBody ZLogDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zLogService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('zuser:zlog:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zLogService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('zuser:zlog:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZLogDTO> list = zLogService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "z_log", list, ZLogExcel.class);
    }

}