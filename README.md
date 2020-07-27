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
    
    