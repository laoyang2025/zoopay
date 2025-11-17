package io.renren.zadmin.controller.org.history;

import io.renren.commons.log.annotation.LogOperation;
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
import io.renren.zadmin.dto.ZCardDTO;
import io.renren.zadmin.dto.ZCardLogHisDTO;
import io.renren.zadmin.excel.ZCardLogExcel;
import io.renren.zadmin.service.ZCardLogHisService;
import io.renren.zadmin.service.ZCardService;
import io.renren.zapi.card.CardMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
* 自营卡银行流水
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/org/cardloghis")
@Tag(name = "zoo_org_cardloghis")
public class ZCardLogHisController {
    @Resource
    private ZCardLogHisService zCardLogHisService;
    @Resource
    private ZCardService   zCardService;
    @Resource
    private CardMatchService cardMatchService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zcardloghis:page')")
    public Result<PageData<ZCardLogHisDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ZCardLogHisDTO> page = zCardLogHisService.page(params);

        return new Result<PageData<ZCardLogHisDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zcardloghis:info')")
    public Result<ZCardLogHisDTO> get(@PathVariable("id") Long id){
        ZCardLogHisDTO data = zCardLogHisService.get(id);

        return new Result<ZCardLogHisDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zcardloghis:save')")
    public Result save(@RequestBody ZCardLogHisDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);
        dto.setDeptName(SecurityUser.getUser().getDeptName());
        zCardLogHisService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zcardloghis:update')")
    public Result update(@RequestBody ZCardLogHisDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zCardLogHisService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zcardloghis:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zCardLogHisService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zcardloghis:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZCardLogHisDTO> list = zCardLogHisService.list(params);
        ExcelUtils.exportExcelToTarget(response, null, "z_card_log", list, ZCardLogExcel.class);
    }

    @GetMapping("cardList")
    @Operation(summary = "商户列表")
    @LogOperation("商户列表")
    @PreAuthorize("hasAuthority('sys:zcardloghis:info')")

    public Result<List<ZCardDTO>> cardList() {
        Result<List<ZCardDTO>> result = new Result<>();
        List<ZCardDTO> list = zCardService.list(new HashMap<>());
        result.setData(list);
        return result;
    }

    /**
     * 上传银行流水
     */
    @GetMapping("upload")
    @Operation(summary = "上传流水")
    @LogOperation("商户列表")
    @PreAuthorize("hasAuthority('sys:zcardloghis:update')")
    private Result uploadFile(@RequestParam("log") String log, @RequestParam("cardId") Long cardId) {
//        cardMatchService.matchFile(cardId, log);
        return Result.ok;
    }
}