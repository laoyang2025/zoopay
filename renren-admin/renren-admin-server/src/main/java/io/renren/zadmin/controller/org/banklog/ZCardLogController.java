package io.renren.zadmin.controller.org.banklog;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import io.renren.dto.SysUserDTO;
import io.renren.zadmin.dao.ZCardDao;
import io.renren.zadmin.dao.ZChargeDao;
import io.renren.zadmin.dto.ZCardDTO;
import io.renren.zadmin.dto.ZCardLogDTO;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.excel.ZCardLogExcel;
import io.renren.zadmin.service.ZCardLogService;
import io.renren.zadmin.service.ZCardService;
import io.renren.zapi.card.CardMatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Wrapper;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
* 自营卡银行流水
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/org/cardlog")
@Tag(name = "zoo_org_cardlog")
public class ZCardLogController {
    @Resource
    private ZCardLogService zCardLogService;
    @Resource
    private ZCardService   zCardService;
    @Resource
    private CardMatchService cardMatchService;
    @Resource
    private ZChargeDao zChargeDao;


    private void attachRealAmount(List<ZCardLogDTO> list) {
        List<Long> idList = list.stream().map(ZCardLogDTO::getChargeId).filter(i -> i != null).toList();

        if (idList.size() == 0) {
            return;
        }

        Map<Long, ZChargeEntity> collect = zChargeDao.selectList(Wrappers.<ZChargeEntity>lambdaQuery()
                .in(ZChargeEntity::getId, idList)
                .select(
                        ZChargeEntity::getRealAmount,
                        ZChargeEntity::getId,
                        ZChargeEntity::getMerchantName,
                        ZChargeEntity::getOrderId
                )
        ).stream().collect(Collectors.toMap(ZChargeEntity::getId, e -> e));

        for (ZCardLogDTO zCardLogDTO : list) {
            if (zCardLogDTO.getChargeId() != null) {
                ZChargeEntity charge = collect.get(zCardLogDTO.getChargeId());
                zCardLogDTO.setRealAmount(charge.getRealAmount());
                zCardLogDTO.setOrderInfo(charge.getMerchantName() + "-" + charge.getOrderId());
            }
        }

    }

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zcardlog:page')")
    public Result<PageData<ZCardLogDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<ZCardLogDTO> page = zCardLogService.page(params);
        List<ZCardLogDTO> list = page.getList();
        attachRealAmount(list);
        return new Result<PageData<ZCardLogDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zcardlog:info')")
    public Result<ZCardLogDTO> get(@PathVariable("id") Long id){
        ZCardLogDTO data = zCardLogService.get(id);

        return new Result<ZCardLogDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zcardlog:save')")
    public Result save(@RequestBody ZCardLogDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);
        dto.setDeptName(SecurityUser.getUser().getDeptName());
        zCardLogService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zcardlog:update')")
    public Result update(@RequestBody ZCardLogDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zCardLogService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zcardlog:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zCardLogService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zcardlog:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZCardLogDTO> list = zCardLogService.list(params);
        attachRealAmount(list);
        ExcelUtils.exportExcelToTarget(response, null, "z_card_log", list, ZCardLogExcel.class);
    }

    @GetMapping("cardList")
    @Operation(summary = "商户列表")
    @LogOperation("商户列表")
    @PreAuthorize("hasAuthority('sys:zcardlog:info')")

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
    @PreAuthorize("hasAuthority('sys:zcardlog:update')")
    private Result uploadFile(@RequestParam("log") String log, @RequestParam("cardId") Long cardId) {
//        cardMatchService.matchFile(cardId, log);
        return Result.ok;
    }
}