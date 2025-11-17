package io.renren.zadmin.controller.org.mtxn;

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
import io.renren.dto.SysUserDTO;
import io.renren.service.SysUserService;
import io.renren.zadmin.dto.ZCardDTO;
import io.renren.zadmin.dto.ZChannelDTO;
import io.renren.zadmin.dto.ZChargeDTO;
import io.renren.zadmin.excel.ZChargeExcel;
import io.renren.zadmin.service.ZCardService;
import io.renren.zadmin.service.ZChannelService;
import io.renren.zadmin.service.ZChargeService;
import io.renren.zapi.ZConfig;
import io.renren.zapi.merchant.ApiService;
import io.renren.zapi.route.RouteService;
import io.renren.zapi.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
* 商户充值
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/org/charge")
@Tag(name = "zoo/org/charge")
public class ZChargeController {
    @Resource
    private RouteService routeService;
    @Resource
    private ZChargeService zChargeService;
    @Resource
    private ZChannelService zChannelService;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private ApiService apiService;
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
    @PreAuthorize("hasAuthority('sys:zcharge:page')")
    public Result<PageData<ZChargeDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){

        MyUserDetail user = SecurityUser.getUser();

        // 代理访问
        if("agent".equals(user.getUserType())) {
            params.put("agentId", user.getId().toString());
        }
        // 卡主访问
        else if("user".equals(user.getUserType())) {
            params.put("userId", user.getId().toString());
        }
        // 码农访问
        else if("ant".equals(user.getUserType())) {
            params.put("antId", user.getId().toString());
        }
        // 拓展方登录
        else if("middle".equals(user.getUserType())) {
            params.put("middleId", user.getId().toString());
        }

        PageData<ZChargeDTO> page = zChargeService.page(params);
        return new Result<PageData<ZChargeDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zcharge:info')")
    public Result<ZChargeDTO> get(@PathVariable("id") Long id){
        ZChargeDTO data = zChargeService.get(id);

        return new Result<ZChargeDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zcharge:save')")
    public Result save(@RequestBody ZChargeDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        zChargeService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zcharge:update')")
    public Result update(@RequestBody ZChargeDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zChargeService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zcharge:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zChargeService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zcharge:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZChargeDTO> list = zChargeService.list(params);

        // 机构时间与UTC的时间差
        int offset = CommonUtils.getOffsetMinutes(SecurityUser.getUser().getTimezone());
        System.out.println("minutes = " + offset + " sys minutes:" + zConfig.getTzMinutes());

        for (ZChargeDTO dto : list) {
            Date createDate = DateUtils.addMinutes(dto.getCreateDate(), zConfig.getTzMinutes());
            dto.setCreateDate(createDate);
            if(dto.getNotifyTime() != null) {
                Date notifyDate = DateUtils.addMinutes(dto.getNotifyTime(), -zConfig.getTzMinutes() + offset);
                dto.setNotifyTime(notifyDate);
            }
        }
        ExcelUtils.exportExcelToTarget(response, null, "z_charge", list, ZChargeExcel.class);
    }

    @GetMapping("channelList")
    @Operation(summary = "渠道列表")
    @LogOperation("渠道列表")
    @PreAuthorize("hasAuthority('sys:zcharge:info')")
    public Result<List<ZChannelDTO>> channelList() {
        List<ZChannelDTO> list = zChannelService.list(new HashMap<>());
        Result<List<ZChannelDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }

    @GetMapping("merchantList")
    @Operation(summary = "商户列表")
    @LogOperation("商户列表")
    @PreAuthorize("hasAuthority('sys:zcharge:info')")
    public Result<List<SysUserDTO>> merchantList() {
        HashMap<String, Object> map = new HashMap();
        map.put("userType", "merchant");
        List<SysUserDTO> list = sysUserService.list(map);
        Result<List<SysUserDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }

    @GetMapping("agentList")
    @Operation(summary = "代理列表")
    @LogOperation("代理列表")
    @PreAuthorize("hasAuthority('sys:zcharge:info')")
    public Result<List<SysUserDTO>> agentList() {
        HashMap<String, Object> map = new HashMap();
        map.put("userType", "agent");
        List<SysUserDTO> list = sysUserService.list(map);
        Result<List<SysUserDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }

    @GetMapping("cardList")
    @Operation(summary = "卡列表")
    @LogOperation("卡列表")
    @PreAuthorize("hasAuthority('sys:zcharge:info')")
    public Result<List<ZCardDTO>> cardList() {
        List<ZCardDTO> list = zCardService.list(new HashMap<>());
        Result<List<ZCardDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }

    @PutMapping("manualProcess")
    @Operation(summary = "人工处理")
    @LogOperation("人工处理")
    @PreAuthorize("hasAuthority('sys:zcharge:update')")
    public Result manualProcess(@RequestBody ZChargeDTO dto) {
        apiService.manualProcess(dto);
        return Result.ok;
    }

    @GetMapping("manualNotify")
    @Operation(summary = "人工通知")
    @LogOperation("人工通知")
    @PreAuthorize("hasAuthority('sys:zcharge:update')")
    public Result manualNotify(@RequestParam("id") Long id) {
        apiService.notifyCharge(id);
        return Result.ok;
    }

    @GetMapping("queryChannel")
    @Operation(summary = "查询渠道")
    @LogOperation("查询渠道")
    @PreAuthorize("hasAuthority('sys:zcharge:update')")
    public Result queryChannel(@RequestParam("id") Long id) {
        apiService.queryChannelCharge(id);
        return Result.ok;
    }
}