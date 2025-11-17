package io.renren.zadmin.controller.org.balance;

import io.renren.commons.log.annotation.LogOperation;
import io.renren.commons.security.user.MyUserDetail;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.Result;
import io.renren.commons.tools.utils.ExcelUtils;
import io.renren.commons.tools.validator.AssertUtils;
import io.renren.commons.tools.validator.ValidatorUtils;
import io.renren.commons.tools.validator.group.AddGroup;
import io.renren.commons.tools.validator.group.DefaultGroup;
import io.renren.commons.tools.validator.group.UpdateGroup;
import io.renren.zadmin.dto.ZLogDTO;
import io.renren.zadmin.excel.ZLogExcel;
import io.renren.zadmin.service.ZLogService;
import io.renren.zapi.ZConfig;
import io.renren.zapi.ZooConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * 所有账变流水
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@RestController
@RequestMapping("zoo/org/log")
@Tag(name = "zoo_org_log")
@Slf4j
public class ZLogController {
    @Resource
    private ZLogService zLogService;

    @Resource
    private ZConfig zConfig;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zlog:page')")
    public Result<PageData<ZLogDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<ZLogDTO> page = zLogService.page(params);

        return new Result<PageData<ZLogDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zlog:info')")
    public Result<ZLogDTO> get(@PathVariable("id") Long id) {
        ZLogDTO data = zLogService.get(id);

        return new Result<ZLogDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zlog:save')")
    public Result save(@RequestBody ZLogDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        zLogService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zlog:update')")
    public Result update(@RequestBody ZLogDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zLogService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zlog:delete')")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zLogService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zlog:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        MyUserDetail user = SecurityUser.getUser();

        // 如果是商户下载的话
        if (ZooConstant.USER_TYPE_MERCHANT.equals(user.getUserType())) {
            params.put("ownerId", user.getId());
        }
        List<ZLogDTO> list = zLogService.list(params);

        for (ZLogDTO zLogDTO : list) {
            Date date = DateUtils.addMinutes(zLogDTO.getCreateDate(), zConfig.getTzMinutes());
            zLogDTO.setCreateDate(date);
        }

        ExcelUtils.exportExcelToTarget(response, null, "账变流水", list, ZLogExcel.class);
    }

}