package com.test.shardingjdbc.boot.bootsharding.service;

import com.google.common.collect.Ordering;
import com.test.shardingjdbc.boot.bootsharding.config.ShardingDataSourceConf;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author wangshuzheng
 * @date 2020/7/14 11:51 上午
 * @description
 */
@Service
public class ShardingService {

    private static final Logger logger = LoggerFactory.getLogger(ShardingService.class);

    @Getter
    private long max, min;

    @Getter
    private long countUseTimes;

    @Getter
    private int countTimes;

    @Autowired
    private List<Long> durationEnum;

    @Getter
    private long[] lessTop;

    private int lessMax;

    @Autowired
    private Queue<Long> queue;

    @Getter
    private HashMap<Long, List<Long>> duration;

    public void reset() {
        lessMax = 1 << ShardingDataSourceConf.twoMi;
        lessTop = new long[lessMax + 1];
        max = 0;
        min = Long.MAX_VALUE;
        countUseTimes = 0L;
        countTimes = 0;
        duration = new HashMap<>(1000);
        for (long du : durationEnum) {
            duration.put(du, new LinkedList<>());
        }
        for (int i = 0; i <= lessMax; i++) {
            lessTop[i] = 0;
        }
    }

    public void runCalculateThread() {
        new CalculateThread().start();
    }

    private void calculate(Long times) {
        if (times > max) {
            max = times;
        }
        if (times < min && times > 0) {
            min = times;
        }
        countTimes += 1;
        countUseTimes += times;
        if (times <= lessMax) {
            lessTop[Math.toIntExact(times)] += 1;
        } else {
            long mapIdx = durationEnum.get(0);
            for (int i = durationEnum.size() - 1; i >= 0; i--) {
                if (times > durationEnum.get(i)) {
                    if (i == durationEnum.size() - 1) {
                        mapIdx = durationEnum.get(i);
                    } else {
                        mapIdx = durationEnum.get(i + 1);
                    }
                    break;
                }
            }
            List<Long> multiset = duration.get(mapIdx);
            multiset.add(times);
            duration.put(mapIdx, multiset);
        }
    }

    class CalculateThread extends Thread {
        @Override
        public void run() {
            while (true) {
                Long times = queue.poll();
                if (times == null) {
                    Thread.yield();
                    continue;
                }
                calculate(times);
            }

        }
    }

    public Long calculateTpTime(long tp) {
        long oneHunPercent = (tp * countTimes) / 1000;
        long pos = (tp * countTimes) % 1000L > 0 ? oneHunPercent + 1 : oneHunPercent;
        int count = 0;
        for (int i = 0; i < lessTop.length; i++) {
            if (count + lessTop[i] > pos) {
                return (long) i;
            }
            count += lessTop[i];
        }
        List<Long> position = null;
        for (Long aLong : durationEnum) {
            int ds = duration.get(aLong).size();
            if (count + ds > pos) {
                position = duration.get(aLong);
                break;
            }
            count += ds;
        }

        if (null != position) {
            long ct = System.currentTimeMillis();
            try {
                position.removeIf(Objects::isNull);
                Ordering<Long> order = Ordering.natural();
                position = order.immutableSortedCopy(position);
                logger.info("sort cost time:{}", System.currentTimeMillis() - ct);
                int idx = (int) pos - count;
                if (idx > position.size()) {
                    idx = position.size() - 1;
                }
                return position.get(idx);
            } catch (Exception e) {
                logger.info("Exception--->{}", e.getMessage());
                int random = new Random().nextInt(position.size() - 1);
                return position.get(random);
            }
        }
        return 0L;
    }
}
