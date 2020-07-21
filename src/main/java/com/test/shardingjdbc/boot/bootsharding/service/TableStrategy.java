package com.test.shardingjdbc.boot.bootsharding.service;

import javax.sql.DataSource;

/**
 * @author wangshuzheng
 * @date 2020/7/20 11:40 上午
 * @description
 */
public interface TableStrategy {

    void insert(final DataSource dataSource, final int starter, final String varcharMsg, final String sql);

    void query(final DataSource dataSource, final int starter, final String sql);

}
