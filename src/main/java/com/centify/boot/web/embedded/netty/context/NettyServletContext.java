package com.centify.boot.web.embedded.netty.context;

import com.centify.boot.web.embedded.netty.constant.NettyConstant;
import com.centify.boot.web.embedded.netty.servlet.*;
import io.netty.handler.codec.http.multipart.DiskAttribute;
import io.netty.handler.codec.http.multipart.DiskFileUpload;
import lombok.SneakyThrows;
import org.hibernate.validator.internal.util.ConcurrentReferenceHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.server.MimeMappings;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <pre>
 * <b>实现ServletContext</b>
 * <b>Describe:
 * 1、只需要有ServletContext空对象，否则Springboot启动时抛NPE
 * 2、SpringBoot-> Netty-->重新赋值addServlet、addFilter</b>
 *
 * <b>Author: tanlin [2020/7/6 11:03]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/6 11:03        tanlin            new file.
 * <pre>
 */
public class NettyServletContext implements ServletContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(NettyServletContext.class);

    /** Default Servlet name used by Tomcat, Jetty, JBoss, and GlassFish: {@value}. */
    private static final String COMMON_DEFAULT_SERVLET_NAME = "default";

    private final ResourceLoader resourceLoader;

    private ClassLoader classLoader;

    private final String resourceBasePath;

    private String contextPath = "";

    private String serverHeader;

    private String servletContextName;

    private final ServerProperties serverProperties;

    private int majorVersion = 3;

    private int minorVersion = 1;

    private int effectiveMajorVersion = 3;

    private int effectiveMinorVersion = 1;

    private String defaultServletName = COMMON_DEFAULT_SERVLET_NAME;

    private final Map<String, NettyServletRegistration> servlets = new HashMap<>(2);

    private final Map<String, String> servletMappings = new HashMap<>(2);

    /**按照Servlet的AddFilter插入顺序，Filter集合必须是按照插入的顺序保存，才能实现Filter有序执行，顾采用LinkedHashMap*/
    private final Map<String, NettyFilterRegistration> filters = new LinkedHashMap<>(8);

    private final Map<String, ServletContext> contexts = new HashMap<>(2);

    private final Map<String, MimeMappings.Mapping> mimeTypes = new LinkedHashMap<>(2);

    private final Set<String> declaredRoles = new LinkedHashSet<>(2);

    private final Map<String, String> initParameters = new LinkedHashMap<>(2);

    private final Map<String, Object> attributes = new LinkedHashMap<>(8);

    @Nullable
    private Set<SessionTrackingMode> sessionTrackingModes;
    private InetSocketAddress serverAddress;
    private ResourceManager resourceManager;
    private Map<String,RequestDispatcher> namedRequestDispatchers = new ConcurrentReferenceHashMap();

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * Create a new {@code NettyServletContext} using the supplied resource base
     * path and resource loader.
     * <p>Registers a for the Servlet named
     * {@literal 'default'}.
     * @param resourceBasePath the root directory of the WAR (should not end with a slash)
     * @param resourceLoader the ResourceLoader to use (or null for the default)
     * @param serverAddress
     */
    public NettyServletContext(String resourceBasePath,String baseDir, ResourceLoader resourceLoader, ServerProperties serverProperties, InetSocketAddress serverAddress) {
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
        this.classLoader = (this.resourceLoader.getClassLoader() == null?ClassUtils.getDefaultClassLoader():this.resourceLoader.getClassLoader());
        this.resourceBasePath = resourceBasePath;
        this.contextPath = resourceBasePath;
        this.serverProperties = serverProperties;
        this.serverAddress = serverAddress;
        setDocBase(baseDir);

    }

    public void setDocBase(String docBase){
        String workspace = '/' + (serverAddress == null || isLocalhost(serverAddress.getHostName())? "localhost": serverAddress.getHostName());
        setDocBase(docBase,workspace);
    }

    public void setDocBase(String docBase,String workspace){
        this.resourceManager = new ResourceManager(docBase,workspace,classLoader);
        this.resourceManager.mkdirs("/");

        /**process Netty TempFile And Attribute Info*/
        DiskFileUpload.deleteOnExitTemporaryFile = true;
        DiskAttribute.deleteOnExitTemporaryFile = true;
        DiskFileUpload.baseDirectory = resourceManager.getRealPath("/");
        DiskAttribute.baseDirectory = resourceManager.getRealPath("/");
    }

    public String getServerHeader() {
        return serverHeader;
    }

    public void setServerHeader(String serverHeader) {
        this.serverHeader = serverHeader;
    }

    public void setServletContextName(String servletContextName) {
        this.servletContextName = servletContextName;
    }


    public static boolean isLocalhost(String host){
        return "localhost".equalsIgnoreCase(host) || host.contains("0.0.0.0") ||  host.contains("127.0.0.1");
    }
    protected static File createTempDir(String prefix) {
        try {
            File tempDir = File.createTempFile(prefix + ".", "");
            tempDir.delete();
            tempDir.mkdir();
            tempDir.deleteOnExit();
            return tempDir;
        }catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to create tempDir. java.io.tmpdir is set to "
                            + System.getProperty("java.io.tmpdir"),
                    ex);
        }
    }
    /**
     * Build a full resource location for the given path, prepending the resource
     * base path of this {@code ServletContext}.
     * @param path the path as specified
     * @return the full resource path
     */
    protected String getResourceLocation(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return this.resourceBasePath + path;
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    @Override
    public ServletContext getContext(String uripath) {
        if (this.serverProperties.getServlet().getContextPath().equals(uripath)) {
            return this;
        }
        return this.contexts.get(uripath);
    }

    @Override
    public int getMajorVersion() {
        return this.majorVersion;
    }

    @Override
    public int getMinorVersion() {
        return this.minorVersion;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return this.effectiveMajorVersion;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return this.effectiveMinorVersion;
    }

    @Override
    public String getMimeType(String file) {
        if (file == null) {
            return null;
        }
        int period = file.lastIndexOf('.');
        if (period < 0) {
            return null;
        }
        String extension = file.substring(period + 1);
        if (extension.length() < 1) {
            return null;
        }
        return mimeTypes.get(extension).getMimeType();
    }

    public void setMimeTypesElement(String extension,MimeMappings.Mapping mimeType){
        this.mimeTypes.put(extension,mimeType);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        return resourceManager.getResourcePaths(path);
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        return resourceManager.getResource(path);
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        return resourceManager.getResourceAsStream(path);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return this.namedRequestDispatchers.get(this.defaultServletName);
    }

    @SneakyThrows
    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return this.namedRequestDispatchers.get(name);
    }

    @Deprecated
    @Override
    @Nullable
    public Servlet getServlet(String name) throws ServletException {
        return servlets.get(name).getServlet();
    }

    @Override
    @Deprecated
    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration(servlets.values().stream().map((item)->
            item.getServlet()).collect(Collectors.toList()));
    }

    @Override
    @Deprecated
    public Enumeration<String> getServletNames() {
        return Collections.enumeration(Collections.emptySet());
    }

    @Override
    public void log(String message) {
        LOGGER.info(message);
    }

    @Override
    @Deprecated
    public void log(Exception ex, String message) {
        LOGGER.info(message, ex);
    }

    @Override
    public void log(String message, Throwable ex) {
        LOGGER.info(message, ex);
    }

    @Override
    public String getRealPath(String path) {
        return resourceManager.getRealPath(path);
    }

    @Override
    public String getServerInfo() {
        return COMMON_DEFAULT_SERVLET_NAME;
    }

    @Override
    public String getInitParameter(String name) {
        Assert.notNull(name, "Parameter name must not be null");
        return this.initParameters.get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(this.initParameters.keySet());
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        Assert.notNull(name, "Parameter name must not be null");
        if (this.initParameters.containsKey(name)) {
            return false;
        }
        this.initParameters.put(name, value);
        return true;
    }

    @Override
    @Nullable
    public Object getAttribute(String name) {
        Assert.notNull(name, "Attribute name must not be null");
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(this.attributes.keySet());
    }

    @Override
    public void setAttribute(String name, @Nullable Object value) {
        Assert.notNull(name, "Attribute name must not be null");
        if (value != null) {
            this.attributes.put(name, value);
        }
        else {
            this.attributes.remove(name);
        }
    }

    @Override
    public void removeAttribute(String name) {
        Assert.notNull(name, "Attribute name must not be null");
        this.attributes.remove(name);
    }
    @Override
    public String getServletContextName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public ClassLoader getClassLoader() {

        return resourceManager.getClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {
        Assert.notNull(roleNames, "Role names array must not be null");
        for (String roleName : roleNames) {
            Assert.hasLength(roleName, "Role name must not be empty");
            this.declaredRoles.add(roleName);
        }
    }

    public Set<String> getDeclaredRoles() {
        return Collections.unmodifiableSet(this.declaredRoles);
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
            throws IllegalStateException, IllegalArgumentException {
        this.sessionTrackingModes = sessionTrackingModes;
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        //TODO 未实现Session
        return null;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        //TODO 未实现Session
        return null;
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        //TODO 未实现Session
        return null;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException();
    }

    @SneakyThrows
    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return addServlet(servletName, className, null);
    }

    @SneakyThrows
    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return addServlet(servletName, servlet.getClass().getName(), servlet);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return addServlet(servletName, servletClass.getName());
    }

    private ServletRegistration.Dynamic addServlet(String servletName, String className, Servlet servlet) throws ClassNotFoundException, InstantiationException, ServletException, IllegalAccessException {
        this.defaultServletName = servletName;
        NettyServletRegistration servletRegistration = new NettyServletRegistration(this, servletName, className, servlet);
        servlets.put(servletName, servletRegistration);

        NettyFilterChain filterChain = NettyFilterChain.getInstance(servletRegistration, this.filters.entrySet().stream()
                .map(entry->entry.getValue().getFilter()).collect(Collectors.toList()));
        filterChain.setServletContext(this);
        this.namedRequestDispatchers.put(servletName, new NettyRequestDispatcher(filterChain));

        return servletRegistration;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }

    /**
     * This method always returns {@code null}.
     * @see javax.servlet.ServletContext#getServletRegistration(java.lang.String)
     */
    @Override
    @Nullable
    public ServletRegistration getServletRegistration(String servletName) {
        return null;
    }

    /**
     * This method always returns an {@linkplain Collections#emptyMap empty map}.
     * @see javax.servlet.ServletContext#getServletRegistrations()
     */
    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return Collections.emptyMap();
    }

    @SneakyThrows
    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return addFilter(filterName, className, null);
    }

    @SneakyThrows
    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return addFilter(filterName, filter.getClass().getName(), filter);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return addFilter(filterName, filterClass.getName());
    }

    private javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className, Filter filter) throws ServletException {
        NettyFilterRegistration filterRegistration = new NettyFilterRegistration(this, filterName, className, filter);
        filters.put(filterName, filterRegistration);
        return filterRegistration;
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
        try {
            return c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * This method always returns {@code null}.
     * @see javax.servlet.ServletContext#getFilterRegistration(java.lang.String)
     */
    @Override
    @Nullable
    public FilterRegistration getFilterRegistration(String filterName) {
        return filters.get(filterName);
    }

    /**
     * This method always returns an {@linkplain Collections#emptyMap empty map}.
     * @see javax.servlet.ServletContext#getFilterRegistrations()
     */
    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return filters;
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVirtualServerName() {
        return NettyConstant.OS_SYSTEM_INFO;
    }

    public void addServletMapping(String urlPattern, String name) {
        servletMappings.put(urlPattern, checkNotNull(name));
    }
    public void addFilterMapping(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String urlPattern) {
        // TODO 实现过滤器 后缀匹配
    }

    public Map<String, String> getServletMappings() {
        return servletMappings;
    }
}