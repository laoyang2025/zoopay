package io.renren.zapi;

import io.renren.entity.SysDeptEntity;
import io.renren.zadmin.dao.ZWarningDao;
import io.renren.zadmin.entity.ZWarningEntity;
import io.renren.zapi.route.RouteService;
import io.renren.zsocket.SocketAdmin;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class AlarmService {

    @Resource
    private ZWarningDao zWarningDao;

    @Resource
    private RouteService routeService;
    /**
     * 系统告警
     */
    public void warn(Long deptId, String msgType, String prompt) {
        // 入库z_warning
        SocketAdmin.sendMessage(deptId, ZooConstant.MSG_TYPE_WARN, prompt, null);
        ZWarningEntity entity = new ZWarningEntity();
        SysDeptEntity dept = routeService.getDept(deptId);
        entity.setDeptId(deptId);
        entity.setDeptName(dept.getName());
        entity.setMsg(prompt);
        entity.setMsgType(msgType);
        zWarningDao.insert(entity);
    }
}
