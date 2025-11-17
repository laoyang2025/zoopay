package io.renren.bledger.controller;

import io.renren.bledger.dto.BotChargeDTO;
import io.renren.bledger.excel.BotChargeExcel;
import io.renren.bledger.service.BotChargeService;
import io.renren.commons.log.annotation.LogOperation;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.ExcelUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.commons.tools.validator.AssertUtils;
import io.renren.commons.tools.validator.ValidatorUtils;
import io.renren.commons.tools.validator.group.AddGroup;
import io.renren.commons.tools.validator.group.DefaultGroup;
import io.renren.commons.tools.validator.group.UpdateGroup;
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
* 充值
*
* @author kernel kernel@qq.com
* @since 1.0 2024-06-02
*/
@RestController
@RequestMapping("botman/botcharge")
@Tag(name = "充值")
public class BotChargeController {
    @Resource
    private BotChargeService botChargeService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys/botman:botcharge:page')")
    public Result<PageData<BotChargeDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<BotChargeDTO> page = botChargeService.page(params);

        return new Result<PageData<BotChargeDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys/botman:botcharge:info')")
    public Result<BotChargeDTO> get(@PathVariable("id") Long id){
        BotChargeDTO data = botChargeService.get(id);

        return new Result<BotChargeDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys/botman:botcharge:save')")
    public Result save(@RequestBody BotChargeDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        botChargeService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys/botman:botcharge:update')")
    public Result update(@RequestBody BotChargeDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        botChargeService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys/botman:botcharge:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        botChargeService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys/botman:botcharge:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<BotChargeDTO> list = botChargeService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "充值", list, BotChargeExcel.class);
    }

}
