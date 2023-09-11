package com.zxxnj.quartz.support.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 *
 * @author zxxnj
 */
@Mapper
public interface SchInstanceRepository {

    /**
     * 清除上报信息
     * @param currentTime
     * @param idleTime
     * @return
     */
    @Update("update sch_instance set instance_id = null, pubts = null, type = 0 where (#{currentTime} - pubts ) > #{idleTime} ;")
    int clearHealthReport(@Param("currentTime") long currentTime, @Param("idleTime") long idleTime);


    /**
     * 更新上报信息
     * @param currentTime
     * @param instanceId
     * @return
     */
    @Update("update sch_instance set pubts = #{currentTime} where instance_id = #{instanceId} and (pubts is not null or pubts !='');")
    int updateHealthReport(@Param("currentTime") long currentTime, @Param("instanceId") String instanceId);

    /**
     *  统计
     * @param currentTime
     * @param idleTime
     * @return
     */
    @Select("select count(*) from sch_instance where (#{currentTime} - pubts ) < #{idleTime};")
    int countSchInstance(@Param("currentTime") long currentTime, @Param("idleTime") long idleTime);

    /**
     * 查找当前实例未分配的id
     * @param instanceId
     * @return
     */
    @Select("select id from sch_instance where instance_id = #{instanceId} and (pubts is  null or pubts ='');")
    List<Integer> lookupIdForNotAssign(String instanceId);

    /**
     *  更新当前实例未分配id
     * @param currentTime
     * @param instanceId
     * @return
     */
    @Update("update sch_instance set pubts = #{currentTime} where instance_id = #{instanceId} and (pubts is  null or pubts ='');")
    int updateNotAssign(@Param("currentTime") long currentTime, @Param("instanceId") String instanceId);

    /**
     *  查找
     * @param instanceId
     * @param currentTime
     * @param idleTime
     * @return
     */
    @Select("select id from sch_instance where instance_id = #{instanceId} and (#{currentTime} - pubts ) < #{idleTime};")
    List<Integer> lookupIdByInstanceId(@Param("instanceId") String instanceId,@Param("currentTime") long currentTime, @Param("idleTime") long idleTime);

    /**
     *  按类型查找
     * @param type
     * @return
     */
    @Select("select id from sch_instance where type = #{type} ;")
    List<Integer> lookupIdByType(@Param("type") Integer type);

    /**
     * 查找存活
     * @param currentTime
     * @param idleTim
     * @return
     */
    @Select("select instance_id from sch_instance where instance_id is not null and instance_id !='' and (#{currentTime} - pubts ) < #{idleTime};")
    List<String> getAllOnlineInstanceId(@Param("currentTime") long currentTime, @Param("idleTime") long idleTim);

}
