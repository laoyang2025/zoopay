/**
 * Copyright (c) 2018 人人开源 All rights reserved.
 * <p>
 * https://www.renren.io
 * <p>
 * 版权所有，侵权必究！
 */

package io.renren.controller;

import io.renren.commons.log.annotation.LogOperation;
import io.renren.commons.security.user.MyUserDetail;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.ExcelUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.dto.SysLogErrorDTO;
import io.renren.excel.SysLogErrorExcel;
import io.renren.service.SysLogErrorService;
import io.renren.zapi.ZooConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;


/**
 * 异常日志
 *
 * @author Mark sunlightcs@gmail.com
 * @since 1.0.0
 */
@RestController
@RequestMapping("log/error")
@Tag(name = "异常日志")
public class SysLogErrorController {
    @Resource
    private SysLogErrorService sysLogErrorService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)"),
            @Parameter(name = "module", description = "模块名称，如：sys")
    })
    @PreAuthorize("hasAuthority('sys:log:error')")
    public Result<PageData<SysLogErrorDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params) {

        // 运营机构登录才能访问
        MyUserDetail user = SecurityUser.getUser();
        if(user.getUserType() == null || user.getUserType().equals(ZooConstant.USER_TYPE_OPERATION)) {
            params.put("userType", ZooConstant.USER_TYPE_OPERATION);
        }

        PageData<SysLogErrorDTO> page = sysLogErrorService.page(params);
        return new Result<PageData<SysLogErrorDTO>>().ok(page);
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("Export Log Error")
    @PreAuthorize("hasAuthority('sys:log:error')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        // 运营机构登录才能访问
        MyUserDetail user = SecurityUser.getUser();
        if(user.getUserType().equals(ZooConstant.USER_TYPE_OPERATION)) {
            params.put("userType", ZooConstant.USER_TYPE_OPERATION);
        }

        List<SysLogErrorDTO> list = sysLogErrorService.list(params);

        ExcelUtils.exportExcelToTarget(response, null, "异常日志", list, SysLogErrorExcel.class);
    }

}