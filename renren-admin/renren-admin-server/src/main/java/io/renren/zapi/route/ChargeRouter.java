package io.renren.zapi.route;

import cn.hutool.core.lang.Pair;
import io.renren.zadmin.entity.ZChargeEntity;
import io.renren.zadmin.entity.ZRouteEntity;
import io.renren.zapi.ZooConstant;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;


@Slf4j
public class ChargeRouter {
    private int totalSum = 0;
    private int totalSumLarge = 0;
    private int totalSumAll = 0;
    private BigDecimal bigAmount = null;


    private Map<Long, ZRouteEntity> routes = new HashMap<>();
    private Map<Long, ZRouteEntity> routesLarge = new HashMap<>();
    private Map<Long, ZRouteEntity> routesAll = new HashMap<>();

    List<Map.Entry<Long, Integer>> cumulatedList = null;
    List<Map.Entry<Long, Integer>> cumulatedLargeList = null;
    List<Map.Entry<Long, Integer>> cumulatedAllList = null;

    public ChargeRouter(List<ZRouteEntity> list) {
        TreeMap<Long, Integer> cumulated = new TreeMap<>();
        TreeMap<Long, Integer> cumulatedLarge = new TreeMap<>();
        TreeMap<Long, Integer> cumulatedAll = new TreeMap<>();
        totalSum = 0;
        totalSumLarge = 0;
        for (ZRouteEntity item : list) {
            if (item.getBigAmount() != null) {
                // 大金额路由
                routesLarge.put(item.getId(), item);
                cumulatedLarge.put(item.getId(), totalSumLarge += item.getWeight());
                if( bigAmount != null) {
                    bigAmount = item.getBigAmount();
                }
            } else {
                // 其他金额路由
                cumulated.put(item.getId(), totalSum += item.getWeight());
                routes.put(item.getId(), item);
            }
            // 所有金额路由
            cumulatedAll.put(item.getId(), totalSumAll += item.getWeight());
            routesAll.put(item.getId(), item);
        }

        this.cumulatedList = new ArrayList<>(cumulated.entrySet());
        this.cumulatedList.sort(Map.Entry.comparingByValue());

        this.cumulatedLargeList = new ArrayList<>(cumulated.entrySet());
        this.cumulatedLargeList.sort(Map.Entry.comparingByValue());

        this.cumulatedAllList = new ArrayList<>(cumulated.entrySet());
        this.cumulatedAllList.sort(Map.Entry.comparingByValue());
    }

    public List<ZRouteEntity> select(ZChargeEntity chargeEntity) {
        if (routesAll.isEmpty()) {
            log.info("没有路由条目");
            return List.of();
        }

        List<ZRouteEntity> rtn = new ArrayList<>();

        // 有大金额路由, 且当前金额大于这个金额, 优先走大金额
        if (bigAmount != null && chargeEntity.getAmount().compareTo(bigAmount) >= 0) {
            log.info("尝试大金额路由...");
            Pair<Long, List<Long>> primaryAndOther = getPrimaryAndOther(totalSumLarge, cumulatedLargeList);
            Long primary = primaryAndOther.getKey();
            List<Long> others = primaryAndOther.getValue();
            Collections.shuffle(others);
            ZRouteEntity primaryRoute = routesAll.get(primary);
            rtn.add(primaryRoute);

            // 卡路由的话, 没有fallback, 宣到卡就结束了
            if (primaryRoute.getProcessMode().equals(ZooConstant.PROCESS_MODE_CARD)) {
                return rtn;
            }

            // 然后看其他渠道
            for (Long other : others) {
                rtn.add(routesAll.get(other));
            }

            // 小金额路由
            if (routes.size() > 0) {
                primaryAndOther = getPrimaryAndOther(totalSum, cumulatedList);
                primary = primaryAndOther.getKey();
                others = primaryAndOther.getValue();
                Collections.shuffle(others);

                rtn.add(routesAll.get(primary));
                for (Long other : others) {
                    rtn.add(routesAll.get(other));
                }

            }
            return rtn;
        }

        // 没有大金额路由, 或者虽然有大金额路由， 但是当前金额不大
        Pair<Long, List<Long>> primaryAndOther = getPrimaryAndOther(totalSumAll, cumulatedAllList);
        Long primary = primaryAndOther.getKey();
        ZRouteEntity primaryRoute = routesAll.get(primary);
        if(primaryRoute.getProcessMode().equals(ZooConstant.PROCESS_MODE_CARD)) {
            rtn.add(primaryRoute);
            return rtn;
        }

        List<Long> others = primaryAndOther.getValue();
        Collections.shuffle(others);

        rtn.add(routesAll.get(primary));
        for (Long other : others) {
            rtn.add(routesAll.get(other));
        }
        return rtn;
    }

    private Pair<Long, List<Long>> getPrimaryAndOther(int totalSumValue, List<Map.Entry<Long, Integer>> cumulatedValue) {
        Random random = new Random();
        Long primary = null;
        List<Long> others = new ArrayList<>();

        StringBuilder sb = new StringBuilder() ;

        int value = random.nextInt(totalSumValue);
        for (Map.Entry<Long, Integer> longIntegerEntry : cumulatedValue) {
            Long key = longIntegerEntry.getKey();
            int cum = longIntegerEntry.getValue();

            sb.append(cum).append("|");

            // 已经选了primary的情况下, 其他都是others
            if (primary != null) {
                others.add(key);
                continue;
            }
            // 选中primary
            if (cum > value) {
                primary = key;
            } else {
                others.add(key);
            }
        }

        log.debug("possibility string:{}, random:{}", sb.toString(), value);
        return Pair.of(primary, others);
    }
}
