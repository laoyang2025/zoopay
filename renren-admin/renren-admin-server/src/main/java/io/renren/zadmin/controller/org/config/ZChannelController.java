package io.renren.zadmin.controller.org.config;

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
import io.renren.zadmin.dao.ZChannelDao;
import io.renren.zadmin.dto.ZChannelDTO;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.excel.ZChannelExcel;
import io.renren.zadmin.service.ZChannelService;
import io.renren.zapi.channel.ChannelFactory;
import io.renren.zapi.channel.PayChannel;
import io.renren.zapi.channel.dto.ChannelBalanceResponse;
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
import java.util.WeakHashMap;


/**
 * 渠道管理
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@RestController
@RequestMapping("zoo/org/channel")
@Tag(name = "zoo_org_channel")
public class ZChannelController {
    @Resource
    private ZChannelService zChannelService;
    @Resource
    private ChannelFactory channelFactory;
    @Resource
    private ZChannelDao zChannelDao;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zchannel:page')")
    public Result<PageData<ZChannelDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<ZChannelDTO> page = zChannelService.page(params);

        return new Result<PageData<ZChannelDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zchannel:info')")
    public Result<ZChannelDTO> get(@PathVariable("id") Long id) {
        ZChannelDTO data = zChannelService.get(id);

        return new Result<ZChannelDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zchannel:save')")
    public Result save(@RequestBody ZChannelDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);
        dto.setDeptName(SecurityUser.getUser().getDeptName());
        zChannelService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zchannel:update')")
    public Result update(@RequestBody ZChannelDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zChannelService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zchannel:delete')")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zChannelService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zchannel:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZChannelDTO> list = zChannelService.list(params);
        ExcelUtils.exportExcelToTarget(response, null, "z_channel", list, ZChannelExcel.class);
    }

    @GetMapping("refresh")
    @Operation(summary = "刷新")
    @LogOperation("刷新")
    @PreAuthorize("hasAuthority('sys:zchannel:update')")
    public Result refresh(@RequestParam("id") Long id) {
        PayChannel payChannel = channelFactory.get(id);
        ChannelBalanceResponse balance = payChannel.balance();
        zChannelDao.update(null, Wrappers.<ZChannelEntity>lambdaUpdate()
                .eq(ZChannelEntity::getId, id)
                .set(ZChannelEntity::getBalanceMemo, balance.getBalanceMemo())
        );
        return Result.ok;
    }
}