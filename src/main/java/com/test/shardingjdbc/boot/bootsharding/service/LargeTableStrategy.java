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
@Service("largeTS")
public class LargeTableStrategy implements TableStrategy {

    private static final Logger logger = LoggerFactory.getLogger(LargeTableStrategy.class);

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
            preparedStatement.setInt(4, starter);
            preparedStatement.setInt(5, starter);
            preparedStatement.setString(6, "jim" + starter);
            preparedStatement.setString(7, varcharMsg);
            preparedStatement.setString(8, varcharMsg);
            preparedStatement.setString(9, varcharMsg);
            preparedStatement.setString(10, varcharMsg);
            preparedStatement.setString(11, varcharMsg);
            preparedStatement.setString(12, varcharMsg);
            preparedStatement.setString(13, varcharMsg);
            preparedStatement.setString(14, varcharMsg);
            preparedStatement.setString(15, varcharMsg);
            preparedStatement.setString(16, varcharMsg);
            preparedStatement.setString(17, varcharMsg);
            preparedStatement.setString(18, varcharMsg);
            preparedStatement.setString(19, varcharMsg);
            preparedStatement.setString(20, varcharMsg);
            preparedStatement.setString(21, varcharMsg);
            preparedStatement.setString(22, varcharMsg);
            preparedStatement.setString(23, varcharMsg);
            preparedStatement.setString(24, varcharMsg);
            preparedStatement.setString(25, varcharMsg);
            preparedStatement.setString(26, varcharMsg);
            preparedStatement.setString(27, varcharMsg);
            preparedStatement.setString(28, varcharMsg);
            preparedStatement.setString(29, varcharMsg);
            preparedStatement.setString(30, varcharMsg);
            preparedStatement.setString(31, varcharMsg);
            preparedStatement.setString(32, varcharMsg);
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
