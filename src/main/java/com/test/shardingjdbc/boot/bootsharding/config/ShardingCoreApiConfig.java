package com.test.shardingjdbc.boot.bootsharding.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.api.config.sharding.KeyGeneratorConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.NoneShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.*;

/**
 * @author wangshuzheng
 * @date 2020/7/13 7:03 下午
 * @description
 */
public class ShardingCoreApiConfig {

    private static int processors = Runtime.getRuntime().availableProcessors();
    private static int poolSize = processors * 4;

    public static DataSource createDataSource(final String usrName, final String dataSourceName, final String url, final String password) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl(String.format("%s%s?useSSL=false&serverTimezone=UTC", url, dataSourceName));
        config.setUsername(usrName);
        config.setPassword(password);
        config.setConnectionInitSql("select 1");
        config.setMinimumIdle(poolSize);
        config.setMaximumPoolSize(poolSize);
        config.addDataSourceProperty("useServerPrepStmts", Boolean.TRUE.toString());
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", 500);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useLocalSessionState", Boolean.TRUE.toString());
        config.addDataSourceProperty("rewriteBatchedStatements", Boolean.TRUE.toString());
        config.addDataSourceProperty("cacheResultSetMetadata", Boolean.TRUE.toString());
        config.addDataSourceProperty("cacheServerConfiguration", Boolean.TRUE.toString());
        config.addDataSourceProperty("elideSetAutoCommits", Boolean.TRUE.toString());
        config.addDataSourceProperty("maintainTimeStats", Boolean.FALSE.toString());
        config.addDataSourceProperty("netTimeoutForStreamingResults", 0);
//        config.addDataSourceProperty("maxLiftTime", 0);
        config.addDataSourceProperty("connectionTimeout", 30);
        DataSource dataSource = new HikariDataSource(config);
        return dataSource;
    }

    /**
     * create sharding datasource map.
     *
     * @return datasource map
     */
    private static Map<String, DataSource> createShardingDataSourceMap(final String usrName, final List<String> urls
            , final String password, final String prefix) {
        Map<String, DataSource> result = new HashMap<>(5);
        result.put(prefix + "0", createDataSource(usrName, prefix + "0", urls.get(0), password));
        result.put(prefix + "1", createDataSource(usrName, prefix + "1", urls.get(1), password));
        return result;
    }

    /**
     * create datasource for master slave & encrypt & sharding scene.
     *
     * @return datasource
     * @throws SQLException sqlexception
     */
    public static DataSource createMSEncShardingDataSource(final String usrName, final List<String> urls
            , final String password, final String prefix) throws SQLException {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration(prefix, prefix + "${0..1}.test32");
        tableRuleConfig.setDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("col01", prefix + "${col01 % 2}"));
        tableRuleConfig.setTableShardingStrategyConfig(new NoneShardingStrategyConfiguration());
        tableRuleConfig.setKeyGeneratorConfig(getKeyGeneratorConfiguration());
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        shardingRuleConfig.getBindingTableGroups().add(prefix);
        Properties properties = new Properties();
//        properties.setProperty("max.connections.size.per.query", String.valueOf(processors * 15));
        properties.setProperty("max.connections.size.per.query", "200");
//        properties.setProperty("executor.size", String.valueOf(processors * 30));
        properties.setProperty("executor.size", "200");
        return ShardingDataSourceFactory.createDataSource(createShardingDataSourceMap(usrName, urls, password, prefix),
                shardingRuleConfig, properties);
    }

    /**
     * get key generator config.
     *
     * @return keyGeneratorConfiguration
     */
    private static KeyGeneratorConfiguration getKeyGeneratorConfiguration() {
        return new KeyGeneratorConfiguration("SNOWFLAKE", "id", new Properties());
    }

}
