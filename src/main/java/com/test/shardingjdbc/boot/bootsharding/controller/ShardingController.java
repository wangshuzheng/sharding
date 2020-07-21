package com.test.shardingjdbc.boot.bootsharding.controller;

import com.test.shardingjdbc.boot.bootsharding.service.ShardingService;
import com.test.shardingjdbc.boot.bootsharding.service.TableStrategy;
import com.test.shardingjdbc.boot.bootsharding.sql.Sqls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.Executors.*;

/**
 * @author wangshuzheng
 * @date 2020/7/9 4:20 下午
 * @description
 */
@RestController
public class ShardingController {

    private static final Logger logger = LoggerFactory.getLogger(ShardingController.class);

    @Autowired
    private DataSource sharingApiDataSource;

    @Autowired
    private ShardingService shardingService;

    @Autowired
    private TableStrategy largeTS;

    @Autowired
    private TableStrategy littleTS;

    @Value("${mysql.database.prefix}")
    private String dbPrefix;

    private int queryThreadStart = 0;

    private void beforesBe() {
        shardingService.reset();
        if (queryThreadStart == 0) {
            shardingService.runCalculateThread();
            queryThreadStart = 1;
        }
    }

    public void addBefore(int size, int thsize) {
        logger.info("add . size per thread {}, thread size {}", size, thsize);
        beforesBe();
    }

    private void addBefore(int thsize) {
        logger.info("query thread size {}", thsize);
        beforesBe();
    }

    @GetMapping("/add/{size}/{thSize}")
    public void insertData(@PathVariable(name = "size") int size, @PathVariable(name = "thSize") int thsize) {
        addBefore(size, thsize);
        int systemAvailableProcessors = Runtime.getRuntime().availableProcessors();
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(systemAvailableProcessors,
                systemAvailableProcessors * 2,
                60, TimeUnit.MILLISECONDS, new SynchronousQueue<>(), Thread::new,
                new ThreadPoolExecutor.CallerRunsPolicy());
        TableStrategy ts = largeTS;
        if (dbPrefix.equalsIgnoreCase("dsl")) {
            ts = littleTS;
        }
        for (int i = 0; i < thsize; i++) {
            threadPool.execute(new InsertRunning(i, size, ts));
        }
        threadPool.shutdown();
        logger.info("insert size {}, startTime {}", thsize * size, System.currentTimeMillis());
    }

    @GetMapping("/query/{thsize}/{sizept}")
    public void queryData(@PathVariable(name = "thsize") int thsize, @PathVariable(name = "sizept") int sizePt) {
        List<Runnable> tasks = new ArrayList<>(thsize);
        addBefore(thsize);
        for (int i = 0; i < thsize; i++) {
            tasks.add(() -> {
                String sql = Sqls.SINGLE_ROUTE_SEARCH;
                TableStrategy ts = largeTS;
                if (dbPrefix.equalsIgnoreCase("dsl")) {
                    sql = Sqls.LT_SINGLE_ROUTE_SEARCH;
                    ts = littleTS;
                }
                for (int j = 0; j < sizePt; j++) {
                    int starter = new Random().nextInt(100000000 - 1);
                    ts.query(sharingApiDataSource, starter, sql);
                }
                logger.info("query Size {}, endTime = {}", sizePt, System.currentTimeMillis());
            });
        }

        ExecutorService executorService = newCachedThreadPool(Thread::new);
        for (int i = 0; i < thsize; i++) {
            executorService.execute(tasks.get(i));
        }
        tasks.clear();
        executorService.shutdown();
    }

    /**
     * 线程执行内容
     */
    class InsertRunning implements Runnable {
        private int i;
        private int size;
        private TableStrategy tableStrategy;

        public InsertRunning(int i, int size, TableStrategy tableStrategy) {
            this.i = i;
            this.size = size;
            this.tableStrategy = tableStrategy;
        }

        @Override
        public void run() {
            int max = (i + 1) * size;
            int starter = i * size + 1;
            int idx = new Random().nextInt(59);
            String varcharMsg = varcharMsg().get(idx);
            String sql = Sqls.INSERT;
            if (dbPrefix.equalsIgnoreCase("dsl")) {
                sql = Sqls.LITTLE_T_INSERT;
            }
            for (; starter <= max; starter++) {
                tableStrategy.insert(sharingApiDataSource, starter, varcharMsg, sql);
            }
        }
    }

    private List<String> varcharMsg() {
        List<String> varchars = new ArrayList<>(80);
        String chars = "abcdefghijklmnopqrstuvwxyz1234567890";
        for (int i = 0; i < 60; i++) {
            int length = chars.length();
            StringBuilder sb;
            sb = new StringBuilder();
            for (int j = 0; j < 50; j++) {
                sb.append(chars.charAt(new Random().nextInt(length - 1)));
            }
            varchars.add(sb.toString());
        }
        return varchars;
    }
}
