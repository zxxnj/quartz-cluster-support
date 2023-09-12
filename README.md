# quartz-cluster-support

#### 介绍
quartz高性能，高可用、无限水平扩容增强支持<br>
本工程提供对quartz无锁化调度，执行效率、高性能、高可用、容灾、无限扩容等功能的增强支持
#### 软件架构
Quartz集群采用了以数据库作为协调中心的方式，需要依赖数据库在集群间同步调度状态，基于数据库分布式锁实现一致性调度。
所以quartz分布式多实例部署锁竞争比较剧烈，所以可以采用分片分表的优化思路，动态添加多组Quartz主处理程序，以达到
高性能，提高同一时间可以扫描执行的任务数量，加快执行效率。但分表之后，多实例部署情况下仍然存在锁竞争，单纯的分表
增加quartz调度处理程序音数据库分布式锁存在，每个实例会产生大量空闲线程，大量空闲的线程会占用许多内存，给垃圾回收
器带来压力，而且大量的线程在竞争CPU资源时还将产生其他性能的开销。因此将分表后的多组Quartz处理程序动态分配到各个
实例


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
