package com.centify.boot.web.embedded.netty.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * <pre>
 * <b>Netty Web 容器属性</b>
 * <b>Describe:
 * 1、提供入站、出站功能、性能配置</b>
 *
 * <b>Author: tanlin [2020/7/30 14:42]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/30 14:42        tanlin            new file.
 * <pre>
 */
@ConfigurationProperties(
        prefix = "spring.netty.web.custom",
        ignoreUnknownFields = true
)
public class NettyEmbeddedProperties {

    /**
     * 接收线程数量，默认=1
     */
    private Integer acceptGroupThread = 1;

    /**
     * 接收线程池前缀名称，默认=acceptGroup
     */
    private String acceptGroupPoolName = "acceptGroup";

    /**
     * 处理线程池数量，默认=CPU*2
     */
    private Integer workerGroupThread = Runtime.getRuntime().availableProcessors() * 2;

    /**
     * 处理线程池前缀名称，默认=workerGroup
     */
    private String workerGroupPoolName = "workerGroup";

    /**
     * 是否允许服务端端口占用，默认=true
     */
    private Boolean optionSoReuseaddr = Boolean.TRUE;

    /**
     * 服务端可处理队列数量，默认=1024
     */
    private Integer optionSoBacklog = 1024;

    /**
     * 接收缓冲区初始大小，默认=4KB
     */
    private Integer optionSoRcvbuf = 4 * 1024;

    /**
     * 低水位线，默认=128KB
     */
    private Integer childOptionWriteBufferLowWaterMark = 128 * 1024;

    /**
     * 高水位线，默认=256KB
     */
    private Integer childOptionWriteBufferHighWaterMark = 256 * 1024;

    /**
     * 响应时间有高要求的场景，默认=true 禁用nagle算法
     */
    private Boolean childOptiontcpNodelay = Boolean.TRUE;

    /**
     * 是否允许端口占用，默认=true
     */
    private Boolean childOptionsoReuseaddr = Boolean.TRUE;

    /**
     * 是否设置TCP/IP长连接，默认=true
     */
    private Boolean childOptionsoKeepalive = Boolean.TRUE;

    /**
     * 接收数据大小，默认=4KB
     */
    private Integer childOptionsoRcvbuf = 4 * 1024;

    /**
     * 发送数据大小，默认=16KB
     */
    private Integer childOptionsoSndbuf = 16 * 1024;

    /**
     * HttpObjectAggregator请求数据粘包最大字节，默认=256KB；(存在文件上传时，需提高最大粘包大小)
     */
    private Integer requestDataMaxcontentlength = 256 * 1024;

    /**
     * channel读取超时，默认=3秒
     */
    private Integer readTimeoutSeconds = 3;

    /**
     * channel写超时，默认=3秒
     */
    private Integer writeTimeoutSeconds = 3;

    public Integer getAcceptGroupThread() {
        return acceptGroupThread;
    }

    public void setAcceptGroupThread(Integer acceptGroupThread) {
        this.acceptGroupThread = acceptGroupThread;
    }

    public String getAcceptGroupPoolName() {
        return acceptGroupPoolName;
    }

    public void setAcceptGroupPoolName(String acceptGroupPoolName) {
        this.acceptGroupPoolName = acceptGroupPoolName;
    }

    public Integer getWorkerGroupThread() {
        return workerGroupThread;
    }

    public void setWorkerGroupThread(Integer workerGroupThread) {
        this.workerGroupThread = workerGroupThread;
    }

    public String getWorkerGroupPoolName() {
        return workerGroupPoolName;
    }

    public void setWorkerGroupPoolName(String workerGroupPoolName) {
        this.workerGroupPoolName = workerGroupPoolName;
    }

    public Boolean getOptionSoReuseaddr() {
        return optionSoReuseaddr;
    }

    public void setOptionSoReuseaddr(Boolean optionSoReuseaddr) {
        this.optionSoReuseaddr = optionSoReuseaddr;
    }

    public Integer getOptionSoBacklog() {
        return optionSoBacklog;
    }

    public void setOptionSoBacklog(Integer optionSoBacklog) {
        this.optionSoBacklog = optionSoBacklog;
    }

    public Integer getOptionSoRcvbuf() {
        return optionSoRcvbuf;
    }

    public void setOptionSoRcvbuf(Integer optionSoRcvbuf) {
        this.optionSoRcvbuf = optionSoRcvbuf;
    }

    public Integer getChildOptionWriteBufferLowWaterMark() {
        return childOptionWriteBufferLowWaterMark;
    }

    public void setChildOptionWriteBufferLowWaterMark(Integer childOptionWriteBufferLowWaterMark) {
        this.childOptionWriteBufferLowWaterMark = childOptionWriteBufferLowWaterMark;
    }

    public Integer getChildOptionWriteBufferHighWaterMark() {
        return childOptionWriteBufferHighWaterMark;
    }

    public void setChildOptionWriteBufferHighWaterMark(Integer childOptionWriteBufferHighWaterMark) {
        this.childOptionWriteBufferHighWaterMark = childOptionWriteBufferHighWaterMark;
    }

    public Boolean getChildOptiontcpNodelay() {
        return childOptiontcpNodelay;
    }

    public void setChildOptiontcpNodelay(Boolean childOptiontcpNodelay) {
        this.childOptiontcpNodelay = childOptiontcpNodelay;
    }

    public Boolean getChildOptionsoReuseaddr() {
        return childOptionsoReuseaddr;
    }

    public void setChildOptionsoReuseaddr(Boolean childOptionsoReuseaddr) {
        this.childOptionsoReuseaddr = childOptionsoReuseaddr;
    }

    public Boolean getChildOptionsoKeepalive() {
        return childOptionsoKeepalive;
    }

    public void setChildOptionsoKeepalive(Boolean childOptionsoKeepalive) {
        this.childOptionsoKeepalive = childOptionsoKeepalive;
    }

    public Integer getChildOptionsoRcvbuf() {
        return childOptionsoRcvbuf;
    }

    public void setChildOptionsoRcvbuf(Integer childOptionsoRcvbuf) {
        this.childOptionsoRcvbuf = childOptionsoRcvbuf;
    }

    public Integer getChildOptionsoSndbuf() {
        return childOptionsoSndbuf;
    }

    public void setChildOptionsoSndbuf(Integer childOptionsoSndbuf) {
        this.childOptionsoSndbuf = childOptionsoSndbuf;
    }

    public Integer getRequestDataMaxcontentlength() {
        return requestDataMaxcontentlength;
    }

    public void setRequestDataMaxcontentlength(Integer requestDataMaxcontentlength) {
        this.requestDataMaxcontentlength = requestDataMaxcontentlength;
    }

    public Integer getReadTimeoutSeconds() {
        return readTimeoutSeconds;
    }

    public void setReadTimeoutSeconds(Integer readTimeoutSeconds) {
        this.readTimeoutSeconds = readTimeoutSeconds;
    }

    public Integer getWriteTimeoutSeconds() {
        return writeTimeoutSeconds;
    }

    public void setWriteTimeoutSeconds(Integer writeTimeoutSeconds) {
        this.writeTimeoutSeconds = writeTimeoutSeconds;
    }

    @Override
    public String toString() {
        return "\r\nNettyEmbeddedProperties{" +
                "  \r\n     acceptGroupThread=" + acceptGroupThread +
                ", \r\n     acceptGroupPoolName='" + acceptGroupPoolName + '\'' +
                ", \r\n     workerGroupThread=" + workerGroupThread +
                ", \r\n     workerGroupPoolName='" + workerGroupPoolName + '\'' +
                ", \r\n     optionSoReuseaddr=" + optionSoReuseaddr +
                ", \r\n     optionSoBacklog=" + optionSoBacklog +
                ", \r\n     optionSoRcvbuf=" + optionSoRcvbuf +
                ", \r\n     childOptionWriteBufferLowWaterMark=" + childOptionWriteBufferLowWaterMark +
                ", \r\n     childOptionWriteBufferHighWaterMark=" + childOptionWriteBufferHighWaterMark +
                ", \r\n     childOptiontcpNodelay=" + childOptiontcpNodelay +
                ", \r\n     childOptionsoReuseaddr=" + childOptionsoReuseaddr +
                ", \r\n     childOptionsoKeepalive=" + childOptionsoKeepalive +
                ", \r\n     childOptionsoRcvbuf=" + childOptionsoRcvbuf +
                ", \r\n     childOptionsoSndbuf=" + childOptionsoSndbuf +
                ", \r\n     requestDataMaxcontentlength=" + requestDataMaxcontentlength +
                ", \r\n     readTimeoutSeconds=" + readTimeoutSeconds +
                ", \r\n     writeTimeoutSeconds=" + writeTimeoutSeconds +
                '}';
    }
}
