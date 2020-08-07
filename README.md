### centify-parent  (POM)
    centify-cloud-netty     (Run/Debug -->Before Launch -->Run Maven Goal -->Compile  POM.properties 替换 @@ 标记的属性；自动编译时，需等到Idea编译完成后，再执行Run/Debug )
    
#### V1.0版本
    支持POST、GET、FORM、Filter、Intercept（暂未加入core config中） 性能>=7800笔/S
    
#### 服务端调优1
    -d64 -server -XX:+AggressiveOpts -XX:+UseLargePages -Xms8g -Xmx8g -Xss256k -XX:MetaspaceSize=384m -XX:MaxMetaspaceSize=384m -XX:NewSize=6g -XX:MaxNewSize=6g -XX:SurvivorRatio=18 -XX:MaxDirectMemorySize=2g -XX:+UseParNewGC -XX:ParallelGCThreads=4 -XX:MaxTenuringThreshold=15 -XX:+UseConcMarkSweepGC -XX:+DisableExplicitGC -XX:+UseCMSInitiatingOccupancyOnly -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark -XX:+CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=70 -XX:+CMSClassUnloadingEnabled -XX:SoftRefLRUPolicyMSPerMB=0 -XX:-ReduceInitialCardMarks -XX:+CMSClassUnloadingEnabled -XX:+ExplicitGCInvokesConcurrent -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationConcurrentTime -XX:+PrintHeapAtGC
    
#### 服务端调优2
    -d64 -server -XX:+AggressiveOpts -XX:+UseLargePages -Xmx4096m -Xms4096m -Xmn2048m -Xss256k -XX:SurvivorRatio=6 -XX:ParallelGCThreads=8 -XX:MaxTenuringThreshold=0 -XX:+UseConcMarkSweepGC
    
#### V1.1.0 GA版本
     支持POST、GET、FORM、Filter、Intercept，支持单/多文件上传 性能>=1.38万笔/秒
#### 服务端调优3(V1.1.0 GA)
    -d64 -server -XX:+AggressiveOpts -XX:+UseLargePages -Xmx4096m  -Xms4096m  -Xmn2048m  -Xss256k  -XX:+UseBiasedLocking -XX:SurvivorRatio=8 -XX:ParallelGCThreads=8 -XX:+UseConcMarkSweepGC -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly  -XX:+UseFastAccessorMethods  -XX:LargePageSizeInBytes=128m  -XX:+CMSParallelRemarkEnabled
#### 服务端调优 paraNew+CMS
    ==============================》》》》》建议JVM参数 paraNew+CMS
    JVM	调优A-4：	性能可以，内存均衡，YGC 0.8%，FGC几乎没有，蜗牛增长；延长反射使用的软引用GC时间为23秒，老年代年龄转化为默认15，使用SURVIVOR，使用率很低，还可以优化
    -d64 -server -XX:+AggressiveOpts -XX:+UseLargePages -Xmx4096m -Xms4096m -Xmn1536m -Xss256k -XX:SurvivorRatio=8 -XX:ParallelGCThreads=8 -XX:MaxTenuringThreshold=15 -XX:+UseConcMarkSweepGC -XX:+UseFastAccessorMethods -XX:+CMSParallelRemarkEnabled -XX:SoftRefLRUPolicyMSPerMB=23000 -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -verbose:gc 
    ==============================》》》》》建议JVM参数 paraNew+CMS

#### 服务端调优 G1
    ==============================》》》》》建议JVM参数 G1
    JVM	调优A-5：	性能可以(比A-4吞吐量低点)，比（A-4好点，主要表现在YGC，约为0.2%）
    -d64 -server -XX:+AggressiveOpts -XX:+UseLargePages -Xmx4096m -Xms4096m -Xmn1536m -Xss256k -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m -XX:SurvivorRatio=12 -XX:ParallelGCThreads=8 -XX:MaxTenuringThreshold=15 -XX:+UseFastAccessorMethods -XX:+ParallelRefProcEnabled -XX:SoftRefLRUPolicyMSPerMB=10000 -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
    ==============================》》》》》建议JVM参数 G1
#### 服务端调优 G1

    ==============================》》》》》建议JVM参数 G1  12小时内无FGC YGC=0.6%  性能持续讲 <=12900
    -d64 -server -XX:+AggressiveOpts -XX:+UseLargePages -Xms4096m -Xmx4096m -Xmn3072m -Xss256k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m  -XX:SurvivorRatio=12 -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+ParallelRefProcEnabled -XX:+AggressiveOpts -XX:SoftRefLRUPolicyMSPerMB=6000 -XX:+UseG1GC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps
    ==============================》》》》》建议JVM参数 G1
#### 服务端调优 paraNew +CMS
    ==============================》paraNew +CMS 性能持续>=13334 CPU35% YGC=0.8 无FGC
    -d64 -server -XX:+AggressiveOpts -XX:+UseLargePages -Xms4096m -Xmx4096m -Xmn2048m -Xss256k -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256m -XX:SurvivorRatio=12 -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+ParallelRefProcEnabled -XX:+AggressiveOpts -XX:SoftRefLRUPolicyMSPerMB=6000 -XX:+UseParNewGC -XX:ParallelGCThreads=8 -XX:MaxTenuringThreshold=15 -XX:+UseConcMarkSweepGC -XX:+DisableExplicitGC -XX:+ScavengeBeforeFullGC -XX:+CMSScavengeBeforeRemark -XX:+CMSParallelRemarkEnabled -XX:+CMSClassUnloadingEnabled -XX:-ReduceInitialCardMarks -XX:+ExplicitGCInvokesConcurrent -XX:+PrintGCDetails -XX:+PrintGCDateStamps 
    ==============================》paraNew +CMS
#### 服务端调优 Parallel + Parallel Old
    ==============================》Parallel GC +Parallel old GC CPU35% YGC=0.3	无FGC
    -d64 -server -XX:+AggressiveOpts -XX:+UseLargePages -Xms4g -Xmx4g -Xmn3g -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -XX:SurvivorRatio=38 -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+ParallelRefProcEnabled -XX:SoftRefLRUPolicyMSPerMB=10000 -XX:TargetSurvivorRatio=80 -XX:MaxTenuringThreshold=15 -XX:-UseAdaptiveSizePolicy -XX:+UseParallelGC -XX:ParallelGCThreads=8 -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps
    -d64 -server -XX:+AggressiveOpts -XX:+UseLargePages -Xms4g -Xmx4g -Xmn2g -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -XX:SurvivorRatio=38 -XX:MaxDirectMemorySize=1g -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+ParallelRefProcEnabled -XX:SoftRefLRUPolicyMSPerMB=10000 -XX:TargetSurvivorRatio=80 -XX:MaxTenuringThreshold=15 -XX:-UseAdaptiveSizePolicy -XX:+UseParallelGC -XX:ParallelGCThreads=8 -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps
    -d64 -server -XX:+AggressiveOpts -XX:+UseLargePages -Xms4g -Xmx4g -Xmn2g -Xss512k -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m -XX:SurvivorRatio=38 -XX:MaxDirectMemorySize=1g -XX:+UseBiasedLocking -XX:+UseFastAccessorMethods -XX:+ParallelRefProcEnabled -XX:SoftRefLRUPolicyMSPerMB=10000 -XX:TargetSurvivorRatio=80 -XX:MaxTenuringThreshold=15 -XX:-UseAdaptiveSizePolicy -XX:+UseParallelGC -XX:ParallelGCThreads=8 -XX:+UseParallelOldGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps
    ==============================》Parallel GC +Parallel old GC

##### Netty Servlet SpringBoot SpringMVC 集成方式
    1、pom.xml 修改
    
    <!--继承父工程POM-->
        <parent>
            <artifactId>centify-cloud-netty</artifactId>
            <groupId>com.centify.boot</groupId>
            <version>1.0.1-SNAPSHOT</version>
            <relativePath/>
        </parent>
        
    <!--引入依赖 spring-boot-starter-netty 容器组件-->
    <dependency>
        <groupId>com.centify.boot</groupId>
        <artifactId>spring-boot-starter-netty</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
    
    <!--移除原有SpringBoot 自带的 tomcat/jetty/undertown 容器-->
    
    <!--部分项目使用到特有的功能，需自行引入、移除、删除相关jar；I生活框架使用了validate，增加此依赖-->
        <!--加入，支持Validate-->
        <dependency>
            <groupId>org.apache.tomcat.embed</groupId>
            <artifactId>tomcat-embed-el</artifactId>
            <version>9.0.16</version>
        </dependency>
        
        <!--移除io.lettuce  netty starter 主键暂时不支持，下一版本修复兼容-->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
            <exclusions>
                <exclusion>
                    <groupId>io.lettuce</groupId>
                    <artifactId>lettuce-core</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
    2、SpringBoot启动类增加注解：
        @EnableNettyEmbedded
    
    