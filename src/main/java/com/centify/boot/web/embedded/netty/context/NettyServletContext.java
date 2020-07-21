package com.centify.boot.web.embedded.netty.context;

import com.centify.boot.web.embedded.netty.servlet.NettyFilterChain;
import com.centify.boot.web.embedded.netty.servlet.NettyFilterRegistration;
import com.centify.boot.web.embedded.netty.servlet.NettyRequestDispatcher;
import com.centify.boot.web.embedded.netty.servlet.NettyServletRegistration;
import com.centify.boot.web.embedded.netty.utils.SpringContextUtil;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockSessionCookieConfig;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.util.WebUtils;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.InvalidPathException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * <pre>
 * <b>实现MockServletContext</b>
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

    private static final String TEMP_DIR_SYSTEM_PROPERTY = "java.io.tmpdir";

    private static final Set<SessionTrackingMode> DEFAULT_SESSION_TRACKING_MODES = new LinkedHashSet<>(4);

    private final ResourceLoader resourceLoader;

    private final String resourceBasePath;

    private String contextPath = "";

    private int majorVersion = 3;

    private int minorVersion = 1;

    private int effectiveMajorVersion = 3;

    private int effectiveMinorVersion = 1;

    private final SessionCookieConfig sessionCookieConfig = new MockSessionCookieConfig();

    private String defaultServletName = COMMON_DEFAULT_SERVLET_NAME;

    private final Map<String, NettyServletRegistration> servlets = new HashMap<>(2);

    private final Map<String, String> servletMappings = new HashMap<>(2);

    private final Map<String, NettyFilterRegistration> filters = new HashMap<>(8);

    private final Map<String, ServletContext> contexts = new HashMap<>(2);

    private final Map<String, MediaType> mimeTypes = new LinkedHashMap<>(2);

    private final Set<String> declaredRoles = new LinkedHashSet<>(2);

    private final Map<String, String> initParameters = new LinkedHashMap<>(2);

    private final Map<String, Object> attributes = new LinkedHashMap<>(8);

    @Nullable
    private Set<SessionTrackingMode> sessionTrackingModes;

    static {
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.COOKIE);
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.URL);
        DEFAULT_SESSION_TRACKING_MODES.add(SessionTrackingMode.SSL);
    }

    /**
     * Create a new {@code MockServletContext}, using no base path and a
     * {@link DefaultResourceLoader} (i.e. the classpath root as WAR root).
     * @see org.springframework.core.io.DefaultResourceLoader
     */
    public NettyServletContext() {
        this("", null);
    }

    /**
     * Create a new {@code MockServletContext}, using a {@link DefaultResourceLoader}.
     * @param resourceBasePath the root directory of the WAR (should not end with a slash)
     * @see org.springframework.core.io.DefaultResourceLoader
     */
    public NettyServletContext(String resourceBasePath) {
        this(resourceBasePath, null);
    }

    /**
     * Create a new {@code MockServletContext}, using the specified {@link ResourceLoader}
     * and no base path.
     * @param resourceLoader the ResourceLoader to use (or null for the default)
     */
    public NettyServletContext(@Nullable ResourceLoader resourceLoader) {
        this("", resourceLoader);
    }

    /**
     * Create a new {@code MockServletContext} using the supplied resource base
     * path and resource loader.
     * <p>Registers a for the Servlet named
     * {@literal 'default'}.
     * @param resourceBasePath the root directory of the WAR (should not end with a slash)
     * @param resourceLoader the ResourceLoader to use (or null for the default)
     * @see #registerNamedDispatcher
     */
    public NettyServletContext(String resourceBasePath, @Nullable ResourceLoader resourceLoader) {
        this.resourceLoader = (resourceLoader != null ? resourceLoader : new DefaultResourceLoader());
        this.resourceBasePath = resourceBasePath;

        // Use JVM temp dir as ServletContext temp dir.
        String tempDir = System.getProperty(TEMP_DIR_SYSTEM_PROPERTY);
        if (tempDir != null) {
            this.attributes.put(WebUtils.TEMP_DIR_CONTEXT_ATTRIBUTE, new File(tempDir));
        }
        FilterChain filterChain = NettyFilterChain.getInstance(null, this.filters.entrySet().stream()
                .map(entry->entry.getValue().getFilter()).collect(Collectors.toList()));
        registerNamedDispatcher(this.defaultServletName, new NettyRequestDispatcher(filterChain));
    }
    /**
     * Build a full resource location for the given path, prepending the resource
     * base path of this {@code MockServletContext}.
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
    public ServletContext getContext(String contextPath) {
        if (this.contextPath.equals(contextPath)) {
            return this;
        }
        return this.contexts.get(contextPath);
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
    public String getMimeType(String filePath) {
        String extension = StringUtils.getFilenameExtension(filePath);
        if (this.mimeTypes.containsKey(extension)) {
            return this.mimeTypes.get(extension).toString();
        }
        else {
            return MediaTypeFactory.getMediaType(filePath).
                    map(MimeType::toString)
                    .orElse(null);
        }
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        Set<String> thePaths = new HashSet<>();
        if (!path.endsWith("/")) {
            path += "/";
        }
        String basePath = getRealPath(path);
        if (basePath == null) {
            return thePaths;
        }
        File theBaseDir = new File(basePath);
        if (!theBaseDir.exists() || !theBaseDir.isDirectory()) {
            return thePaths;
        }
        String theFiles[] = theBaseDir.list();
        if (theFiles == null) {
            return thePaths;
        }
        for (String filename : theFiles) {
            File testFile = new File(basePath + File.separator + filename);
            if (testFile.isFile())
                thePaths.add(path + filename);
            else if (testFile.isDirectory())
                thePaths.add(path + filename + "/");
        }
        return thePaths;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        if (!path.startsWith("/"))
            throw new MalformedURLException("Path '" + path + "' does not start with '/'");
        URL url = new URL(getClassLoader().getResource(""), path.substring(1));
        try {
            url.openStream();
        } catch (Throwable t) {
            url = null;
        }
        return url;
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        try {
            return getResource(path).openStream();
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        // FIXME proper path matching
        String servletName = servletMappings.get(path);
        if (servletName == null) {
            servletName = servletMappings.get("/");
        }
        Servlet servlet = null;
        try {
            servlet = null == servletName ? null : servlets.get(servletName).getServlet();
            if (servlet == null) {
                return null;
            }
            // FIXME proper path matching
            FilterChain filterChain = NettyFilterChain.getInstance(servlet, this.filters.entrySet().stream()
                    .map(entry->entry.getValue().getFilter()).collect(Collectors.toList()));
            return new NettyRequestDispatcher(filterChain);
        } catch (ServletException e) {
            // TODO log exception
            return null;
        }
    }

    @SneakyThrows
    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return (RequestDispatcher) (this.servlets.get(name)).getServlet();
    }

    /**
     * Register a {@link RequestDispatcher} (typically a {@link })
     * that acts as a wrapper for the named Servlet.
     * @param name the name of the wrapped Servlet
     * @param requestDispatcher the dispatcher that wraps the named Servlet
     * @see #getNamedDispatcher
     * @see #
     */
    public void registerNamedDispatcher(String name, RequestDispatcher requestDispatcher) {
        Assert.notNull(name, "RequestDispatcher name must not be null");
        Assert.notNull(requestDispatcher, "RequestDispatcher must not be null");
//        this.namedRequestDispatchers.put(name, requestDispatcher);
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
        return Collections.enumeration(Collections.emptySet());
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
    @Nullable
    public String getRealPath(String path) {
        String resourceLocation = getResourceLocation(path);
        Resource resource = null;
        try {
            resource = this.resourceLoader.getResource(resourceLocation);
            return resource.getFile().getAbsolutePath();
        }
        catch (InvalidPathException | IOException ex) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not determine real path of resource " +
                        (resource != null ? resource : resourceLocation), ex);
            }
            return null;
        }
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
    @Nullable
    public ClassLoader getClassLoader() {
        return ClassUtils.getDefaultClassLoader();
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
        return DEFAULT_SESSION_TRACKING_MODES;
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        return (this.sessionTrackingModes != null ?
                Collections.unmodifiableSet(this.sessionTrackingModes) : DEFAULT_SESSION_TRACKING_MODES);
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return this.sessionCookieConfig;
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return addServlet(servletName, className, null);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return addServlet(servletName, servlet.getClass().getName(), servlet);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return addServlet(servletName, servletClass.getName());
    }

    private ServletRegistration.Dynamic addServlet(String servletName, String className, Servlet servlet) {
        NettyServletRegistration servletRegistration = new NettyServletRegistration(this, servletName, className, servlet);
        servlets.put(servletName, servletRegistration);
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

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        return addFilter(filterName, className, null);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return addFilter(filterName, filter.getClass().getName(), filter);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return addFilter(filterName, filterClass.getName());
    }

    private javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className, Filter filter) {
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
        throw new UnsupportedOperationException();
    }

    public void addServletMapping(String urlPattern, String name) {
        servletMappings.put(urlPattern, checkNotNull(name));
    }
    public void addFilterMapping(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String urlPattern) {
        // TODO 实现过滤器 后缀匹配
    }
}