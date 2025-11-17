package io.renren.zadmin.controller.org.report;

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
import io.renren.zadmin.dto.VdMerchantDTO;
import io.renren.zadmin.excel.VdMerchantExcel;
import io.renren.zadmin.service.VdMerchantService;
import io.renren.zapi.ZooConstant;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;


/**
* VIEW
*
* @author Mark sunlightcs@gmail.com
* @since 3.0 2024-09-10
*/
@RestController
@RequestMapping("zoo/org/vdmerchant")
@Tag(name = "VIEW")
@Slf4j
public class VdMerchantController {
    @Resource
    private VdMerchantService vdMerchantService;

    @GetMapping("page")
    @Operation(summary = "分页")
    @Parameters({
            @Parameter(name = Constant.PAGE, description = "当前页码，从1开始", required = true),
            @Parameter(name = Constant.LIMIT, description = "每页显示记录数", required = true),
            @Parameter(name = Constant.ORDER_FIELD, description = "排序字段"),
            @Parameter(name = Constant.ORDER, description = "排序方式，可选值(asc、desc)")
    })
    @PreAuthorize("hasAuthority('sys:vdmerchant:page')")
    public Result<PageData<VdMerchantDTO>> page(@Parameter(hidden = true) @RequestParam Map<String, Object> params){

        MyUserDetail user = SecurityUser.getUser();
        if ("merchant".equals(user.getUserType())) {
            params.put("merchantName", user.getUsername());
            log.info("vdmerchant page for: {}", user.getUsername());
        }

        PageData<VdMerchantDTO> page = vdMerchantService.page(params);
        return new Result<PageData<VdMerchantDTO>>().ok(page);
    }

    @GetMapping("{id}")
    @Operation(summary = "信息")
    @PreAuthorize("hasAuthority('sys:vdmerchant:info')")
    public Result<VdMerchantDTO> get(@PathVariable("id") Long id){
        VdMerchantDTO data = vdMerchantService.get(id);

        return new Result<VdMerchantDTO>().ok(data);
    }

    @PostMapping
    @Operation(summary = "保存")
    @LogOperation("保存")
    @PreAuthorize("hasAuthority('sys:vdmerchant:save')")
    public Result save(@RequestBody VdMerchantDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, AddGroup.class, DefaultGroup.class);

        vdMerchantService.save(dto);

        return new Result();
    }

    @PutMapping
    @Operation(summary = "修改")
    @LogOperation("修改")
    @PreAuthorize("hasAuthority('sys:vdmerchant:update')")
    public Result update(@RequestBody VdMerchantDTO dto){
        //效验数据
        ValidatorUtils.validateEntity(dto, UpdateGroup.class, DefaultGroup.class);

        vdMerchantService.update(dto);

        return new Result();
    }

    @DeleteMapping
    @Operation(summary = "删除")
    @LogOperation("删除")
    @PreAuthorize("hasAuthority('sys:vdmerchant:delete')")
    public Result delete(@RequestBody Long[] ids){
        //效验数据
        AssertUtils.isArrayEmpty(ids, "id");

        vdMerchantService.delete(ids);

        return new Result();
    }

    @GetMapping("export")
    @Operation(summary = "导出")
    @LogOperation("导出")
    @PreAuthorize("hasAuthority('sys:vdmerchant:export')")
    public void export(@Parameter(hidden = true) @RequestParam Map<String, Object> params, HttpServletResponse response) throws Exception {
        params.put(Constant.LIMIT, "1000000");
        MyUserDetail user = SecurityUser.getUser();
        if (user.getUserType().equals(ZooConstant.USER_TYPE_MERCHANT)) {
            params.put("merchantName", user.getUsername());
        }
        List<VdMerchantDTO> list = vdMerchantService.page(params).getList();
        ExcelUtils.exportExcelToTarget(response, null, "VIEW", list, VdMerchantExcel.class);
    }

}