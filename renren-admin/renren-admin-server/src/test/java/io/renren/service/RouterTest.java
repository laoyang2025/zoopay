package io.renren.service;


import io.renren.commons.tools.exception.RenException;
import io.renren.zadmin.entity.ZCardLogEntity;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZRouteEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;
import io.renren.zapi.route.ChargeRouter;
import io.renren.zapi.route.WithdrawRouter;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RouterTest {

    private void add(long id, int weight, List<ZRouteEntity> list) {
        ZRouteEntity e;
        e = new ZRouteEntity();
        e.setWeight(weight);
        e.setId(id);
        list.add(e);
    }

    @Test
    public void chargeRouterTest() {
        List<ZRouteEntity> list = new ArrayList<>();

//        add(1L, 100, list);
//        add(2L, 200, list);
//        add(3L, 300, list);
//        add(4L, 400, list);

        ChargeRouter router = new ChargeRouter(list);

        Map<Long, Integer> stat = new HashMap<>();

        for (int i = 0; i < 1000; i++) {
            List<ZRouteEntity> select = router.select(new ZChargeEntity());
//            System.out.println("----------------------------------");
//            for (Long aLong : select) {
//                System.out.println(aLong);
//            }
            ZRouteEntity route = select.get(0);
            Integer integer = stat.get(route.getWeight());
            if (integer == null) {
                stat.put(route.getId(), 1);
            } else {
                stat.put(route.getId(), integer + 1);
            }
        }
        stat.forEach((k, v) -> System.out.println(k + "->" + v));
    }

    @Test
    public void withdrawRouterTest() {
        List<ZRouteEntity> list = new ArrayList<>();
        add(1L, 100, list);
        add(2L, 200, list);
        add(3L, 300, list);
        add(4L, 400, list);

        WithdrawRouter router = new WithdrawRouter(list);

        Map<Long, Integer> stat = new HashMap<>();

        for (int i = 0; i < 1000; i++) {
            ZRouteEntity route = router.select(new ZWithdrawEntity());
        }
        stat.forEach((k, v) -> System.out.println(k + "->" + v));
    }

    @Resource
    private TransactionTemplate tx;
    @Test
    public void txTest() {
        System.out.println(new ZCardLogEntity());
    }

}
