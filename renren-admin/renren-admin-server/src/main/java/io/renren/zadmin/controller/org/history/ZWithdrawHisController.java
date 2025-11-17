package io.renren.zadmin.controller.org.history;

import io.renren.commons.log.annotation.LogOperation;
import io.renren.commons.security.user.MyUserDetail;
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
import io.renren.dto.SysUserDTO;
import io.renren.service.SysUserService;
import io.renren.zadmin.dao.ZCardDao;
import io.renren.zadmin.dto.*;
import io.renren.zadmin.excel.ZWithdrawExcel;
import io.renren.zadmin.service.ZCardService;
import io.renren.zadmin.service.ZChannelService;
import io.renren.zadmin.service.ZWithdrawHisService;
import io.renren.zapi.merchant.ApiService;
import io.renren.zapi.route.RouteService;
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
* 商户提现
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/org/withdrawhis")
@Tag(name = "zoo_org_withdraw")
public class ZWithdrawHisController {

    @Resource
    private ZCardService zCardService;
    @Resource
    private ZWithdrawHisService zWithdrawHisService;
    @Resource
    private ZChannelService zChannelService;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private RouteService routeService;
    @Resource
    private ApiService apiService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:page')")
    public Result<PageData<ZWithdrawHisDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
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

        PageData<ZWithdrawHisDTO> page = zWithdrawHisService.page(params);
        return new Result<PageData<ZWithdrawHisDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:info')")
    public Result<ZWithdrawHisDTO> get(@PathVariable("id") Long id){
        ZWithdrawHisDTO data = zWithdrawHisService.get(id);
        return new Result<ZWithdrawHisDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:save')")
    public Result save(@RequestBody ZWithdrawHisDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);
        zWithdrawHisService.save(dto);
        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:update')")
    public Result update(@RequestBody ZWithdrawHisDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);
        zWithdrawHisService.update(dto);
        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");
        zWithdrawHisService.delete(ids);
        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZWithdrawHisDTO> list = zWithdrawHisService.list(params);
        ExcelUtils.exportExcelToTarget(response, null, "z_withdraw", list, ZWithdrawExcel.class);
    }

    @GetMapping("channelList")
    @Operation(summary = "渠道列表")
    @LogOperation("渠道列表")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:info')")
    public Result<List<ZChannelDTO>> channelList() {
        List<ZChannelDTO> list = zChannelService.list(new HashMap<>());
        Result<List<ZChannelDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }

    @GetMapping("merchantList")
    @Operation(summary = "商户列表")
    @LogOperation("商户列表")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:info')")
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
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:info')")
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
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:info')")
    public Result<List<ZCardDTO>> cardList() {
        List<ZCardDTO> list = zCardService.list(new HashMap<>());
        Result<List<ZCardDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }

    // 当前商户的代付路由
    @GetMapping("route")
    @Operation(summary = "代理列表")
    @LogOperation("代理列表")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:info')")
    public Result<List<ZRouteDTO>> route(@RequestParam("merchantId") Long merchantId) {
        Result<List<ZRouteDTO>> result = new Result<>();
        List<ZRouteDTO> routeList = routeService.getRoutes(merchantId, "withdraw");
        result.setData(routeList);
        return result;
    }

    // 当前商户的代付路由
    @PutMapping("assign")
    @Operation(summary = "分配")
    @LogOperation("分派")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:update')")
    public Result assign(@RequestBody ZWithdrawDTO dto) {
        apiService.manualWithdrawAssign(dto);
        return Result.ok;
    }

    @GetMapping("manualNotify")
    @Operation(summary = "人工通知")
    @LogOperation("人工通知")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:update')")
    public Result manualNotify(@RequestParam("id") Long id) {
        apiService.notifyWithdraw(id);
        return Result.ok;
    }

    // 拒绝掉
    @GetMapping("reject")
    @Operation(summary = "拒绝")
    @LogOperation("拒绝")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:update')")
    public Result reject(@RequestParam("id") Long id) {
        apiService.rejectWithdraw(id);
        return Result.ok;
    }

    // 人工成功
    @GetMapping("success")
    @Operation(summary = "成功")
    @LogOperation("成功")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:update')")
    public Result success(@RequestParam("id") Long id, @RequestParam("utr") String utr) {
        apiService.successWithdraw(id, utr);
        return Result.ok;
    }

    // 查询渠道
    @GetMapping("queryChannel")
    @Operation(summary = "查询渠道")
    @LogOperation("查询渠道")
    @PreAuthorize("hasAuthority('sys:zwithdrawhis:update')")
    public Result queryChannel(@RequestParam("id") Long id) {
        apiService.queryChannelWithdraw(id);
        return Result.ok;
    }
}