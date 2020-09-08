package com.centify.boot.web.embedded.netty.constant;

/**
 * <pre>
 * <b>Netty静态类</b>
 * <b>Describe:</b>
 *
 * <b>Author: tanlin [2020/6/9 17:04]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/6/9 17:04        tanlin            new file.
 * <pre>
 */
public final class NettyConstant {
    /**Http favicon.ico 地址*/
    public static final String HTTP_REQUEST_FAVICON="/favicon.ico";

    public static final String OS_SYSTEM_INFO;

    public static final String JVM_INFO;
    static {
        StringBuilder infos = new StringBuilder();
        infos.append("[OS-System] Name:").append(System.getProperty("os.name")).append(",")
                .append("Version=").append(System.getProperty("os.version")).append(",")
                .append("ARCH=").append(System.getProperty("os.arch")).append(" ");

        OS_SYSTEM_INFO = infos.toString();

        infos = new StringBuilder();

        infos.append("[JVM-System] Version=").append(System.getProperty("java.runtime.version")).append(",")
                .append("Vendor=").append(System.getProperty("java.vm.vendor")).append(" ");
        JVM_INFO = infos.toString();
    }
}
