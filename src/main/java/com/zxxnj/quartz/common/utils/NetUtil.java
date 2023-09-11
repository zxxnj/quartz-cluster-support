package com.zxxnj.quartz.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

/**
 * 获取实例唯一编码
 * @author zxxnj
 */
@Slf4j
public class NetUtil {

    private NetUtil() {
    }

    public static String generateInstance() {
        return InstanceHolder.INSTANCE.getInstance();
    }

    /**
     * 枚举类获取
     */
    private enum InstanceHolder {
        /**
         * 枚举
         */
        INSTANCE;
        /**
         * 实例信息
         */
        private String instance;

        InstanceHolder() {
            //此处参考quartz生成实例id的计算方法，copy from org.quartz.simpl.SimpleInstanceIdGenerator
            try {
                instance = InetAddress.getLocalHost().getHostName() + System.currentTimeMillis();
            } catch (Exception e) {
                log.error("Couldn't get host name!", e);
            }
        }

        /**
         * 获取实例信息
         *
         * @return 实例信息
         */
        public String getInstance() {
            return instance;
        }
    }
}
