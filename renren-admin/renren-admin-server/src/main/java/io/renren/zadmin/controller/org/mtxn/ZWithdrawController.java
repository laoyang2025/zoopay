package io.renren.zadmin.controller.org.mtxn;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import io.renren.zadmin.dao.ZCardDao;
import io.renren.zadmin.dao.ZChannelDao;
import io.renren.zadmin.dao.ZWarningDao;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.dto.ZCardDTO;
import io.renren.zadmin.dto.ZChannelDTO;
import io.renren.zadmin.dto.ZRouteDTO;
import io.renren.zadmin.dto.ZWithdrawDTO;
import io.renren.zadmin.entity.ZCardEntity;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZWarningEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zadmin.excel.ZWithdrawExcel;
import io.renren.zadmin.service.ZCardService;
import io.renren.zadmin.service.ZChannelService;
import io.renren.zadmin.service.ZWithdrawService;
import io.renren.zapi.ZConfig;
import io.renren.zapi.merchant.ApiService;
import io.renren.zapi.route.RouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.util.*;


/**
 * 商户提现
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-11
 */
@RestController
@RequestMapping("zoo/org/withdraw")
@Tag(name = "zoo_org_withdraw")
@Slf4j
public class ZWithdrawController {

    @Resource
    private ZCardService zCardService;
    @Resource
    private ZConfig zConfig;
    @Resource
    private ZWithdrawService zWithdrawService;
    @Resource
    private ZChannelService zChannelService;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private RouteService routeService;
    @Resource
    private ApiService apiService;
    @Resource
    private ZWithdrawDao zWithdrawDao;
    @Resource
    private ZWarningDao zWarningDao;
    @Resource
    private ZChannelDao zChannelDao;
    @Resource
    private ZCardDao zCardDao;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:zwithdraw:page')")
    public Result<PageData<ZWithdrawDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        MyUserDetail user = SecurityUser.getUser();
        // 代理访问
        if ("agent".equals(user.getUserType())) {
            params.put("agentId", user.getId().toString());
        }
        // 卡主访问
        else if ("user".equals(user.getUserType())) {
            params.put("userId", user.getId().toString());
        }
        // 码农访问
        else if ("ant".equals(user.getUserType())) {
            params.put("antId", user.getId().toString());
        }
        // 拓展方登录
        else if ("middle".equals(user.getUserType())) {
            params.put("middleId", user.getId().toString());
        }

        PageData<ZWithdrawDTO> page = zWithdrawService.page(params);
        return new Result<PageData<ZWithdrawDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zwithdraw:info')")
    public Result<ZWithdrawDTO> get(@PathVariable("id") Long id) {
        ZWithdrawDTO data = zWithdrawService.get(id);
        return new Result<ZWithdrawDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zwithdraw:save')")
    public Result save(@RequestBody ZWithdrawDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);
        zWithdrawService.save(dto);
        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zwithdraw:update')")
    public Result update(@RequestBody ZWithdrawDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);
        zWithdrawService.update(dto);
        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zwithdraw:delete')")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");
        zWithdrawService.delete(ids);
        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zwithdraw:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZWithdrawDTO> list = zWithdrawService.list(params);
        for (ZWithdrawDTO dto : list) {
            Date createDate = DateUtils.addMinutes(dto.getCreateDate(), zConfig.getTzMinutes());
            dto.setCreateDate(createDate);
            if (dto.getNotifyTime() != null) {
                Date notifyDate = DateUtils.addMinutes(dto.getNotifyTime(), zConfig.getTzMinutes());
                dto.setNotifyTime(notifyDate);
            }
        }
        ExcelUtils.exportExcelToTarget(response, null, "z_withdraw", list, ZWithdrawExcel.class);
    }

    // 卡列表
    @GetMapping("/cards")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zroute:info')")
    public Result<List<ZCardDTO>> getCardList() {
        Map<String, Object> params = new HashMap<>();
        params.put(Constant.PAGE, "1");
        params.put(Constant.LIMIT, "1000");
        List<ZCardDTO> list = zCardService.list(params);
        Result<List<ZCardDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }

    @GetMapping("channelList")
    @Operation(summary = "渠道列表")
    @LogOperation("渠道列表")
    @PreAuthorize("hasAuthority('sys:zwithdraw:info')")
    public Result<List<ZChannelDTO>> channelList() {
        List<ZChannelDTO> list = zChannelService.list(new HashMap<>());
        Result<List<ZChannelDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }

    @GetMapping("merchantList")
    @Operation(summary = "商户列表")
    @LogOperation("商户列表")
    @PreAuthorize("hasAuthority('sys:zwithdraw:info')")
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
    @PreAuthorize("hasAuthority('sys:zwithdraw:info')")
    public Result<List<SysUserDTO>> agentList() {
        HashMap<String, Object> map = new HashMap();
        map.put("userType", "agent");
        List<SysUserDTO> list = sysUserService.list(map);
        Result<List<SysUserDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }


    // 当前商户的代付路由
    @GetMapping("route")
    @Operation(summary = "代理列表")
    @LogOperation("代理列表")
    @PreAuthorize("hasAuthority('sys:zwithdraw:info')")
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
    @PreAuthorize("hasAuthority('sys:zwithdraw:update')")
    public Result assign(@RequestBody ZWithdrawDTO dto) {
        apiService.manualWithdrawAssign(dto);

        ZChannelEntity zChannelEntity = zChannelDao.selectById(dto.getChannelId());
        // 记录操作记录
        ZWithdrawEntity entity = zWithdrawDao.selectById(dto.getId());
        ZWarningEntity warningEntity = new ZWarningEntity();
        warningEntity.setDeptId(entity.getDeptId());
        warningEntity.setDeptName(entity.getDeptName());
        warningEntity.setMsgType("operation");
        String username = SecurityUser.getUser().getUsername();
        String msg = "代付分配," + "商户:" + entity.getMerchantName() + ",商户单号:" + entity.getOrderId() +
                ",分配目标:" + zChannelEntity.getChannelLabel() +
                ",操作员:" + username;
        warningEntity.setMsg(msg);
        zWarningDao.insert(warningEntity);

        return Result.ok;
    }

    @GetMapping("manualNotify")
    @Operation(summary = "人工通知")
    @LogOperation("人工通知")
    @PreAuthorize("hasAuthority('sys:zwithdraw:update')")
    public Result manualNotify(@RequestParam("id") Long id) {
        apiService.notifyWithdraw(id);
        return Result.ok;
    }

    // 拒绝掉
    @GetMapping("reject")
    @Operation(summary = "拒绝")
    @LogOperation("拒绝")
    @PreAuthorize("hasAuthority('sys:zwithdraw:update')")
    public Result reject(@RequestParam("id") Long id) {
        apiService.rejectWithdraw(id);
        return Result.ok;
    }

    // 人工成功
    @GetMapping("success")
    @Operation(summary = "成功")
    @LogOperation("成功")
    @PreAuthorize("hasAuthority('sys:zwithdraw:update')")
    public Result success(@RequestParam("id") Long id, @RequestParam("utr") String utr) {
        apiService.successWithdraw(id, utr);
        return Result.ok;
    }

    // 查询渠道
    @GetMapping("queryChannel")
    @Operation(summary = "查询渠道")
    @LogOperation("查询渠道")
    @PreAuthorize("hasAuthority('sys:zwithdraw:update')")
    public Result queryChannel(@RequestParam("id") Long id) {
        apiService.queryChannelWithdraw(id);
        return Result.ok;
    }

    // 批量处理
    @GetMapping("batch")
    @Operation(summary = "批处理代付")
    @LogOperation("批处理代付")
    @PreAuthorize("hasAuthority('sys:zwithdraw:update')")
    public Result batch(@RequestParam("id") String id,
                        @RequestParam("processStatus") Integer processStatus,
                        @RequestParam(value = "cardId", required = false) Long cardId,
                        @RequestParam(value = "channelId", required = false) Long channelId
    ) {
        // 100 -> 选卡   200 -> 选的渠道
        String[] split = id.split(",");
        List<Long> idList = new ArrayList<>();
        for (String s : split) {
            idList.add(Long.parseLong(s));
        }
        log.info("cardId: {}, channelId: {}", cardId, channelId);
        apiService.batchWithdraw(idList, processStatus, cardId, channelId);

        // 批量分配代付订单
        if (channelId != null || cardId != null) {
            String objectInfo;
            if (channelId != null) {
                ZChannelEntity zChannelEntity = zChannelDao.selectById(channelId);
                objectInfo = ",分配目标:" + zChannelEntity.getChannelLabel() + ":渠道";
            } else {
                ZCardEntity zCardEntity = zCardDao.selectById(cardId);
                objectInfo = ",分配目标:" + zCardEntity.getAccountUser() + ":" + zCardEntity.getAccountNo();
            }

            List<ZWithdrawEntity> zWithdrawEntities = zWithdrawDao.selectList(Wrappers.<ZWithdrawEntity>lambdaQuery()
                    .in(ZWithdrawEntity::getId, idList)
            );
            ZWithdrawEntity first = zWithdrawEntities.get(0);
            // 记录操作记录
            ZWarningEntity warningEntity = new ZWarningEntity();
            warningEntity.setDeptId(first.getDeptId());
            warningEntity.setDeptName(first.getDeptName());
            warningEntity.setMsgType("operation");
            String username = SecurityUser.getUser().getUsername();
            String msg = "代付分配," + "平台单号:" + id +
                    objectInfo +
                    ",操作员:" + username;
            warningEntity.setMsg(msg);
            zWarningDao.insert(warningEntity);
        }

        return Result.ok;
    }

}