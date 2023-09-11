package com.zxxnj.quartz.support.dao;

import org.apache.ibatis.annotations.*;




/**
 * @author zxxnj
 */
@Mapper
public interface SchStateRepository {

    /**
     * 上报信息
     *
     * @param instanceId
     * @param pubts
     * @return
     */
    @Insert("INSERT INTO `sch_state` (`instance_id`, `pubts`) " +
            "VALUES (#{instanceId}, #{pubts})")
    int insertSchState(@Param("instanceId") String instanceId, @Param("pubts") long pubts);

    /**
     * 更新上报信息
     *
     * @param instanceId
     * @param pubts
     * @return
     */
    @Update("update sch_state set  pubts = #{pubts} where instance_id = #{instanceId};")
    int updateSchState(@Param("instanceId") String instanceId, @Param("pubts") long pubts);

    /**
     *  删除
     * @param currentTime
     * @param idleTime
     * @return
     */
    @Delete("delete from sch_state where (#{currentTime} - pubts ) > #{idleTime};")
    int deleteSchState(@Param("currentTime") long currentTime, @Param("idleTime") long idleTime);

}
