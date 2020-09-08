package com.centify.boot.web.embedded.netty;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * <pre>
 * <b>Junit + contiperf</b>
 * <b>Describe:
 *
 *1）PerfTest参数
 * @PerfTest(invocations = 300)：执行300次，和线程数量无关，默认值为1，表示执行1次；
 * @PerfTest(threads=30)：并发执行30个线程，默认值为1个线程；
 * @PerfTest(duration = 20000)：重复地执行测试至少执行20s。
 *
 * 三个属性可以组合使用，其中Threads必须和其他两个属性组合才能生效。当Invocations和Duration都有指定时，
 * 以执行次数多的为准。例，@PerfTest(invocations = 300, threads = 2, duration = 100)，如果执行
 * 方法300次的时候执行时间还没到100ms，则继续执行到满足执行时间等于100ms，如果执行到50次的时候已经100ms了，
 * 则会继续执行之100次。
 * 如果你不想让测试连续不间断的跑完，可以通过注释设置等待时间，例，@PerfTest(invocations = 1000,
 * threads = 10, timer = RandomTimer.class, timerParams = { 30, 80 }) ，每执行完一次会
 * 等待30~80ms然后才会执行下一次调用。在开多线程进行并发压测的时候，如果一下子达到最大进程数有些系统
 * 可能会受不了，ContiPerf还提供了“预热”功能，例，@PerfTest(threads = 10, duration = 60000,
 * rampUp = 1000) ，启动时会先起一个线程，然后每个1000ms起一线程，到9000ms时10个线程同时执行，
 * 那么这个测试实际执行了69s，如果只想衡量全力压测的结果，那么可以在注释中加入warmUp，
 * 即@PerfTest(threads = 10, duration = 60000, rampUp = 1000, warmUp = 9000) ，那么统计
 * 结果的时候会去掉预热的9s。
 *
 * 2）Required参数
 * @Required(throughput = 20)：要求每秒至少执行20个测试；
 * @Required(average = 50)：要求平均执行时间不超过50ms；
 * @Required(median = 45)：要求所有执行的50%不超过45ms；
 * @Required(max = 2000)：要求没有测试超过2s；
 * @Required(totalTime = 5000)：要求总的执行时间不超过5s；
 * @Required(percentile90 = 3000)：要求90%的测试不超过3s；
 * @Required(percentile95 = 5000)：要求95%的测试不超过5s；
 * @Required(percentile99 = 10000)：要求99%的测试不超过10s;
 * @Required(percentiles = "66:200,96:500")：要求66%的测试不超过200ms，96%的测试不超过500ms。
 *
 * ================结果指标================
 * 结果视图文件：target/contiperf-report/index.html
 *
 * Execution time 执行总时间
 * Total invocations 总请求数
 * Throughput 每秒效率 TPS
 * Min. latency 最短响应时间
 * Average latency 平均响应时间
 * Median TP50响应时间
 * 90% TP90响应时间
 * Max latency 最长响应时间
 * </b>
 *
 * <b>Author: tanlin [2020/9/8 16:39]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/9/8 16:39        tanlin            new file.
 * <pre>
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {NettyServletWebApplication_Back.class})
public class SpringBootJunitContiperfTest_Back {

    /**
     * 引入 ContiPerf 进行性能测试
     * 激活性能测试，否则@PerfTest 无法生效
     */
    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();

    @Before
    public void testBefore(){
        System.out.println("测试前");
    }

    @After
    public void testAfter(){
        System.out.println("测试后");
    }
    @Test
    public void contextLoads() {
        //TODO JUNIT测试
    }

    /**
     * 10个线程 执行100次
     * invocations:调用次数，执行次数与线程无关
     * threads:线程
     */
    @Test
    @PerfTest(invocations = 1,threads = 1)
    public void test() {
        //TODO 性能测试
    }
}
