package io.renren.zadmin.controller.org.optxn;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import io.renren.zadmin.dao.ZAgentChargeDao;
import io.renren.zadmin.dto.ZAgentChargeDTO;
import io.renren.zadmin.entity.ZAgentChargeEntity;
import io.renren.zadmin.excel.ZAgentChargeExcel;
import io.renren.zadmin.service.ZAgentChargeService;
import io.renren.zapi.ledger.AgentLedger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.Resource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
* 代理充值
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/org/agentcharge")
@Tag(name = "zoo_org_agentcharge")
public class ZAgentChargeController {
    @Resource
    private ZAgentChargeService zAgentChargeService;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ZAgentChargeDao zAgentChargeDao;
    @Resource
    private AgentLedger agentLedger;


    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zagentcharge:page')")
    public Result<PageData<ZAgentChargeDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ZAgentChargeDTO> page = zAgentChargeService.page(params);

        return new Result<PageData<ZAgentChargeDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zagentcharge:info')")
    public Result<ZAgentChargeDTO> get(@PathVariable("id") Long id){
        ZAgentChargeDTO data = zAgentChargeService.get(id);

        return new Result<ZAgentChargeDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zagentcharge:save')")
    public Result save(@RequestBody ZAgentChargeDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        zAgentChargeService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zagentcharge:update')")
    public Result update(@RequestBody ZAgentChargeDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zAgentChargeService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zagentcharge:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zAgentChargeService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zagentcharge:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZAgentChargeDTO> list = zAgentChargeService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "z_agent_charge", list, ZAgentChargeExcel.class);
    }

    //////////////////////////////////////////////////////
    // 代理充值后, 运营后台查看处理
    //////////////////////////////////////////////////////

    @PreAuthorize("hasAuthority('sys:zagentcharge:verify')")
    @GetMapping("verify")
    public Result verify(@RequestParam("id") Long id){
        ZAgentChargeEntity zAgentChargeEntity = zAgentChargeDao.selectById(id);
        if (zAgentChargeEntity == null) {
            return Result.fail(9999, "invalid id");
        }
        tx.executeWithoutResult(status -> {
            zAgentChargeDao.update(null, Wrappers.<ZAgentChargeEntity>lambdaUpdate()
                    .eq(ZAgentChargeEntity::getId, id)
                    .set(ZAgentChargeEntity::getProcessStatus, 1)
            );


            agentLedger.agentAgentChargeSuccess(zAgentChargeEntity);
        });
        return Result.ok;
    }

}