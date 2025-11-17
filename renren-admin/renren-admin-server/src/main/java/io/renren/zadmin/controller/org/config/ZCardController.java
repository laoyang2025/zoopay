package io.renren.zadmin.controller.org.config;

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
import io.renren.zadmin.dto.ZCardDTO;
import io.renren.zadmin.excel.ZCardExcel;
import io.renren.zadmin.service.ZCardService;
import io.renren.zapi.ZConfig;
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
 * 自营卡
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@RestController
@RequestMapping("zoo/org/card")
@Tag(name = "zoo_org_card")
public class ZCardController {
    @Resource
    private ZCardService zCardService;
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
    @PreAuthorize("hasAuthority('sys:zcard:page')")
    public Result<PageData<ZCardDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<ZCardDTO> page = zCardService.page(params);

        return new Result<PageData<ZCardDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zcard:info')")
    public Result<ZCardDTO> get(@PathVariable("id") Long id) {
        ZCardDTO data = zCardService.get(id);

        return new Result<ZCardDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zcard:save')")
    public Result save(@RequestBody ZCardDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);
        dto.setDeptName(SecurityUser.getUser().getDeptName());

        zCardService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zcard:update')")
    public Result update(@RequestBody ZCardDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zCardService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zcard:delete')")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");
        zCardService.delete(ids);
        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zcard:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZCardDTO> list = zCardService.list(params);
        ExcelUtils.exportExcelToTarget(response, null, "z_card", list, ZCardExcel.class);
    }

    @GetMapping("pushbullet")
    @Operation(summary = "pushbullet")
    @LogOperation("pushbullet")
    @PreAuthorize("hasAuthority('sys:zcard:info')")
    public Result<List<String>> pushbullet() {
        MyUserDetail user = SecurityUser.getUser();
        Long deptId = user.getDeptId();
        Map<String, List<String>> pushbullets = zConfig.getPushbullets();
        List<String> keyList = pushbullets.get(deptId.toString());
        Result<List<String>> result = new Result<>();
        result.setData(keyList);
        return result;
    }
}