package com.zxxnj.quartz.support;

import com.zxxnj.quartz.common.Constants;
import com.zxxnj.quartz.common.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * 初始化表结构
 *
 * @author zxxnj
 */
@Component
@Slf4j
public class InitializeSchema {

    @Resource(name = "quartzDataSource")
    private DataSource dataSource;

    @Value("${scheduler.size:20}")
    private int schedulerSize;

    public void createTable() {
        initTable(Constants.TABLE_SCH_INSTANCE, () -> createTableSchInstance());
        initTable(Constants.TABLE_SCH_STATE, () -> createTableSchState());
        initTable(Constants.TABLE_SCH_LOCKS, () -> createTableSchLocks());
    }

    public void initTable(String tableName, Runnable runnable) {
        String checkSql = "select count(*) from " + tableName;
        try (Connection conn = dataSource.getConnection(); Statement stat = conn.createStatement()) {
            stat.execute(checkSql);
        } catch (Exception e) {
            log.error("initTable:{}, error:{}", tableName, e);
            runnable.run();
        }
    }

    public void createTableSchInstance() {
        String initSql = "CREATE TABLE `sch_instance` (" +
                "  `id` int(3) NOT NULL ," +
                "  `instance_id` varchar(255)  DEFAULT NULL COMMENT '实例id'," +
                "  `pubts` bigint(20) DEFAULT NULL COMMENT '上报时间'," +
                "  `type` int(3) DEFAULT '1' COMMENT '类别'," +
                "  PRIMARY KEY (`id`) " +
                ") ENGINE=InnoDB COMMENT='scheduler分配';";
        try (Connection conn = dataSource.getConnection(); Statement stat = conn.createStatement()) {
            stat.execute(initSql);
            String schInstanceSql = getSchInstanceSql();
            stat.execute(schInstanceSql);
        } catch (Exception e) {
            log.error("createTableSchInstance error.", e);
        }
    }

    public String getSchInstanceSql() {
        String insertSql = "INSERT INTO `sch_instance` (`id`) VALUES (#{});";
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= schedulerSize; i++) {
            stringBuilder.append(insertSql.replace("#{}", Integer.toString(i)));
        }
        return stringBuilder.toString();
    }


    public void createTableSchState() {
        String initSql = "CREATE TABLE `sch_state` ( " +
                "  `instance_id` varchar(200)  NOT NULL, " +
                "  `pubts` bigint(20) DEFAULT NULL, " +
                "  PRIMARY KEY (`instance_id`) " +
                ") ENGINE=InnoDB COMMENT='实例信息';";
        try (Connection conn = dataSource.getConnection(); Statement stat = conn.createStatement()) {
            stat.execute(initSql);
        } catch (Exception e) {
            log.error("createTableSchState error.", e);
        }
    }

    public void createTableSchLocks() {
        String initSql = "CREATE TABLE `sch_locks` ( " +
                "  `lock_name` varchar(120)  NOT NULL, " +
                "  PRIMARY KEY (`lock_name`) " +
                ") ENGINE=InnoDB COMMENT='sch锁';";
        try (Connection conn = dataSource.getConnection(); Statement stat = conn.createStatement()) {
            stat.execute(initSql);
            String schLocksSql = "INSERT INTO `sch_locks` (`lock_name`) VALUES ('schedule_lock');";
            stat.execute(schLocksSql);
        } catch (Exception e) {
            log.error("createTableSchLocks error.", e);
        }
    }

    public void initSchedulerGroupTable(int number) {
        String tableName = "QRTZ" + number + "_triggers";
        String checkSql = "select count(*) from " + tableName;
        try (Connection conn = dataSource.getConnection(); Statement stat = conn.createStatement();
             InputStream inputStream = new ClassPathResource("org/quartz/impl/jdbcjobstore/tables_mysql_innodb.sql").getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            try {
                stat.execute(checkSql);
            } catch (SQLException e) {
                log.error("initSchedulerGroup:{}, error:{}", tableName, e);
                String sql = reader.lines().collect(Collectors.joining("\n"));
                sql = sql.replace("QRTZ", "QRTZ" + number);
                stat.execute(sql);
            }
        } catch (Exception e) {
            log.error("initSchedulerGroup error.", e);
        }
    }

}
