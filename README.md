### centify-parent  (POM)
    centify-cloud-netty     (Run/Debug -->Before Launch -->Run Maven Goal -->Compile  POM.properties 替换 @@ 标记的属性；自动编译时，需等到Idea编译完成后，再执行Run/Debug )
    
#### V1.0版本
    支持POST、GET、FORM、Filter、Intercept（暂未加入core config中） 性能>=7800笔/S
    
#### 服务端调优1
    -Xms8g -Xmx8g -Xss256k -XX:MetaspaceSize=384m -XX:MaxMetaspaceSize=384m -XX:NewSize=6g -XX:MaxNewSize=6g -XX:SurvivorRatio=18 -XX:MaxDirectMemorySize=2g -XX:+UseParNewGC -XX:ParallelGCThreads=4 -XX:MaxTenuringThreshold=15 -XX:+UseConcMarkSweepGC -XX:+DisableExplicitGC -XX:+UseCMSInitiatingOccupancyOnly -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark -XX:+CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=70 -XX:+CMSClassUnloadingEnabled -XX:SoftRefLRUPolicyMSPerMB=0 -XX:-ReduceInitialCardMarks -XX:+CMSClassUnloadingEnabled -XX:+ExplicitGCInvokesConcurrent -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationConcurrentTime -XX:+PrintHeapAtGC