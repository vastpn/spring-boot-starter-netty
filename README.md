### centify-parent  (POM)
    centify-cloud-netty     (Run/Debug -->Before Launch -->Run Maven Goal -->Compile  POM.properties 替换 @@ 标记的属性；自动编译时，需等到Idea编译完成后，再执行Run/Debug )
    
#### V1.0版本
    支持POST、GET、FORM、Filter、Intercept（暂未加入core config中） 性能>=7800笔/S
    
#### 服务端调优1
    -Xms8g -Xmx8g -Xss256k -XX:MetaspaceSize=384m -XX:MaxMetaspaceSize=384m -XX:NewSize=6g -XX:MaxNewSize=6g -XX:SurvivorRatio=18 -XX:MaxDirectMemorySize=2g -XX:+UseParNewGC -XX:ParallelGCThreads=4 -XX:MaxTenuringThreshold=15 -XX:+UseConcMarkSweepGC -XX:+DisableExplicitGC -XX:+UseCMSInitiatingOccupancyOnly -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark -XX:+CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=70 -XX:+CMSClassUnloadingEnabled -XX:SoftRefLRUPolicyMSPerMB=0 -XX:-ReduceInitialCardMarks -XX:+CMSClassUnloadingEnabled -XX:+ExplicitGCInvokesConcurrent -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationConcurrentTime -XX:+PrintHeapAtGC
    
#### 服务端调优2
    -Xmx4096m -Xms4096m -Xmn2048m -Xss256k -XX:SurvivorRatio=6 -XX:ParallelGCThreads=8 -XX:MaxTenuringThreshold=0 -XX:+UseConcMarkSweepGC
    
#### V1.1.0 GA版本
     支持POST、GET、FORM、Filter、Intercept，支持单/多文件上传 性能>=1.38万笔/秒
#### 服务端调优3(V1.1.0 GA)
    -Xmx4096m  -Xms4096m  -Xmn2048m  -Xss256k  -XX:+UseBiasedLocking -XX:SurvivorRatio=8 -XX:ParallelGCThreads=8 -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly  -XX:+UseFastAccessorMethods  -XX:LargePageSizeInBytes=128m  -XX:+CMSParallelRemarkEnabled
