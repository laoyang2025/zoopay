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
import io.renren.dto.SysUserDTO;
import io.renren.service.SysUserService;
import io.renren.zadmin.dao.ZChannelDao;
import io.renren.zadmin.dao.ZWarningDao;
import io.renren.zadmin.dto.ZCardDTO;
import io.renren.zadmin.dto.ZChannelDTO;
import io.renren.zadmin.dto.ZRouteDTO;
import io.renren.zadmin.entity.ZChannelEntity;
import io.renren.zadmin.entity.ZRouteEntity;
import io.renren.zadmin.entity.ZWarningEntity;
import io.renren.zadmin.excel.ZRouteExcel;
import io.renren.zadmin.service.ZCardService;
import io.renren.zadmin.service.ZChannelService;
import io.renren.zadmin.service.ZRouteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * z_route
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-08-12
 */
@RestController
@RequestMapping("zoo/org/route")
@Tag(name = "zoo_org_route")
@Slf4j
public class ZRouteController {
    @Resource
    private ZRouteService zRouteService;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private ZCardService zCardService;
    @Resource
    private ZChannelService zChannelService;
    @Resource
    private ZChannelDao zChannelDao;
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
    @PreAuthorize("hasAuthority('sys:zroute:page')")
    public Result<PageData<ZRouteDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<ZRouteDTO> page = zRouteService.page(params);
        return new Result<PageData<ZRouteDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zroute:info')")
    public Result<ZRouteDTO> get(@PathVariable("id") Long id) {
        ZRouteDTO data = zRouteService.get(id);
        return new Result<ZRouteDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:zroute:save')")
    public Result save(@RequestBody ZRouteDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);
        List<ZRouteDTO.RouteObject> objectList = dto.getObjectList();
        try {
            // 批量添加
            if (objectList != null && objectList.size() > 0) {
                for (ZRouteDTO.RouteObject routeObject : objectList) {
                    dto.setObjectId(routeObject.getObjectId());
                    dto.setObjectName(routeObject.getObjectName());
                    dto.setId(null); // very important
                    if (dto.getRouteType().equals("withdraw")) {
                        dto.setPayCode("withdraw");
                    }
                    zRouteService.save(dto);
                }
            } else {
                // 单笔添加
                if (dto.getRouteType().equals("withdraw")) {
                    dto.setPayCode("withdraw");
                }
                zRouteService.save(dto);
            }
        } catch (DuplicateKeyException ex) {

            if (objectList == null) {
                return Result.fail(9999, "路由条目已存在, 请勿重复添加:" + dto.getObjectName());
            }

            // todo: match to specific card
            String message = ex.getMessage();
            // System.out.println(message);
            String regex = ".*Duplicate entry '\\d+-\\w+-\\w+-(\\d+)'.*";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(message);
            if (matcher.matches()) {
                String group = matcher.group(1);
                Long cardId = Long.parseLong(group);
                for (ZRouteDTO.RouteObject routeObject : objectList) {
                    if (routeObject.getObjectId().equals(cardId)) {
                        return Result.fail(9999, "路由条目已存在, 请勿重复添加:" + routeObject.getObjectName());
                    }
                }
            }
            return Result.fail(9999, "路由条目已存在, 请勿重复添加, code=9993");
        }

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:zroute:update')")
    public Result update(@RequestBody ZRouteDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        ZRouteEntity zRouteEntity = zRouteService.selectById(dto.getId());
        Integer oldEnable = zRouteEntity.getEnabled();
        Integer newEnable = dto.getEnabled();

        zRouteService.update(dto);

        if (zRouteEntity.getRouteType().equals("charge")) {
            ZWarningEntity warningEntity = new ZWarningEntity();
            warningEntity.setDeptId(zRouteEntity.getDeptId());
            warningEntity.setDeptName(zRouteEntity.getDeptName());
            warningEntity.setMsgType("operation");
            String msg;
            String username = SecurityUser.getUser().getUsername();

            if (oldEnable == 0 && newEnable == 1) {
                msg = "启用路由,商户:" +  zRouteEntity.getMerchantName() + ", 路由信息:" + zRouteEntity.getObjectName() + ":" + zRouteEntity.getObjectId() +", 操作人:" + username;
                warningEntity.setMsg(msg);
                zWarningDao.insert(warningEntity);
            } else if (oldEnable == 1 && newEnable == 0) {
                msg = "停用路由，商户:" + zRouteEntity.getMerchantName() + ", 路由信息:" +zRouteEntity.getObjectName() + ":" + zRouteEntity.getObjectId() +", 操作人:" + username;
                warningEntity.setMsg(msg);
                zWarningDao.insert(warningEntity);
            }
        }

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:zroute:delete')")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        zRouteService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:zroute:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<ZRouteDTO> list = zRouteService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "z_route", list, ZRouteExcel.class);
    }


    // 商户列表
    @GetMapping("/merchants")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zroute:info')")
    public Result<List<SysUserDTO>> getMerchantList() {
        Map<String, Object> params = new HashMap<>();
        params.put("userType", "merchant");
        List<SysUserDTO> list = sysUserService.list(params);
        Result<List<SysUserDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }

    // 代理列表
    @GetMapping("/agents")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zroute:info')")
    public Result<List<SysUserDTO>> getAgentList() {
        Map<String, Object> params = new HashMap<>();
        params.put("userType", "agent");
        List<SysUserDTO> list = sysUserService.list(params);
        Result<List<SysUserDTO>> result = new Result<>();
        result.setData(list);
        return result;
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

    // 渠道列表
    @GetMapping("/channels")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:zroute:info')")
    public Result<List<ZChannelDTO>> getChannelList() {
        Map<String, Object> params = new HashMap<>();
        List<ZChannelDTO> list = zChannelService.list(params);
        Result<List<ZChannelDTO>> result = new Result<>();
        result.setData(list);
        return result;
    }
}