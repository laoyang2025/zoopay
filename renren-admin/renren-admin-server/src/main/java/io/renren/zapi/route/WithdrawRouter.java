package io.renren.zapi.route;

import io.renren.commons.tools.exception.RenException;
import io.renren.zadmin.entity.ZRouteEntity;
import io.renren.zadmin.entity.ZWithdrawEntity;

import java.util.*;

public class WithdrawRouter {

    private Integer totalSum;

    private List<Map.Entry<Long, Integer>> cumulatedList = null;

    private Map<Long, ZRouteEntity> routes = new HashMap<>();

    public WithdrawRouter(List<ZRouteEntity> list) {
        TreeMap<Long, Integer> cumulated = new TreeMap<>();
        totalSum = 0;
        for (ZRouteEntity item : list) {
            cumulated.put(item.getId(), totalSum += item.getWeight());
            routes.put(item.getId(), item);
        }
        this.cumulatedList = new ArrayList<>(cumulated.entrySet());
        this.cumulatedList.sort(Map.Entry.comparingByValue());
     }

    public ZRouteEntity select(ZWithdrawEntity entity) {
        if(cumulatedList == null || cumulatedList.isEmpty()) {
            throw new RenException("no routes");
        }
        Random random = new Random();
        int value = random.nextInt(totalSum);

        Long primary = null;
        for (Map.Entry<Long, Integer> entry : cumulatedList) {
            if (entry.getValue() > value) {
                primary = entry.getKey();
                break;
            }
        }
        if(primary == null) {
            throw new RenException("select withdraw route failed");
        }
        return routes.get(primary);
    }
}
