/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.renren.commons.log.annotation.LogOperation;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.security.user.MyUserDetail;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.exception.ErrorCode;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.ExcelUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.commons.tools.validator.AssertUtils;
import io.renren.commons.tools.validator.ValidatorUtils;
import io.renren.commons.tools.validator.group.AddGroup;
import io.renren.commons.tools.validator.group.DefaultGroup;
import io.renren.commons.tools.validator.group.UpdateGroup;
import io.renren.dao.SysDeptDao;
import io.renren.dao.SysUserDao;
import io.renren.dto.PasswordDTO;
import io.renren.dto.SysUserDTO;
import io.renren.entity.SysDeptEntity;
import io.renren.entity.SysUserEntity;
import io.renren.excel.SysUserExcel;
import io.renren.service.SysRoleUserService;
import io.renren.service.SysUserDetailService;
import io.renren.service.SysUserPostService;
import io.renren.service.SysUserService;
import io.renren.zadmin.dao.ZBalanceDao;
import io.renren.zadmin.dao.ZWithdrawDao;
import io.renren.zadmin.entity.ZBalanceEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.ledger.ZLedger;
import io.renren.zapi.ZooConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户管理
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@RestController
@RequestMapping("user")
@Tag(name = "用户管理")
public class SysUserController {
    @Resource
    private SysDeptDao sysDeptDao;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private SysUserDetailService sysUserDetailService;
    @Resource
    private SysRoleUserService sysRoleUserService;
    @Resource
    private PasswordEncoder passwordEncoder;
    @Resource
    private SysUserPostService sysUserPostService;
    @Resource
    private ZBalanceDao zBalanceDao;
    @Resource
    private ZLedger ledger;
    @Resource
    private TransactionTemplate tx;
    @Resource
    private ZWithdrawDao zWithdrawDao;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)"),
            @Parameter(name = "username", description = "用户名")
    })
    @PreAuthorize("hasAuthority('sys:user:page')")
    public Result<PageData<SysUserDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        PageData<SysUserDTO> page = sysUserService.page(params);
        Object userType = params.get("userType");

        if (page.getList().size() == 0) {
            return new Result<PageData<SysUserDTO>>().ok(page);
        }

        // 卡主, 商户， 码农
        if ("user".equals(userType) || "merchant".equals(userType) || "ant".equals(userType)) {
            List<Long> longs = page.getList().stream().map(e -> e.getId()).toList();
            Map<Long, BigDecimal> collect = zBalanceDao.selectList(Wrappers.<ZBalanceEntity>lambdaQuery()
                            .in(ZBalanceEntity::getId, longs)
                            .select(ZBalanceEntity::getBalance, ZBalanceEntity::getId))
                    .stream().collect(Collectors.toMap(e -> e.getId(), e -> e.getBalance()));
            for (SysUserDTO sysUserDTO : page.getList()) {
                sysUserDTO.setBalance(collect.get(sysUserDTO.getId()));
            }
        }

        // 代理
        if ("agent".equals(userType) ) {
            List<Long> longs = page.getList().stream().map(e -> e.getId()).toList();
            Map<Long, List<ZBalanceEntity>> collect = zBalanceDao.selectList(Wrappers.<ZBalanceEntity>lambdaQuery()
                            .in(ZBalanceEntity::getOwnerId, longs)
                            .select(ZBalanceEntity::getBalance, ZBalanceEntity::getId, ZBalanceEntity::getOwnerType))
                    .stream().collect(Collectors.groupingBy(e -> e.getOwnerId()));

            for (SysUserDTO sysUserDTO : page.getList()) {
                List<ZBalanceEntity> zBalanceEntities = collect.get(sysUserDTO.getId());
                for (ZBalanceEntity zBalanceEntity : zBalanceEntities) {
                    if(zBalanceEntity.getOwnerType().equals("agent")) {
                        sysUserDTO.setBalance(zBalanceEntity.getBalance());
                    }
                    else if(zBalanceEntity.getOwnerType().equals("agent:share")) {
                        sysUserDTO.setAgentShare(zBalanceEntity.getBalance());
                    }
                }
            }
        }

        return new Result<PageData<SysUserDTO>>().ok(page);
    }

    @GetMapping("list")
    @Operation(summary = "列表")
    @PreAuthorize("hasAuthority('sys:user:page')")
    public Result<List<SysUserDTO>> list(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {
        List<SysUserDTO> list = sysUserService.list(params);
        return new Result<List<SysUserDTO>>().ok(list);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:user:info')")
    public Result<SysUserDTO> get(@PathVariable("id") Long id) {

        SysUserDTO data = sysUserService.get(id);

        //用户角色列表
        List<Long> roleIdList = sysRoleUserService.getRoleIdList(id);
        data.setRoleIdList(roleIdList);

        //用户岗位列表
        List<Long> postIdList = sysUserPostService.getPostIdList(id);
        data.setPostIdList(postIdList);

        return new Result<SysUserDTO>().ok(data);
    }


    @GetMapping("info")
    @Operation(summary = "登录用户信息")
    public Result<SysUserDTO> info() {
        SysUserDTO data = sysUserService.get(SecurityUser.getUserId());

        // 填充部分机构信息
        if (data.getDeptId() != null) {
            SysDeptEntity dept = sysDeptDao.getById(data.getDeptId());
            data.setDeptName(dept.getName());
            data.setProcessMode(dept.getProcessMode());
            data.setCurrency(dept.getCurrency());
            data.setTimezone(dept.getTimezone());
            data.setC1Rate(dept.getC1Rate());
            data.setC2Rate(dept.getC2Rate());
            data.setAntChargeRate(dept.getAntChargeRate());
            data.setOutRate(dept.getOutRate());
        }

        return new Result<SysUserDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("Save User")
    @PreAuthorize("hasAuthority('sys:user:save')")
    public Result save(@RequestBody SysUserDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);
        sysUserService.save(dto);
        return new Result();
    }

    // 商户上线
    @PostMapping("production")
    @Operation(summary = "上线")
    @LogOperation("Save User")
    @PreAuthorize("hasAuthority('sys:user:save')")
    public Result production(@RequestBody SysUserDTO dto) {

        Long aLong = zWithdrawDao.selectCount(Wrappers.<ZWithdrawEntity>lambdaQuery()
                .eq(ZWithdrawEntity::getMerchantId, dto.getId())
                .in(ZWithdrawEntity::getProcessStatus, List.of(ZooConstant.WITHDRAW_STATUS_NEW, ZooConstant.WITHDRAW_STATUS_ASSIGNED))
        );

        if (aLong > 0) {
            return Result.fail(9999, "商户还有未处理的代付订单, 不能上线");
        }

        ZBalanceEntity balanceEntity = zBalanceDao.selectById(dto.getId());

        tx.executeWithoutResult(status -> {
            sysUserDao.update(null, Wrappers.<SysUserEntity>lambdaUpdate()
                    .set(SysUserEntity::getDev, 1)
                    .eq(SysUserEntity::getId, dto.getId())
            );
            ledger.adjust(dto.getId(), balanceEntity.getBalance().negate(), "商户上线, 余额清零");
        });
        return Result.ok;
    }


    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("Update User")
    @PreAuthorize("hasAuthority('sys:user:update')")
    public Result update(@RequestBody SysUserDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        sysUserService.update(dto);

        return new Result();
    }

    @PutMapping("app")
    @Operation(summary = "修改用户信息")
    @LogOperation("Update User")
    @PreAuthorize("hasAuthority('sys:user:update')")
    public Result updateUserInfo(@RequestBody SysUserDTO dto) {
        sysUserService.updateUserInfo(dto);

        return new Result();
    }

    @PutMapping("password")
    @Operation(summary = "修改密码")
    @LogOperation("Password User")
    public Result password(@RequestBody PasswordDTO dto) {
        //效验数据
        ValidatorUtils.validateEntity(dto);

        MyUserDetail user = SecurityUser.getUser();

        //原密码不正确
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return new Result().error(ErrorCode.PASSWORD_ERROR);
        }

        sysUserService.updatePassword(user.getId(), dto.getNewPassword());

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("Delete User")
    @PreAuthorize("hasAuthority('sys:user:delete')")
    public Result delete(@RequestBody Long[] ids) {
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        sysUserService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("Export User")
    @PreAuthorize("hasAuthority('sys:user:export')")
    @Parameter(name = "username", description = "用户名")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<SysUserDTO> list = sysUserService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "用户管理", list, SysUserExcel.class);
    }

    /**
     * 根据用户Id，获取用户信息
     */
    @GetMapping("getById")
    public Result<MyUserDetail> getById(Long id) {
        MyUserDetail userDetail = sysUserDetailService.getUserDetailById(id);

        return new Result<MyUserDetail>().ok(userDetail);
    }

    /**
     * 根据用户ID,查询用户姓名列表
     */
    @PostMapping("getRealNameList")
    public Result<List<String>> getRealNameList(@RequestBody List<Long> ids) {
        List<String> realNameList = sysUserService.getRealNameList(ids);

        return new Result<List<String>>().ok(realNameList);
    }

    /**
     * 根据角色ID,查询用户ID列表
     */
    @PostMapping("getUserIdListByRoleIdList")
    public Result<List<Long>> getUserIdListByRoleIdList(@RequestParam List<Long> ids) {
        List<Long> userIdList = sysUserService.getUserIdListByRoleIdList(ids);

        return new Result<List<Long>>().ok(userIdList);
    }

    /**
     * 根据角色ID,查询角色名称列表
     */
    @PostMapping("getRoleNameList")
    public Result<List<String>> getRoleNameList(@RequestBody List<Long> ids) {
        List<String> userIdList = sysUserService.getRoleNameList(ids);

        return new Result<List<String>>().ok(userIdList);
    }

    /**
     * 根据岗位ID,查询用户ID列表
     */
    @PostMapping("getUserIdListByPostIdList")
    public Result<List<Long>> getUserIdListByPostIdList(@RequestParam List<Long> ids) {
        List<Long> userIdList = sysUserService.getUserIdListByPostIdList(ids);

        return new Result<List<Long>>().ok(userIdList);
    }

    /**
     * 根据部门ID,查询部门领导列表
     */
    @PostMapping("getLeaderIdListByDeptIdList")
    public Result<List<Long>> getLeaderIdListByDeptIdList(@RequestParam List<Long> ids) {
        List<Long> userIdList = sysUserService.getLeaderIdListByDeptIdList(ids);

        return new Result<List<Long>>().ok(userIdList);
    }

    /**
     * 根据用户ID,查询部门领导ID
     */
    @PostMapping("getLeaderIdListByUserId")
    public Result<Long> getLeaderIdListByUserId(Long userId) {
        Long leaderId = sysUserService.getLeaderIdListByUserId(userId);
        return new Result<Long>().ok(leaderId);
    }

    @Resource
    private SysUserDao sysUserDao;

    /**
     * reset google
     */
    @GetMapping("reset")
    public Result resetGoogle(Long id) {
        GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
        String key = googleAuthenticator.createCredentials().getKey();
        sysUserDao.update(null, Wrappers.<SysUserEntity>lambdaUpdate()
                .eq(SysUserEntity::getId, id)
                .set(SysUserEntity::getTotpKey, key)
                .set(SysUserEntity::getTotpStatus, 1)
        );
        return new Result();
    }

}