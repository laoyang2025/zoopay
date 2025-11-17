package io.renren.bledger.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.bledger.dto.BotAccountDTO;
import io.renren.bledger.excel.BotAccountExcel;
import io.renren.bledger.service.BotAccountService;
import io.renren.commons.log.annotation.LogOperation;
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
import io.renren.dao.SysDeptDao;
import io.renren.dao.SysUserDao;
import io.renren.entity.SysDeptEntity;
import io.renren.entity.SysUserEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
* 机器人账号
*
* @author kernel kernel@qq.com
* @since 1.0 2024-06-02
*/
@RestController
@RequestMapping("botman/botaccount")
@Tag(name = "机器人账号")
@Slf4j
public class BotAccountController {
    @Resource
    private BotAccountService botAccountService;

    @Resource
    private SysDeptDao orgDeptDao;

    @Resource
    private SysUserDao orgUserDao;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys/botman:botaccount:page')")
    public Result<PageData<BotAccountDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){
        PageData<BotAccountDTO> page = botAccountService.page(params);

        return new Result<PageData<BotAccountDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys/botman:botaccount:info')")
    public Result<BotAccountDTO> get(@PathVariable("id") Long id){
        BotAccountDTO data = botAccountService.get(id);

        return new Result<BotAccountDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys/botman:botaccount:save')")
    public Result save(@RequestBody BotAccountDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        Long deptId = SecurityUser.getUser().getDeptId();

        SysDeptEntity botDeptEntity = orgDeptDao.selectOne(Wrappers.<SysDeptEntity>lambdaQuery()
                .eq(SysDeptEntity::getName, "飞机记账")
                .eq(SysDeptEntity::getPid, deptId)
        );
        if(botDeptEntity == null) {
            return Result.fail(9999, "invalid user");
        }

        SysUserEntity orgUserEntity = orgUserDao.selectOne(Wrappers.<SysUserEntity>lambdaQuery()
                .eq(SysUserEntity::getDeptId, botDeptEntity.getId())
                .eq(SysUserEntity::getUsername, dto.getUserName())
        );
        if( orgUserEntity == null) {
            return Result.fail(9999, "no such user");
        }

        dto.setUserId( orgUserEntity.getId());
        log.info("dto: {}", dto);
        botAccountService.save(dto);
        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys/botman:botaccount:update')")
    public Result update(@RequestBody BotAccountDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        botAccountService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys/botman:botaccount:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        botAccountService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys/botman:botaccount:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        List<BotAccountDTO> list = botAccountService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "机器人账号", list, BotAccountExcel.class);
    }

}
