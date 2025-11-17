package io.renren.zapi.channel.channels;

import io.renren.commons.tools.exception.RenException;
import io.renren.entity.SysUserEntity;
import io.renren.zadmin.entity.ZRouteEntity;
import io.renren.zapi.ZooConstant;
import io.renren.zapi.agent.AgentService;
import io.renren.zapi.ant.AntService;
import io.renren.zapi.card.CardService;
import io.renren.zapi.channel.dto.ChannelChargeResponse;
import io.renren.zapi.channel.dto.ChannelContext;
import io.renren.zapi.channel.dto.ChannelWithdrawResponse;
import io.renren.zapi.channel.PayChannel;
import io.renren.zapi.merchant.ApiContext;
import io.renren.zapi.route.ChargeRouter;
import io.renren.zapi.route.RouteService;
import io.renren.zapi.route.WithdrawRouter;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 本地内部渠道
 */
@Service
@Slf4j
public class LocalChannel implements PayChannel {
    private ChannelContext context;
    @Resource
    private RouteService routeService;
    @Resource
    private AgentService agentService;
    @Resource
    private AntService antService;
    @Resource
    private CardService cardService;

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public ChannelContext getContext() {
        return context;
    }

    @Override
    public void setContext(ChannelContext context) {
        this.context = context;
    }

    @Override
    public ChannelChargeResponse charge(ZChargeEntity chargeEntity) {
        ApiContext apiContext = ApiContext.getContext();
        String mode = apiContext.getDept().getProcessMode();
        log.debug("本地渠道尝试加载路由...");
        ChargeRouter chargeRouter = routeService.getChargeRouter(mode, chargeEntity.getPayCode());
        // 代理跑分
        if(mode.equals(ZooConstant.PROCESS_MODE_AGENT)) {
            List<ZRouteEntity> selected = chargeRouter.select(chargeEntity);
            return agentService.charge(chargeEntity, selected);
        }
        // 自营卡
        if(mode.equals(ZooConstant.PROCESS_MODE_CARD)) {
            List<ZRouteEntity> selected = chargeRouter.select(chargeEntity);
            log.debug("自营卡模式, selected: {}", selected);
            return cardService.charge(chargeEntity, selected);
        }
        // 码农跑分
        if(mode.equals(ZooConstant.PROCESS_MODE_ANT)) {
            return antService.charge(chargeEntity);
        }
        throw new RenException("unsupported process mode");
    }

    @Override
    public ChannelWithdrawResponse withdraw(ZWithdrawEntity withdrawEntity, SysUserEntity merchant) {
        String mode = routeService.getProcessMode(withdrawEntity.getDeptId());
        WithdrawRouter withdrawRouter = routeService.getWithdrawRouter(mode, merchant);

        // 代理跑分
        if(mode.equals(ZooConstant.PROCESS_MODE_AGENT)) {
            ZRouteEntity selected = withdrawRouter.select(withdrawEntity);
            log.debug("本地代付路由-代理跑分: {}", selected);
            return agentService.withdraw(withdrawEntity, selected.getObjectId());
        }
        // 自营卡
        if(mode.equals(ZooConstant.PROCESS_MODE_CARD)) {
            ZRouteEntity selected = withdrawRouter.select(withdrawEntity);
            log.debug("本地代付路由-自营卡: {}", selected);
            return cardService.withdraw(withdrawEntity, selected.getObjectId());
        }
        // 码农跑分
        if(mode.equals(ZooConstant.PROCESS_MODE_ANT)) {
            log.debug("本地代付路由-跑分");
            return antService.withdraw(withdrawEntity);
        }
        throw new RenException("unsupported process mode");
    }
}
