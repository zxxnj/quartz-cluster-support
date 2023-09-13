# quartz-cluster-support

#### 介绍
quartz分布式场景下高性能，高可用、无限水平扩容增强支持<br>
本工程提供对quartz分布式场景下无锁化调度，执行效率、高性能、高可用、容灾、无限水平扩容等功能的增强支持
#### 软件架构
1.整体原理
Quartz集群采用了以数据库作为协调中心的方式，需要依赖数据库在集群间同步调度状态，基于抢占式数据库分布式锁实现一致性调度。
所以quartz分布式多实例部署锁竞争比较剧烈，可以采用分片分表的优化思路，动态添加多组Quartz主处理程序，以达到高性能，
提高同一时间可以扫描执行的任务数量，加快执行效率。但分表之后，多实例部署情况下仍然存在锁竞争，单纯的分表增加quartz调度
处理程序，因数据库分布式锁存在，每个实例会产生大量空闲线程，大量空闲的线程会占用许多内存，给垃圾回收器带来压力，而且大量
的线程在竞争CPU资源时还将产生其他性能的开销。因此将分表后的多组Quartz处理程序动态分配到各个实例，避免多个实例分配相同
的调度程序产生锁竞争，以达到无锁化调度。此时采用分表+动态分配Quartz处理程序的策略即可达到无限水平扩容的增强支持应对未来
业务激增。


![quartz分表](images/quartz%E5%88%86%E5%B8%83%E5%BC%8F%E8%B0%83%E5%BA%A6.png)

![quartz集群动态分配](images/quartz%E9%9B%86%E7%BE%A4%E5%8A%A8%E6%80%81%E5%88%86%E9%85%8D.png)

2.动态分配策略  
（1）定义quartz处理程序Scheduler总数scheduler.size 和 quartz总实例数 instanceCount。  
（2）根据scheduler.size和instanceCount分配多个Scheduler到不同的实例中。  
（3）针对每一个实例：  
      n = Scheduler总数/调度中心总实例数  
      m = Scheduler总数%调度中心总实例数  
    第一次分配时m>0 则分配n+1个Scheduler，后面的 依次取剩余实例数和剩余Scheduler个数取商取余进行再次分配。  
（4）定时上报心跳，按检查间隔（默认10s）定时上报更新pubts。根据心跳清除消亡或异常的实例  

3.协调者程序高可用保障以及容灾流程  
（1）定时检测SCH_INSTANCE表中的pubts与当前时间差值大于3倍的检查间隔，判断当前实例是否异常或者死亡，清除超期数据对应该条记录的 instance_id、pubts。  
（2）定时检测到SCH_INSTANCE表中有未分配的id均分给其它存活实例，或者进行重新分配。  
![输入图片说明](images/%E9%AB%98%E5%8F%AF%E7%94%A8%E5%AE%B9%E7%81%BE%E4%BF%9D%E9%9A%9C.png)

4.粘性分配策略  
 采用粘性分配策略，在有异常实例是针对存储实例与Scheduler分配表清除数据时记修改SCH_INSTANCE中type=1，在异常实例恢复时根据SCH_INSTANCE分配的存活实例和
 quartz总实例数 instanceCount判断采用粘性分配策略分配type=1的数据给新启动的实例

#### 安装教程

1.  xxxx
2.  xxxx
3.  xxxx

#### 使用说明

1.  xxxx
2.  xxxx
3.  xxxx

#### 参与贡献

1.  Fork 本仓库
2.  新建 Feat_xxx 分支
3.  提交代码
4.  新建 Pull Request


#### 特技

1.  使用 Readme\_XXX.md 来支持不同的语言，例如 Readme\_en.md, Readme\_zh.md
2.  Gitee 官方博客 [blog.gitee.com](https://blog.gitee.com)
3.  你可以 [https://gitee.com/explore](https://gitee.com/explore) 这个地址来了解 Gitee 上的优秀开源项目
4.  [GVP](https://gitee.com/gvp) 全称是 Gitee 最有价值开源项目，是综合评定出的优秀开源项目
5.  Gitee 官方提供的使用手册 [https://gitee.com/help](https://gitee.com/help)
6.  Gitee 封面人物是一档用来展示 Gitee 会员风采的栏目 [https://gitee.com/gitee-stars/](https://gitee.com/gitee-stars/)
