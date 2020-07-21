package com.test.shardingjdbc.boot.bootsharding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Queue;

/**
 * @author wangshuzheng
 * @date 2020/7/20 11:43 上午
 * @description
 */
@Service("littleTS")
public class LittleTableStrategy implements TableStrategy {

    private static final Logger logger = LoggerFactory.getLogger(LittleTableStrategy.class);

    @Autowired
    private Queue<Long> queue;

    @Override
    public void insert(final DataSource dataSource, final int starter, final String varcharMsg, final String sql) {
        long st = System.currentTimeMillis();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, starter);
            preparedStatement.setLong(2, starter);
            preparedStatement.setInt(3, starter);
            preparedStatement.setString(4, "jim" + starter);
            preparedStatement.setString(5, varcharMsg);
            preparedStatement.setString(6, varcharMsg);
            preparedStatement.execute();
            if (!queue.offer(System.currentTimeMillis() - st)) {
                logger.info("add in queue fail");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void query(final DataSource dataSource, final int starter, final String sql) {
        long st = System.currentTimeMillis();
        try (Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            preparedStatement.setLong(1, starter);
            preparedStatement.setLong(2, starter);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    rs.getInt(1);
                }
            }
            if (!queue.offer(System.currentTimeMillis() - st)) {
                logger.info("add in queue fail");
            }
        } catch (SQLException e) {
            logger.error("Exception--->{}", e.getMessage(), e);
        }
    }
}
