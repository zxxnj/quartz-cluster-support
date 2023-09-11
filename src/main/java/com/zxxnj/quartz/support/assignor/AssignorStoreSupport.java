package com.zxxnj.quartz.support.assignor;

import com.zxxnj.quartz.common.Constants;
import com.zxxnj.quartz.common.utils.DateUtil;
import com.zxxnj.quartz.common.utils.MathUtil;
import com.zxxnj.quartz.common.utils.NetUtil;
import com.zxxnj.quartz.support.DynamicSchedulerInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zxxnj
 */
@Component
@Slf4j
public class AssignorStoreSupport {

    @Resource(name = "quartzDataSource")
    private DataSource dataSource;

    public List<Integer> rangeAssign(int schedulerSize, int instanceCount) {
        return executeInNonManagedTxLock(Constants.SCH_LOCKS_NAME_SCHEDULE, conn -> {
            int numAssignorPerInstance;
            List<String> assignSchs = new ArrayList<>();
            List<Integer> ids = new ArrayList<>();
            //去重后的
            int assignInstances = 0;
            String instanceId = NetUtil.generateInstance();
            PreparedStatement preparedStatement;
            ResultSet rs;
            long currentTime = DateUtil.current();
            try {
                preparedStatement = conn.prepareStatement("update sch_instance set instance_id = null, pubts = null ,type = 0 where (" + currentTime + " - pubts ) > " + 3 * DynamicSchedulerInitializer.TASK_INTERVAL_DELAY + " or (pubts is null and instance_id not in (SELECT instance_id from sch_state));");
                preparedStatement.executeUpdate();

                preparedStatement = conn.prepareStatement("select id from sch_instance where instance_id ='" + instanceId + "';");
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    return null;
                }

                preparedStatement = conn.prepareStatement("select instance_id from sch_instance where instance_id is not null and instance_id !='';");
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    assignSchs.add(rs.getString(Constants.QUARTZ_SCHED_ASSIGNOR_INSTANCE_ID));
                }

                if (assignSchs != null && assignSchs.size() > 0) {
                    assignInstances = assignSchs.stream().distinct().collect(Collectors.toList()).size();
                }
                if (assignSchs.size() == schedulerSize || instanceCount == assignInstances) {
                    return null;
                }

                numAssignorPerInstance = MathUtil.divideToCeil(schedulerSize - assignSchs.size(), instanceCount - assignInstances);

                preparedStatement = conn.prepareStatement("select id from sch_instance where instance_id is null limit " + numAssignorPerInstance + ";");
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    ids.add(rs.getInt(Constants.QUARTZ_SCHED_ASSIGNOR_ID));
                }

                String inSql = ids.stream().map(String::valueOf).collect(Collectors.joining(Constants.QUARTZ_SCHED_ASSIGNOR_SEPARATOR));

                currentTime = DateUtil.current();
                preparedStatement = conn.prepareStatement("update sch_instance set instance_id = '" + instanceId + "' , pubts = " + currentTime + "  where id in (" + inSql + ");");
                preparedStatement.executeUpdate();

            } catch (Exception e) {
                log.error("quartz RangAssignor error.", e);
            }
            return ids;
        });
    }

    public List<Integer> stickyAssign() {
        return executeInNonManagedTxLock(Constants.SCH_LOCKS_NAME_SCHEDULE, conn -> {
            int numAssignorPerInstance;
            List<String> noAssignIds = new ArrayList<>();
            List<String> onlineInstanceId = new ArrayList<>();
            List<String> stickAssignIds;
            String stickAssignOnlineInstanceId;
            long currentTime;
            PreparedStatement preparedStatement;
            ResultSet rs;
            try {
                currentTime = DateUtil.current();
                preparedStatement = conn.prepareStatement("update sch_instance set instance_id = null, pubts = null ,type = 0 where (" + currentTime + " - pubts ) > " + 3 * DynamicSchedulerInitializer.TASK_INTERVAL_DELAY + " or (pubts is null and instance_id not in (SELECT instance_id from sch_state));");
                preparedStatement.executeUpdate();

                preparedStatement = conn.prepareStatement("select id from sch_instance where instance_id is null or instance_id ='';");
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    noAssignIds.add(rs.getString(Constants.QUARTZ_SCHED_ASSIGNOR_ID));
                }
                if (noAssignIds.isEmpty()) {
                    return null;
                }
                preparedStatement = conn.prepareStatement("select instance_id from sch_state where (" + currentTime + " - pubts ) < " + 3 * DynamicSchedulerInitializer.CLUSTER_INTERVAL_DELAY + ";");
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    onlineInstanceId.add(rs.getString(Constants.QUARTZ_SCHED_ASSIGNOR_INSTANCE_ID));
                }
                if (!onlineInstanceId.isEmpty()) {
                    for (int i = 0; i < onlineInstanceId.size(); i++) {
                        if (0 == onlineInstanceId.size() - i) {
                            return null;
                        }
                        numAssignorPerInstance = MathUtil.divideToCeil(noAssignIds.size(), onlineInstanceId.size() - i);
                        stickAssignIds = noAssignIds.subList(0, numAssignorPerInstance);
                        stickAssignOnlineInstanceId = onlineInstanceId.get(i);
                        String inSql = stickAssignIds.stream().collect(Collectors.joining(Constants.QUARTZ_SCHED_ASSIGNOR_SEPARATOR));
                        preparedStatement = conn.prepareStatement("update sch_instance set instance_id = '" + stickAssignOnlineInstanceId + "' ,type = 1  where id in (" + inSql + ");");
                        preparedStatement.executeUpdate();
                        noAssignIds = noAssignIds.subList(numAssignorPerInstance, noAssignIds.size());
                        if (noAssignIds.isEmpty()) {
                            return null;
                        }
                    }
                }
            } catch (Exception e) {
                log.error("quartz stickAssignor error.", e);
            }
            return null;
        });
    }

    public List<Integer> stickyRebalanced(Integer numAssignorPerInstance) {
        return executeInNonManagedTxLock(Constants.SCH_LOCKS_NAME_SCHEDULE, conn -> {
            List<Integer> ids = new ArrayList<>();
            long currentTime;
            PreparedStatement preparedStatement;
            ResultSet rs;
            try {
                preparedStatement = conn.prepareStatement("select id from sch_instance where type = 1  limit " + numAssignorPerInstance + ";");
                rs = preparedStatement.executeQuery();
                while (rs.next()) {
                    ids.add(rs.getInt(Constants.QUARTZ_SCHED_ASSIGNOR_ID));
                }

                String inSql = ids.stream().map(String::valueOf).collect(Collectors.joining(Constants.QUARTZ_SCHED_ASSIGNOR_SEPARATOR));
                currentTime = DateUtil.current();
                preparedStatement = conn.prepareStatement("update sch_instance set instance_id = '" + NetUtil.generateInstance() + "' , pubts = " + currentTime + " ,type = 0 where id in (" + inSql + ");");
                preparedStatement.executeUpdate();
            } catch (Exception e) {
                log.error("quartz stickyRebalanced error.", e);
            }
            return ids;
        });
    }


    protected interface TransactionCallback<T> {
        /**
         * 回调执行
         *
         * @param conn
         * @return
         * @throws Exception
         */
        T execute(Connection conn) throws Exception;
    }

    protected <T> T executeInNonManagedTxLock(
            String lockName,
            TransactionCallback<T> txCallback) {
        Boolean connAutoCommit = null;
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            conn = dataSource.getConnection();
            connAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);

            if (lockName != null) {
                preparedStatement = conn.prepareStatement("select lock_name from sch_locks where lock_name = '" + lockName + "' for update");
                preparedStatement.execute();
            }
            final T result = txCallback.execute(conn);
            return result;
        } catch (Exception e) {
            log.error("executeInNonManagedTxLock error.", e);
        } finally {
            commitConnection(conn, connAutoCommit);
        }
        return null;
    }


    protected void commitConnection(Connection conn, boolean connAutoCommit) {
        if (conn != null) {
            try {
                conn.commit();
            } catch (SQLException e) {
                log.error("Couldn't commit jdbc connection.", e);
                rollbackConnection(conn);
            }
            try {
                conn.setAutoCommit(connAutoCommit);
            } catch (SQLException e) {
                log.error("Couldn't setAutoCommit.", e);
            }
            try {
                conn.close();
            } catch (SQLException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    protected void rollbackConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                log.error("Couldn't rollback jdbc connection. "+e.getMessage(), e);
            }
        }
    }

}
