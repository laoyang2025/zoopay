package io.renren.zadmin.controller.org.balance;

import io.renren.commons.log.annotation.LogOperation;
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
import io.renren.zadmin.dao.ZBalanceDao;
import io.renren.zadmin.dao.ZWarningDao;
import io.renren.zadmin.dto.ZBalanceDTO;
import io.renren.zadmin.entity.ZBalanceEntity;
import io.renren.zadmin.entity.ZWarningEntity;
import io.renren.zadmin.excel.ZBalanceExcel;
import io.renren.zadmin.service.ZBalanceService;
import io.renren.zapi.ledger.ZLedger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
* 所有账户余额
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/org/balance")
@Tag(name = "zoo_org_balance")
public class ZBalanceController {
    @Resource
    private ZBalanceService zBalanceService;

    @Resource
    private ZLedger ledger;

    @Resource
    private ZBalanceDao zBalanceDao;

    @Resource
    private ZWarningDao zWarningDao;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zbalance:page')")
    public Result<PageData<ZBalanceDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ZBalanceDTO> page = zBalanceService.page(params);

        return new Result<PageData<ZBalanceDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zbalance:info')")
    public Result<ZBalanceDTO> get(@PathVariable("id") Long id){
        ZBalanceDTO data = zBalanceService.get(id);

        return new Result<ZBalanceDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zbalance:save')")
    public Result save(@RequestBody ZBalanceDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        zBalanceService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zbalance:update')")
    public Result update(@RequestBody ZBalanceDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zBalanceService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zbalance:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zBalanceService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zbalance:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZBalanceDTO> list = zBalanceService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "z_withdraw", list, ZBalanceExcel.class);
    }

    @GetMapping("adjust")
    @Operation(summary = "调账")
    @LogOperation("调账")
    @PreAuthorize("hasAuthority('sys:zbalance:update')")
    public Result adjust(@RequestParam("id") Long id, @RequestParam("adjust")BigDecimal adjust, @RequestParam("reason") String reason) throws Exception {

        ledger.adjust(id, adjust, reason);

        ZBalanceEntity zBalanceEntity = zBalanceDao.selectById(id);

        // 记录操作人记录
        ZWarningEntity warningEntity = new ZWarningEntity();
        warningEntity.setDeptId(zBalanceEntity.getDeptId());
        warningEntity.setDeptName(zBalanceEntity.getDeptName());
        warningEntity.setMsgType("operation");
        String username = SecurityUser.getUser().getUsername();
        String msg = "调整余额, 调整金额:" + adjust + ", 原因:" + reason + ", 操作员:" + username;
        warningEntity.setMsg(msg);
        zWarningDao.insert(warningEntity);

        return Result.ok;
    }

}