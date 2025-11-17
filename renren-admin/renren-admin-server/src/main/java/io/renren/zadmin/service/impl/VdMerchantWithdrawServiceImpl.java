package io.renren.zadmin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.mybatis.annotation.DataFilter;
import io.renren.commons.mybatis.service.impl.CrudServiceImpl;
import io.renren.commons.security.user.MyUserDetail;
import io.renren.commons.security.user.SecurityUser;
import io.renren.commons.tools.constant.Constant;
import io.renren.commons.tools.page.PageData;
import io.renren.dao.SysUserDao;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dao.VdMerchantWithdrawDao;
import io.renren.zadmin.dto.VdChargeChannelDTO;
import io.renren.zadmin.dto.VdMerchantDTO;
import io.renren.zadmin.dto.VdMerchantWithdrawDTO;
import io.renren.zadmin.entity.VdMerchantEntity;
import io.renren.zadmin.entity.VdMerchantWithdrawEntity;
import io.renren.zadmin.service.VdMerchantWithdrawService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.symmetric.AES;
import org.glassfish.jersey.server.wadl.internal.WadlResource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * VIEW
 *
 * @author Mark sunlightcs@gmail.com
 * @since 3.0 2024-09-11
 */
@Service
public class VdMerchantWithdrawServiceImpl extends CrudServiceImpl<VdMerchantWithdrawDao, VdMerchantWithdrawEntity, VdMerchantWithdrawDTO> implements VdMerchantWithdrawService {

    @DataFilter(userId = "")
    @Override
    public PageData<VdMerchantWithdrawDTO> page(Map<String, Object> params) {
        IPage<VdMerchantWithdrawEntity> page = baseDao.selectPage(
                getPage(params, Constant.CREATE_DATE, false),
                applyFilter(params)
        );
        return getPageData(page, VdMerchantWithdrawDTO.class);
    }

    @Resource
    private SysUserDao sysUserDao;

    @Override
    public QueryWrapper<VdMerchantWithdrawEntity> getWrapper(Map<String, Object> params) {
        QueryWrapper<VdMerchantWithdrawEntity> wrapper = new QueryWrapper<>();

        String createDate = (String) params.get("createDate");
        wrapper.eq(StringUtils.isNotBlank(createDate), "create_date", createDate);

        String merchantName = (String) params.get("merchantName");
        wrapper.eq(StringUtils.isNotBlank(merchantName), "merchant_name", merchantName);

        if (StringUtils.isBlank(merchantName)) {
            MyUserDetail user = SecurityUser.getUser();
            if ("middle".equals(user.getUserType())) {
                List<String> merchantNameList = sysUserDao.selectList(Wrappers.<SysUserEntity>lambdaQuery()
                        .eq(SysUserEntity::getMiddleId, user.getId())
                        .select(SysUserEntity::getUsername)
                ).stream().map(SysUserEntity::getUsername).collect(Collectors.toList());
                if (merchantNameList.size() > 0) {
                    wrapper.in("merchant_name", merchantNameList);
                }
                else {
                    // 为空， 应该看不到任何交易
                    wrapper.eq("dept_id", "1L");
                }
            }
        }

        return wrapper;
    }


}