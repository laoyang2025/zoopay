package io.renren.zadmin.controller.org.optxn;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.log.annotation.LogOperation;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.Result;
import io.renren.commons.tools.utils.ExcelUtils;
import io.renren.commons.tools.validator.AssertUtils;
import io.renren.commons.tools.validator.ValidatorUtils;
import io.renren.commons.tools.validator.group.AddGroup;
import io.renren.commons.tools.validator.group.DefaultGroup;
import io.renren.commons.tools.validator.group.UpdateGroup;
import io.renren.zadmin.dao.ZAntChargeDao;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.dto.ZAntChargeDTO;
import io.renren.zadmin.entity.ZAntChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zadmin.excel.ZAntChargeExcel;
import io.renren.zadmin.service.ZAntChargeService;
import io.renren.zapi.ledger.AntLedger;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.event.WithdrawCompleteEvent;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.Resource;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
* 码农充值
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/org/antcharge")
@Tag(name = "zoo_org_antcharge")
public class ZAntChargeController {
    @Resource
    private ZAntChargeService zAntChargeService;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ZAntChargeDao zAntChargeDao;
    @Resource
    private ZWithdrawDao zWithdrawDao;
    @Resource
    private AntLedger antLedger;
    @Resource
    private ApplicationEventPublisher publisher;


    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zantcharge:page')")
    public Result<PageData<ZAntChargeDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ZAntChargeDTO> page = zAntChargeService.page(params);

        return new Result<PageData<ZAntChargeDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zantcharge:info')")
    public Result<ZAntChargeDTO> get(@PathVariable("id") Long id){
        ZAntChargeDTO data = zAntChargeService.get(id);

        return new Result<ZAntChargeDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zantcharge:save')")
    public Result save(@RequestBody ZAntChargeDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        zAntChargeService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zantcharge:update')")
    public Result update(@RequestBody ZAntChargeDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zAntChargeService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zantcharge:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zAntChargeService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zantcharge:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZAntChargeDTO> list = zAntChargeService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "z_charge", list, ZAntChargeExcel.class);
    }


    /**
     * 码农充值通过
     * @param id
     * @return
     */
    @PreAuthorize("hasAuthority('sys:zantcharge:verify')")
    @GetMapping("verify")
    public Result verify(@RequestParam("id") Long id){
        ZAntChargeEntity zAntChargeEntity = zAntChargeDao.selectById(id);
        if (zAntChargeEntity == null) {
            return Result.fail(9999, "invalid id");
        }
        tx.executeWithoutResult(status -> {
            zAntChargeDao.update(null, Wrappers.<ZAntChargeEntity>lambdaUpdate()
                    .eq(ZAntChargeEntity::getId, id)
                    .set(ZAntChargeEntity::getProcessStatus, 1)
            );
            // 是否为代付抢单充值
            if (zAntChargeEntity.getWithdrawId() != null) {
                ZWithdrawEntity zWithdrawEntity = zWithdrawDao.selectById(zAntChargeEntity.getWithdrawId());
                // 更新代付状态， 同时通知商户
                zWithdrawDao.update(null, Wrappers.<ZWithdrawEntity>lambdaUpdate()
                        .eq(ZWithdrawEntity::getClaimed, 1)
                        .eq(ZWithdrawEntity::getId, zWithdrawEntity.getId())
                        .set(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_SUCCESS)
                        .set(ZWithdrawEntity::getProcessStatus, ZooConstant.WITHDRAW_STATUS_ASSIGNED)
                );
                // 通知商户
                publisher.publishEvent(new WithdrawCompleteEvent(null, zWithdrawEntity.getId()));
            }
            // 如果是公户充值
            else if (zAntChargeEntity.getBasketId() != null) {
                //
            } else {
                throw new RenException("invalid ant charge");
            }

            antLedger.antAntChargeSuccess(zAntChargeEntity);
        });
        return Result.ok;
    }

}