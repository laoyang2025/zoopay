package io.renren.zadmin.controller.ant;

import io.renren.commons.log.annotation.LogOperation;
import io.renren.commons.security.user.MyUserDetail;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.page.PageData;
import io.renren.commons.tools.utils.ExcelUtils;
import io.renren.commons.tools.utils.Result;
import io.renren.commons.tools.validator.AssertUtils;
import io.renren.commons.tools.validator.ValidatorUtils;
import io.renren.commons.tools.validator.group.AddGroup;
import io.renren.commons.tools.validator.group.DefaultGroup;
import io.renren.commons.tools.validator.group.UpdateGroup;
import io.renren.zapi.ZooConstant;
import io.renren.zadmin.dto.ZBalanceDTO;
import io.renren.zadmin.excel.ZBalanceExcel;
import io.renren.zadmin.service.ZBalanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


/**
* z_withdraw
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-08-11
*/
@RestController
@RequestMapping("zoo/ant/balance")
@Tag(name = "zoo_ant_balance")
public class AntBalanceController {
    @Resource
    private ZBalanceService zBalanceService;

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('zant:zbalance:info')")
    public Result<ZBalanceDTO> get(@PathVariable("id") Long id){
        MyUserDetail user = SecurityUser.getUser();

        ZBalanceDTO data = zBalanceService.get(id);
        if(!data.getOwnerId().equals(user.getId()) || !ZooConstant.OWNER_TYPE_ANT.equals(data.getOwnerType())) {
            throw new RenException("invalid user");
        }

        return new Result<ZBalanceDTO>().ok(data);
    }



}