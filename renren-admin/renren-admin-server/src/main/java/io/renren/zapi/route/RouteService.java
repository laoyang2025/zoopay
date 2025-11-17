package io.renren.zapi.route;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.renren.commons.tools.exception.RenException;
import io.renren.commons.tools.utils.ConvertUtils;
import io.renren.dao.SysDeptDao;
import io.renren.dao.SysUserDao;
import io.renren.entity.SysDeptEntity;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.dto.ZRouteDTO;
import io.renren.zapi.merchant.ApiContext;
import io.renren.zadmin.dao.ZRouteDao;
import io.renren.zadmin.entity.ZRouteEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@Slf4j
public class RouteService {
    @Resource
    private ZRouteDao zRouteDao;
    @Resource
    private SysDeptDao sysDeptDao;
    @Resource
    private SysUserDao sysUserDao;

    /**
     * @param processMode
     * @param payCode
     * @return
     */
    public ChargeRouter getChargeRouter(String processMode, String payCode) {
        ApiContext context = ApiContext.getContext();
        SysUserEntity merchant = context.getMerchant();

        log.debug("加载收款路由: deptId:{}, merchantId:{}, processMode:{}", merchant.getDeptId(), merchant.getId(), processMode);
        List<ZRouteEntity> zRouteEntities = zRouteDao.selectList(Wrappers.<ZRouteEntity>lambdaQuery()
                .eq(ZRouteEntity::getDeptId, merchant.getDeptId())
                .eq(ZRouteEntity::getMerchantId, merchant.getId())
                .eq(ZRouteEntity::getProcessMode, processMode)
                .eq(ZRouteEntity::getPayCode, payCode)
                .eq(ZRouteEntity::getEnabled, 1)
                .eq(ZRouteEntity::getRouteType, "charge")
        );
        log.debug("路由条目 entries: {}", zRouteEntities);
        if (zRouteEntities.size() == 0) {
            throw new RenException("no channel available");
        }
        ChargeRouter chargeRouter = new ChargeRouter(zRouteEntities);
        return chargeRouter;
    }

    public WithdrawRouter getWithdrawRouter(String processMode, SysUserEntity merchant) {
        log.debug("加载代付路由[{}]: deptId:{}, merchantId:{}, processMode:{}", merchant.getUsername(), merchant.getDeptId(), merchant.getId(), processMode);
        List<ZRouteEntity> zRouteEntities = zRouteDao.selectList(Wrappers.<ZRouteEntity>lambdaQuery()
                .eq(ZRouteEntity::getDeptId, merchant.getDeptId())
                .eq(ZRouteEntity::getMerchantId, merchant.getId())
                .eq(ZRouteEntity::getProcessMode, processMode)
                .eq(ZRouteEntity::getEnabled, 1)
                .eq(ZRouteEntity::getRouteType, "withdraw")
        );
        if(zRouteEntities.size() == 0) {
            log.debug("[{}]没有配置代付路由", merchant.getUsername());
            throw new RenException("not withdraw routes");
        }
        return new WithdrawRouter(zRouteEntities);
    }

    public String getProcessMode(Long deptId) {
        return sysDeptDao.getById(deptId).getProcessMode();
    }

    public SysDeptEntity getDept(Long deptId) {
        SysDeptEntity byId = sysDeptDao.getById(deptId);
        return byId;
    }

    public SysUserEntity getSysUser(Long id) {
        return sysUserDao.selectById(id);
    }

    public List<ZRouteDTO> getRoutes(Long merchantId, String chargeOrWithdraw) {
        List<ZRouteEntity> routes = null;
        if(chargeOrWithdraw.equals("withdraw")) {
            routes = zRouteDao.selectList(Wrappers.<ZRouteEntity>lambdaQuery()
                    .eq(ZRouteEntity::getMerchantId, merchantId)
                            .eq(ZRouteEntity::getRouteType, "withdraw")
                    .eq(ZRouteEntity::getEnabled, 1)
            );
        } else {
            routes = zRouteDao.selectList(Wrappers.<ZRouteEntity>lambdaQuery()
                    .eq(ZRouteEntity::getMerchantId, merchantId)
                    .eq(ZRouteEntity::getRouteType, "charge")
                    .eq(ZRouteEntity::getEnabled, 1)
            );
        }
        return ConvertUtils.sourceToTarget(routes, ZRouteDTO.class);
    }
}
