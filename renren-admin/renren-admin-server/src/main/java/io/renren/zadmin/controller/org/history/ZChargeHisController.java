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
import io.renren.zadmin.dto.ZCardDTO;
import io.renren.zadmin.dto.ZChannelDTO;
import io.renren.zadmin.dto.ZChargeDTO;
import io.renren.zadmin.dto.ZChargeHisDTO;
import io.renren.zadmin.excel.ZChargeExcel;
import io.renren.zadmin.service.ZCardService;
import io.renren.zadmin.service.ZChannelService;
import io.renren.zadmin.service.ZChargeHisService;
import io.renren.zapi.merchant.ApiService;
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
* 商户充值
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/org/chargehis")
@Tag(name = "zoo/org/chargehis")
public class ZChargeHisController {
    @Resource
    private ZChargeHisService zChargeHisService;
    @Resource
    private ZChannelService zChannelService;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private ApiService apiService;
    @Resource
    private ZCardService zCardService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zchargehis:page')")
    public Result<PageData<ZChargeHisDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){

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

        PageData<ZChargeHisDTO> page = zChargeHisService.page(params);
        return new Result<PageData<ZChargeHisDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zchargehis:info')")
    public Result<ZChargeHisDTO> get(@PathVariable("id") Long id){
        ZChargeHisDTO data = zChargeHisService.get(id);

        return new Result<ZChargeHisDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zchargehis:save')")
    public Result save(@RequestBody ZChargeHisDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        zChargeHisService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zchargehis:update')")
    public Result update(@RequestBody ZChargeHisDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        zChargeHisService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zchargehis:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zChargeHisService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zchargehis:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZChargeHisDTO> list = zChargeHisService.list(params);
        ExcelUtils.exportExcelToTarget(response, null, "z_charge", list, ZChargeExcel.class);
    }

    @GetMapping("channelList")
    @Operation(summary = "渠道列表")
    @LogOperation("渠道列表")
    @PreAuthorize("hasAuthority('sys:zchargehis:info')")
    public Result<List<ZChannelDTO>> channelList() {
        List<ZChannelDTO> list = zChannelService.list(new HashMap<>());
        Result<List<ZChannelDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }

    @GetMapping("merchantList")
    @Operation(summary = "商户列表")
    @LogOperation("商户列表")
    @PreAuthorize("hasAuthority('sys:zchargehis:info')")
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
    @PreAuthorize("hasAuthority('sys:zchargehis:info')")
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
    @PreAuthorize("hasAuthority('sys:zchargehis:info')")
    public Result<List<ZCardDTO>> cardList() {
        List<ZCardDTO> list = zCardService.list(new HashMap<>());
        Result<List<ZCardDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }

    @PutMapping("manualProcess")
    @Operation(summary = "人工处理")
    @LogOperation("人工处理")
    @PreAuthorize("hasAuthority('sys:zchargehis:update')")
    public Result manualProcess(@RequestBody ZChargeDTO dto) {
        apiService.manualProcess(dto);
        return Result.ok;
    }

    @GetMapping("manualNotify")
    @Operation(summary = "人工通知")
    @LogOperation("人工通知")
    @PreAuthorize("hasAuthority('sys:zchargehis:update')")
    public Result manualNotify(@RequestParam("id") Long id) {
        apiService.notifyCharge(id);
        return Result.ok;
    }

    @GetMapping("queryChannel")
    @Operation(summary = "查询渠道")
    @LogOperation("查询渠道")
    @PreAuthorize("hasAuthority('sys:zchargehis:update')")
    public Result queryChannel(@RequestParam("id") Long id) {
        apiService.queryChannelCharge(id);
        return Result.ok;
    }

}