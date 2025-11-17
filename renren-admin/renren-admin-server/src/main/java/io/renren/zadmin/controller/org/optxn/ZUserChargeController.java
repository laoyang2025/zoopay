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
import io.renren.zadmin.dao.ZUserChargeDao;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.dto.ZUserChargeDTO;
import io.renren.zadmin.entity.ZUserChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zadmin.excel.ZUserChargeExcel;
import io.renren.zadmin.service.ZUserChargeService;
import io.renren.zapi.ledger.AgentLedger;
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
* 卡主充值
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/org/usercharge")
@Tag(name = "zoo_org/usercharge")
public class ZUserChargeController {
    @Resource
    private ZUserChargeService zUserChargeService;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ZWithdrawDao zWithdrawDao;
    @Resource
    private ZUserChargeDao zUserChargeDao;
    @Resource
    private AgentLedger agentLedger;
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
    @PreAuthorize("hasAuthority('sys:zusercharge:page')")
    public Result<PageData<ZUserChargeDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ZUserChargeDTO> page = zUserChargeService.page(params);

        return new Result<PageData<ZUserChargeDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zusercharge:info')")
    public Result<ZUserChargeDTO> get(@PathVariable("id") Long id){
        ZUserChargeDTO data = zUserChargeService.get(id);

        return new Result<ZUserChargeDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zusercharge:save')")
    public Result save(@RequestBody ZUserChargeDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        zUserChargeService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zusercharge:update')")
    public Result update(@RequestBody ZUserChargeDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zUserChargeService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zusercharge:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zUserChargeService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zusercharge:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZUserChargeDTO> list = zUserChargeService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "z_user_charge", list, ZUserChargeExcel.class);
    }


    /**
     * 卡主充值通过
     * @param id
     * @return
     */
    @PreAuthorize("hasAuthority('sys:zusercharge:verify')")
    @GetMapping("verify")
    public Result verify(@RequestParam("id") Long id){

        ZUserChargeEntity zUserChargeEntity = zUserChargeDao.selectById(id);
        if (zUserChargeEntity == null) {
            return Result.fail(9999, "invalid id");
        }
        tx.executeWithoutResult(status -> {
            zUserChargeDao.update(null, Wrappers.<ZUserChargeEntity>lambdaUpdate()
                    .eq(ZUserChargeEntity::getId, id)
                    .set(ZUserChargeEntity::getProcessStatus, 1)
            );
            // 是否为代付抢单充值
            if (zUserChargeEntity.getWithdrawId() != null) {
                ZWithdrawEntity zWithdrawEntity = zWithdrawDao.selectById(zUserChargeEntity.getWithdrawId());
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
            else if (zUserChargeEntity.getBasketId() != null) {
                //
            } else {
                throw new RenException("invalid user charge");
            }

            agentLedger.userUserChargeSuccess(zUserChargeEntity);
        });
        return Result.ok;
    }

}