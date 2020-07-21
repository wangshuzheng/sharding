package com.test.shardingjdbc.boot.bootsharding.config;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.sql.DataSource;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author wangshuzheng
 * @date 2020/7/9 3:20 下午
 * @description
 */
@PropertySource({"classpath:db.properties"})
@Configuration
public class ShardingDataSourceConf {

    public static int twoMi = 9;

    @Value("${mysql.password}")
    private String passwrod;

    @Value("${mysql.user}")
    private String user;

    @Value("${mysql.url.second}")
    private String url1;

    @Value("${mysql.url.first}")
    private String url2;

    @Value("${mysql.driver}")
    private String driver;

    @Value("${mysql.database.prefix}")
    private String dbPrefix;

    @Bean(name = "ds0", destroyMethod = "close")
    public BasicDataSource dataSource1() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(driver);
        basicDataSource.setUrl(url1 + dbPrefix + "0");
        basicDataSource.setUsername(user);
        basicDataSource.setPassword(passwrod);
        return basicDataSource;
    }

    @Bean(name = "queue")
    public Queue<Long> queue() {
        return new ConcurrentLinkedQueue<>();
//        return new LinkedBlockingQueue<>();
//        return new SynchronousQueue<>();
    }

    @Bean(name = "durationEnum")
    public List<Long> durationEnum() {
        int max = 10;
        List<Long> set = new ArrayList<>();
        for (int i = twoMi - 1; i <= max; i++) {
            set.add((long) (2 << i));
        }
        set.add((long) (2 << max) + 1);
        return set;
    }

    @Bean(name = "ds1", destroyMethod = "close")
    public BasicDataSource dataSource2() {
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(driver);
        basicDataSource.setUrl(url2 + dbPrefix + "1");
        basicDataSource.setUsername(user);
        basicDataSource.setPassword(passwrod);
        return basicDataSource;
    }

    @Bean("sharingApiDataSource")
    public DataSource sharingApiDataSource() throws Exception {
        List<String> urls = new ArrayList<>();
        urls.add(url1);
        urls.add(url2);
        return ShardingCoreApiConfig.createMSEncShardingDataSource(user, urls, passwrod, dbPrefix);
    }

}
