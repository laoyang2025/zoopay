package io.renren.zadmin.controller.ant;


import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.tools.utils.Result;
import io.renren.dao.SysDeptDao;
import io.renren.dao.SysUserDao;
import io.renren.dto.SysUserDTO;
import io.renren.entity.SysDeptEntity;
import io.renren.entity.SysUserEntity;
import io.renren.service.SysUserService;
import io.renren.zapi.utils.CommonUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("zoo/ant/register")
@Tag(name = "zoo_ant_register")
public class AntRegisterController {

    @Resource
    private SysUserDao sysUserDao;
    @Resource
    private SysUserService sysUserService;
    @Resource
    private SysDeptDao sysDeptDao;

    /**
     * 推荐码
     *
     * @param deptId
     * @param username
     * @return
     */
    private String getRcode(Long deptId, String username) {
        for (int i = 0; i < 10; i++) {
            String code = DigestUtil.md5Hex(username + Math.random()).substring(0, 8);
            SysUserEntity user = sysUserDao.selectOne(Wrappers.<SysUserEntity>lambdaQuery()
                    .eq(SysUserEntity::getRcode, code)
                    .eq(SysUserEntity::getRcode, deptId)
            );
            if (user == null) {
                return code;
            }
        }
        return null;
    }

    /**
     * 从域名获取当前的机构
     * @return
     */
    private SysDeptEntity getDept() {
        String domain = CommonUtils.getDomain();
        SysDeptEntity deptEntity = sysDeptDao.selectOne(Wrappers.<SysDeptEntity>lambdaQuery()
                .eq(SysDeptEntity::getApiDomain, domain)
        );
        return deptEntity;
    }

    // 注册
    @PostMapping("zoo/ant/register")
    public Result register(@RequestBody SysUserDTO dto) {
        SysDeptEntity dept = getDept();
        dto.setDeptId(dept.getId());
        dto.setDeptName(dept.getName());
        dto.setRealName(dto.getUsername());
        dto.setRcode(getRcode(dept.getId(), dto.getUsername()));
        sysUserService.save(dto);
        return new Result();
    }
}
