package com.test.shardingjdbc.boot.bootsharding.controller;

import com.alibaba.fastjson.JSON;
import com.test.shardingjdbc.boot.bootsharding.service.ShardingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wangshuzheng
 * @date 2020/7/17 4:40 下午
 * @description
 */
@RestController
public class QueryController {

    @Autowired
    private ShardingService shardingService;

    @GetMapping("/show/{slice}")
    public String showSliceData(@PathVariable(name = "slice") int slice) {
        HashMap<Long, List<Long>> duration = shardingService.getDuration();
        for (Long key : duration.keySet()) {
            if (key == slice) {
                return JSON.toJSONString(duration.get(key));
            }
        }
        return "not found";
    }

    @GetMapping("/show")
    public String showDatas() {
        Map<String, Object> result = new HashMap<>(10);
        result.put("最小值", shardingService.getMin());
        result.put("最大值", shardingService.getMax());
        result.put("平均值", shardingService.getCountUseTimes() / shardingService.getCountTimes());
        result.put("TP50", shardingService.calculateTpTime(500L));
        result.put("TP90", shardingService.calculateTpTime(900L));
        result.put("TP99", shardingService.calculateTpTime(990L));
        result.put("TP999", shardingService.calculateTpTime(999L));
        result.put("TotalUseTime", shardingService.getCountUseTimes());
        result.put("TotalRequest", shardingService.getCountTimes());
        return JSON.toJSONString(result);
    }

    @GetMapping("/list")
    public String listDatas() {
        Map<Object, Object> result = new HashMap<>(2500);
        long[] lastTop = shardingService.getLessTop();
        for (int i = 0; i < lastTop.length; i++) {
            result.put(i, lastTop[i]);
        }
        HashMap<Long, List<Long>> duration = shardingService.getDuration();
        for (Long key : duration.keySet()) {
            result.put(key, duration.get(key).size());
        }
        return JSON.toJSONString(result);
    }

}
