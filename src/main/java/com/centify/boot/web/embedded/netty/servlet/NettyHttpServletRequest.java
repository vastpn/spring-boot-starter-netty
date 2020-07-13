package com.centify.boot.web.embedded.netty.servlet;

import com.centify.boot.web.embedded.netty.constant.HttpHeaderConstants;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.*;
import org.springframework.util.*;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.security.Principal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/7/13 14:02]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/13 14:02        tanlin            new file.
 * <pre>
 */
public class NettyHttpServletRequest implements HttpServletRequest {

    private static final String HTTP = "http";

    private static final String HTTPS = "https";

    private static final String CHARSET_PREFIX = "charset=";

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    private static final ServletInputStream EMPTY_SERVLET_INPUT_STREAM =
            new DelegatingServletInputStream(StreamUtils.emptyInput());

    private static final BufferedReader EMPTY_BUFFERED_READER =
            new BufferedReader(new StringReader(""));

    /**
     * Date formats as specified in the HTTP RFC.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.1.1">Section 7.1.1.1 of RFC 7231</a>
     */
    private static final String[] DATE_FORMATS = new String[] {
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy"
    };


    // ---------------------------------------------------------------------
    // Public constants
    // ---------------------------------------------------------------------

    /**
     * The default protocol: 'HTTP/1.1'.
     * @since 4.3.7
     */
    public static final String DEFAULT_PROTOCOL = "HTTP/1.1";

    /**
     * The default scheme: 'http'.
     * @since 4.3.7
     */
    public static final String DEFAULT_SCHEME = HTTP;

    /**
     * The default server address: '127.0.0.1'.
     */
    public static final String DEFAULT_SERVER_ADDR = "127.0.0.1";

    /**
     * The default server name: 'localhost'.
     */
    public static final String DEFAULT_SERVER_NAME = "localhost";

    /**
     * The default server port: '80'.
     */
    public static final int DEFAULT_SERVER_PORT = 80;

    /**
     * The default remote address: '127.0.0.1'.
     */
    public static final String DEFAULT_REMOTE_ADDR = "127.0.0.1";

    /**
     * The default remote host: 'localhost'.
     */
    public static final String DEFAULT_REMOTE_HOST = "localhost";


    // ---------------------------------------------------------------------
    // Lifecycle properties
    // ---------------------------------------------------------------------

    private final ServletContext servletContext;

    private boolean active = true;


    // ---------------------------------------------------------------------
    // ServletRequest properties
    // ---------------------------------------------------------------------

    private final Map<String, Object> attributes = new LinkedHashMap<>();

    @Nullable
    private String characterEncoding;

//    @Nullable
//    private byte[] content;

    private FullHttpRequest fullHttpRequest;

    @Nullable
    private NettyServletInputStream inputStream = new NettyServletInputStream();

    @Nullable
    private BufferedReader reader;

    private final Map<String, String[]> parameters = new LinkedHashMap<>(16);

    private String protocol = DEFAULT_PROTOCOL;

    private String scheme = DEFAULT_SCHEME;

    private String serverName = DEFAULT_SERVER_NAME;

    private int serverPort = DEFAULT_SERVER_PORT;

    private String remoteAddr = DEFAULT_REMOTE_ADDR;

    private String remoteHost = DEFAULT_REMOTE_HOST;

    /** List of locales in descending order. */
    private final LinkedList<Locale> locales = new LinkedList<>();

    private boolean secure = false;

    private int remotePort = DEFAULT_SERVER_PORT;

    private String localName = DEFAULT_SERVER_NAME;

    private String localAddr = DEFAULT_SERVER_ADDR;

    private int localPort = DEFAULT_SERVER_PORT;

    private boolean asyncStarted = false;

    private boolean asyncSupported = false;

    @Nullable
    private MockAsyncContext asyncContext;

    private DispatcherType dispatcherType = DispatcherType.REQUEST;


    // ---------------------------------------------------------------------
    // HttpServletRequest properties
    // ---------------------------------------------------------------------

    @Nullable
    private String authType;

    @Nullable
    private Cookie[] cookies;

    @Nullable
    private String method;

    @Nullable
    private String pathInfo;

    private String contextPath = "";

    @Nullable
    private String queryString;

    @Nullable
    private String remoteUser;

    private final Set<String> userRoles = new HashSet<>();

    @Nullable
    private Principal userPrincipal;

    @Nullable
    private String requestedSessionId;

    @Nullable
    private String requestURI;

    private String servletPath = "";

    @Nullable
    private HttpSession session;

    private boolean requestedSessionIdValid = true;

    private boolean requestedSessionIdFromCookie = true;

    private boolean requestedSessionIdFromURL = false;

    private final MultiValueMap<String, Part> parts = new LinkedMultiValueMap<>();


    // ---------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------

    /**
     * Create a new {@code NettyHttpServletRequest} with a default
     * {@link MockServletContext}.
     * @see #NettyHttpServletRequest(ServletContext, String, String)
     */
    public NettyHttpServletRequest() {
        this(null, "", "");
    }

    /**
     * Create a new {@code NettyHttpServletRequest} with a default
     * {@link MockServletContext}.
     * @param method the request method (may be {@code null})
     * @param requestURI the request URI (may be {@code null})
     * @see #setMethod
     * @see #setRequestURI
     * @see #NettyHttpServletRequest(ServletContext, String, String)
     */
    public NettyHttpServletRequest(@Nullable String method, @Nullable String requestURI) {
        this(null, method, requestURI);
    }

    /**
     * Create a new {@code NettyHttpServletRequest} with the supplied {@link ServletContext}.
     * @param servletContext the ServletContext that the request runs in
     * (may be {@code null} to use a default {@link MockServletContext})
     * @see #NettyHttpServletRequest(ServletContext, String, String)
     */
    public NettyHttpServletRequest(@Nullable ServletContext servletContext) {
        this(servletContext, "", "");
    }

    /**
     * Create a new {@code NettyHttpServletRequest} with the supplied {@link ServletContext},
     * {@code method}, and {@code requestURI}.
     * <p>The preferred locale will be set to {@link Locale#ENGLISH}.
     * @param servletContext the ServletContext that the request runs in (may be
     * {@code null} to use a default {@link MockServletContext})
     * @param method the request method (may be {@code null})
     * @param requestURI the request URI (may be {@code null})
     * @see #setMethod
     * @see #setRequestURI
     * @see MockServletContext
     */
    public NettyHttpServletRequest(@Nullable ServletContext servletContext, @Nullable String method, @Nullable String requestURI) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.method = method;
        this.requestURI = requestURI;
        this.locales.add(Locale.ENGLISH);
    }

    public NettyHttpServletRequest(@Nullable ServletContext servletContext, @Nullable String method, @Nullable String requestURI,FullHttpRequest fullHttpRequest) {
        this.servletContext = (servletContext != null ? servletContext : new MockServletContext());
        this.method = method;
        this.requestURI = requestURI;
        this.locales.add(Locale.ENGLISH);
        this.fullHttpRequest = fullHttpRequest;
        this.inputStream.wrap(fullHttpRequest.content());
    }


    // ---------------------------------------------------------------------
    // Lifecycle methods
    // ---------------------------------------------------------------------

    /**
     * Return the ServletContext that this request is associated with. (Not
     * available in the standard HttpServletRequest interface for some reason.)
     */
    @Override
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    /**
     * Return whether this request is still active (that is, not completed yet).
     */
    public boolean isActive() {
        return this.active;
    }

    /**
     * Mark this request as completed, keeping its state.
     */
    public void close() {
        this.active = false;
    }

    /**
     * Invalidate this request, clearing its state.
     */
    public void invalidate() {
        close();
        clearAttributes();
    }

    /**
     * Check whether this request is still active (that is, not completed yet),
     * throwing an IllegalStateException if not active anymore.
     */
    protected void checkActive() throws IllegalStateException {
        Assert.state(this.active, "Request is not active anymore");
    }


    // ---------------------------------------------------------------------
    // ServletRequest interface
    // ---------------------------------------------------------------------

    @Override
    public Object getAttribute(String name) {
        checkActive();
        return this.attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        checkActive();
        return Collections.enumeration(new LinkedHashSet<>(this.attributes.keySet()));
    }

    @Override
    @Nullable
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public void setCharacterEncoding(@Nullable String characterEncoding) {
        this.characterEncoding = characterEncoding;
    }


    /**
     * Set the content of the request body as a byte array.
     * <p>If the supplied byte array represents text such as XML or JSON, the
     * {@link #setCharacterEncoding character encoding} should typically be
     * set as well.
     * @see #setCharacterEncoding(String)
     * @see #getContentAsByteArray()
     * @see #getContentAsString()
     */
    public void setContent(@Nullable byte[] content) {
        this.inputStream = null;
        this.reader = null;
    }

    /**
     * Get the content of the request body as a byte array.
     * @return the content as a byte array (potentially {@code null})
     * @since 5.0
     * @see #setContent(byte[])
     * @see #getContentAsString()
     */
    @Nullable
    public byte[] getContentAsByteArray() {
        return ByteBufUtil.getBytes(this.fullHttpRequest.content());
    }

    /**
     * Get the content of the request body as a {@code String}, using the configured
     * {@linkplain #getCharacterEncoding character encoding}.
     * @return the content as a {@code String}, potentially {@code null}
     * @throws IllegalStateException if the character encoding has not been set
     * @throws UnsupportedEncodingException if the character encoding is not supported
     * @since 5.0
     * @see #setContent(byte[])
     * @see #setCharacterEncoding(String)
     * @see #getContentAsByteArray()
     */
    @Nullable
    public String getContentAsString() throws IllegalStateException, UnsupportedEncodingException {
        Assert.state(this.characterEncoding != null,
                "Cannot get content as a String for a null character encoding. " +
                        "Consider setting the characterEncoding in the request.");

        if (fullHttpRequest ==null){
            return null;
        }

        if (fullHttpRequest.content().readableBytes()<=0) {
            return null;
        }
        return new String(ByteBufUtil.getBytes(this.fullHttpRequest.content()), this.characterEncoding);
    }

    @Override
    public int getContentLength() {
        return (int) getContentLengthLong();
    }

    @Override
    public long getContentLengthLong() {
        return inputStream.getContentLength();
    }

    @Override
    @Nullable
    public String getContentType() {
        return getHeader(HttpHeaderConstants.CONTENT_TYPE.toString());
    }

    @Override
    public ServletInputStream getInputStream() {
        if(reader != null){
            throw new IllegalStateException("getReader() has already been called for this request");
        }
        return inputStream;
    }

    /**
     * Set a single value for the specified HTTP parameter.
     * <p>If there are already one or more values registered for the given
     * parameter name, they will be replaced.
     */
    public void setParameter(String name, String value) {
        setParameter(name, new String[] {value});
    }

    /**
     * Set an array of values for the specified HTTP parameter.
     * <p>If there are already one or more values registered for the given
     * parameter name, they will be replaced.
     */
    public void setParameter(String name, String... values) {
        Assert.notNull(name, "Parameter name must not be null");
        this.parameters.put(name, values);
    }

    /**
     * Set all provided parameters <strong>replacing</strong> any existing
     * values for the provided parameter names. To add without replacing
     * existing values, use {@link #addParameters(java.util.Map)}.
     */
    public void setParameters(Map<String, ?> params) {
        Assert.notNull(params, "Parameter map must not be null");
        params.forEach((key, value) -> {
            if (value instanceof String) {
                setParameter(key, (String) value);
            }
            else if (value instanceof String[]) {
                setParameter(key, (String[]) value);
            }
            else {
                throw new IllegalArgumentException(
                        "Parameter map value must be single value " + " or array of type [" + String.class.getName() + "]");
            }
        });
    }

    /**
     * Add a single value for the specified HTTP parameter.
     * <p>If there are already one or more values registered for the given
     * parameter name, the given value will be added to the end of the list.
     */
    public void addParameter(String name, @Nullable String value) {
        addParameter(name, new String[] {value});
    }

    /**
     * Add an array of values for the specified HTTP parameter.
     * <p>If there are already one or more values registered for the given
     * parameter name, the given values will be added to the end of the list.
     */
    public void addParameter(String name, String... values) {
        Assert.notNull(name, "Parameter name must not be null");
        String[] oldArr = this.parameters.get(name);
        if (oldArr != null) {
            String[] newArr = new String[oldArr.length + values.length];
            System.arraycopy(oldArr, 0, newArr, 0, oldArr.length);
            System.arraycopy(values, 0, newArr, oldArr.length, values.length);
            this.parameters.put(name, newArr);
        }
        else {
            this.parameters.put(name, values);
        }
    }

    /**
     * Add all provided parameters <strong>without</strong> replacing any
     * existing values. To replace existing values, use
     * {@link #setParameters(java.util.Map)}.
     */
    public void addParameters(Map<String, ?> params) {
        Assert.notNull(params, "Parameter map must not be null");
        params.forEach((key, value) -> {
            if (value instanceof String) {
                addParameter(key, (String) value);
            }
            else if (value instanceof String[]) {
                addParameter(key, (String[]) value);
            }
            else {
                throw new IllegalArgumentException("Parameter map value must be single value " +
                        " or array of type [" + String.class.getName() + "]");
            }
        });
    }

    /**
     * Remove already registered values for the specified HTTP parameter, if any.
     */
    public void removeParameter(String name) {
        Assert.notNull(name, "Parameter name must not be null");
        this.parameters.remove(name);
    }

    /**
     * Remove all existing parameters.
     */
    public void removeAllParameters() {
        this.parameters.clear();
    }

    @Override
    @Nullable
    public String getParameter(String name) {
        Assert.notNull(name, "Parameter name must not be null");
        String[] arr = this.parameters.get(name);
        return (arr != null && arr.length > 0 ? arr[0] : null);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        Assert.notNull(name, "Parameter name must not be null");
        return this.parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(this.parameters);
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String getScheme() {
        return this.scheme;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public String getServerName() {
        String host = getHeader(HttpHeaders.HOST);
        if (host != null) {
            host = host.trim();
            if (host.startsWith("[")) {
                host = host.substring(1, host.indexOf(']'));
            }
            else if (host.contains(":")) {
                host = host.substring(0, host.indexOf(':'));
            }
            return host;
        }

        // else
        return this.serverName;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    @Override
    public int getServerPort() {
        String host = getHeader(HttpHeaders.HOST);
        if (host != null) {
            host = host.trim();
            int idx;
            if (host.startsWith("[")) {
                idx = host.indexOf(':', host.indexOf(']'));
            }
            else {
                idx = host.indexOf(':');
            }
            if (idx != -1) {
                return Integer.parseInt(host.substring(idx + 1));
            }
        }

        // else
        return this.serverPort;
    }

    @Override
    public BufferedReader getReader() throws UnsupportedEncodingException {
        if(reader == null){
            synchronized (this){
                if(reader == null){
                    reader = StringUtils.isEmpty(this.characterEncoding)?
                            new BufferedReader(new InputStreamReader(getInputStream())):
                            new BufferedReader(new InputStreamReader(getInputStream(),this.characterEncoding));
                }
            }
        }
        return reader;
    }

    public void setRemoteAddr(String remoteAddr) {
        this.remoteAddr = remoteAddr;
    }

    @Override
    public String getRemoteAddr() {
        return this.remoteAddr;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    @Override
    public String getRemoteHost() {
        return this.remoteHost;
    }

    @Override
    public void setAttribute(String name, @Nullable Object value) {
        checkActive();
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
        checkActive();
        Assert.notNull(name, "Attribute name must not be null");
        this.attributes.remove(name);
    }

    /**
     * Clear all of this request's attributes.
     */
    public void clearAttributes() {
        this.attributes.clear();
    }


    /**
     * Return the first preferred {@linkplain Locale locale} configured
     * in this mock request.
     * <p>If no locales have been explicitly configured, the default,
     * preferred {@link Locale} for the <em>server</em> mocked by this
     * request is {@link Locale#ENGLISH}.
     * <p>In contrast to the Servlet specification, this mock implementation
     * does <strong>not</strong> take into consideration any locales
     * specified via the {@code Accept-Language} header.
     * @see javax.servlet.ServletRequest#getLocale()
     */
    @Override
    public Locale getLocale() {
        return this.locales.getFirst();
    }

    /**
     * Return an {@linkplain Enumeration enumeration} of the preferred
     * {@linkplain Locale locales} configured in this mock request.
     * <p>If no locales have been explicitly configured, the default,
     * preferred {@link Locale} for the <em>server</em> mocked by this
     * request is {@link Locale#ENGLISH}.
     * <p>In contrast to the Servlet specification, this mock implementation
     * does <strong>not</strong> take into consideration any locales
     * specified via the {@code Accept-Language} header.
     * @see javax.servlet.ServletRequest#getLocales()
     */
    @Override
    public Enumeration<Locale> getLocales() {
        return Collections.enumeration(this.locales);
    }

    /**
     * Set the boolean {@code secure} flag indicating whether the mock request
     * was made using a secure channel, such as HTTPS.
     * @see #isSecure()
     * @see #getScheme()
     * @see #setScheme(String)
     */
    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    /**
     * Return {@code true} if the {@link #setSecure secure} flag has been set
     * to {@code true} or if the {@link #getScheme scheme} is {@code https}.
     * @see javax.servlet.ServletRequest#isSecure()
     */
    @Override
    public boolean isSecure() {
        return (this.secure || HTTPS.equalsIgnoreCase(this.scheme));
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return new MockRequestDispatcher(path);
    }

    @Override
    @Deprecated
    public String getRealPath(String path) {
        return this.servletContext.getRealPath(path);
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    @Override
    public int getRemotePort() {
        return this.remotePort;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    @Override
    public String getLocalName() {
        return this.localName;
    }

    public void setLocalAddr(String localAddr) {
        this.localAddr = localAddr;
    }

    @Override
    public String getLocalAddr() {
        return this.localAddr;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    @Override
    public int getLocalPort() {
        return this.localPort;
    }

    @Override
    public AsyncContext startAsync() {
        return startAsync(this, null);
    }

    @Override
    public AsyncContext startAsync(ServletRequest request, @Nullable ServletResponse response) {
        Assert.state(this.asyncSupported, "Async not supported");
        this.asyncStarted = true;
        this.asyncContext = new MockAsyncContext(request, response);
        return this.asyncContext;
    }

    public void setAsyncStarted(boolean asyncStarted) {
        this.asyncStarted = asyncStarted;
    }

    @Override
    public boolean isAsyncStarted() {
        return this.asyncStarted;
    }

    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    @Override
    public boolean isAsyncSupported() {
        return this.asyncSupported;
    }

    public void setAsyncContext(@Nullable MockAsyncContext asyncContext) {
        this.asyncContext = asyncContext;
    }

    @Override
    @Nullable
    public AsyncContext getAsyncContext() {
        return this.asyncContext;
    }

    public void setDispatcherType(DispatcherType dispatcherType) {
        this.dispatcherType = dispatcherType;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return this.dispatcherType;
    }


    // ---------------------------------------------------------------------
    // HttpServletRequest interface
    // ---------------------------------------------------------------------

    public void setAuthType(@Nullable String authType) {
        this.authType = authType;
    }

    @Override
    @Nullable
    public String getAuthType() {
        return this.authType;
    }


    private static String encodeCookies(@NonNull Cookie... cookies) {
        return Arrays.stream(cookies)
                .map(c -> c.getName() + '=' + (c.getValue() == null ? "" : c.getValue()))
                .collect(Collectors.joining("; "));
    }

    @Override
    @Nullable
    public Cookie[] getCookies() {
        return this.cookies;
    }

    /**
     * Return the long timestamp for the date header with the given {@code name}.
     * <p>If the internal value representation is a String, this method will try
     * to parse it as a date using the supported date formats:
     * <ul>
     * <li>"EEE, dd MMM yyyy HH:mm:ss zzz"</li>
     * <li>"EEE, dd-MMM-yy HH:mm:ss zzz"</li>
     * <li>"EEE MMM dd HH:mm:ss yyyy"</li>
     * </ul>
     * @param name the header name
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.1.1">Section 7.1.1.1 of RFC 7231</a>
     */
    @Override
    public long getDateHeader(String name) {
        String value = getHeader(name);
        if(StringUtils.isEmpty(value)){
            return -1;
        }
        return parseDateHeader(name, (String) value);
    }

    private long parseDateHeader(String name, String value) {
        for (String dateFormat : DATE_FORMATS) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
            simpleDateFormat.setTimeZone(GMT);
            try {
                return simpleDateFormat.parse(value).getTime();
            }
            catch (ParseException ex) {
                // ignore
            }
        }
        throw new IllegalArgumentException("Cannot parse date value '" + value + "' for '" + name + "' header");
    }

    @Override
    @Nullable
    public String getHeader(String name) {
        Object value = this.fullHttpRequest.headers().get((CharSequence) name);
        return value == null? null :String.valueOf(value);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        Collection collection = this.fullHttpRequest.headers().getAll((CharSequence)name);
        return new Enumeration<String>() {
            private Iterator iterator = collection.iterator();
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }
            @Override
            public String nextElement() {
                return iterator.next().toString();
            }
        };
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set nameSet = this.fullHttpRequest.headers().names();
        return new Enumeration<String>() {
            private Iterator iterator = nameSet.iterator();
            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }
            @Override
            public String nextElement() {
                return iterator.next().toString();
            }
        };
    }

    @Override
    public int getIntHeader(String name) {
        String headerStringValue = getHeader(name);
        if (headerStringValue == null) {
            return -1;
        }
        return Integer.parseInt(headerStringValue);
    }

    public void setMethod(@Nullable String method) {
        this.method = method;
    }

    @Override
    @Nullable
    public String getMethod() {
        return this.method;
    }

    public void setPathInfo(@Nullable String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    @Nullable
    public String getPathInfo() {
        return this.pathInfo;
    }

    @Override
    @Nullable
    public String getPathTranslated() {
        return (this.pathInfo != null ? getRealPath(this.pathInfo) : null);
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    public void setQueryString(@Nullable String queryString) {
        this.queryString = queryString;
    }

    @Override
    @Nullable
    public String getQueryString() {
        return this.queryString;
    }

    public void setRemoteUser(@Nullable String remoteUser) {
        this.remoteUser = remoteUser;
    }

    @Override
    @Nullable
    public String getRemoteUser() {
        return this.remoteUser;
    }

    public void addUserRole(String role) {
        this.userRoles.add(role);
    }

    @Override
    public boolean isUserInRole(String role) {
        return (this.userRoles.contains(role) || (this.servletContext instanceof MockServletContext &&
                ((MockServletContext) this.servletContext).getDeclaredRoles().contains(role)));
    }

    public void setUserPrincipal(@Nullable Principal userPrincipal) {
        this.userPrincipal = userPrincipal;
    }

    @Override
    @Nullable
    public Principal getUserPrincipal() {
        return this.userPrincipal;
    }

    public void setRequestedSessionId(@Nullable String requestedSessionId) {
        this.requestedSessionId = requestedSessionId;
    }

    @Override
    @Nullable
    public String getRequestedSessionId() {
        return this.requestedSessionId;
    }

    public void setRequestURI(@Nullable String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    @Nullable
    public String getRequestURI() {
        return this.requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        String scheme = getScheme();
        String server = getServerName();
        int port = getServerPort();
        String uri = getRequestURI();

        StringBuffer url = new StringBuffer(scheme).append("://").append(server);
        if (port > 0 && ((HTTP.equalsIgnoreCase(scheme) && port != 80) ||
                (HTTPS.equalsIgnoreCase(scheme) && port != 443))) {
            url.append(':').append(port);
        }
        if (StringUtils.hasText(uri)) {
            url.append(uri);
        }
        return url;
    }

    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public String getServletPath() {
        return this.servletPath;
    }

    public void setSession(HttpSession session) {
        this.session = session;
        if (session instanceof MockHttpSession) {
            MockHttpSession mockSession = ((MockHttpSession) session);
            mockSession.access();
        }
    }

    @Override
    @Nullable
    public HttpSession getSession(boolean create) {
        checkActive();
        // Reset session if invalidated.
        if (this.session instanceof MockHttpSession && ((MockHttpSession) this.session).isInvalid()) {
            this.session = null;
        }
        // Create new session if necessary.
        if (this.session == null && create) {
            this.session = new MockHttpSession(this.servletContext);
        }
        return this.session;
    }

    @Override
    @Nullable
    public HttpSession getSession() {
        return getSession(true);
    }

    /**
     * The implementation of this (Servlet 3.1+) method calls
     * {@link MockHttpSession#changeSessionId()} if the session is a mock session.
     * Otherwise it simply returns the current session id.
     * @since 4.0.3
     */
    public String changeSessionId() {
        Assert.isTrue(this.session != null, "The request does not have a session");
        if (this.session instanceof MockHttpSession) {
            return ((MockHttpSession) this.session).changeSessionId();
        }
        return this.session.getId();
    }

    public void setRequestedSessionIdValid(boolean requestedSessionIdValid) {
        this.requestedSessionIdValid = requestedSessionIdValid;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return this.requestedSessionIdValid;
    }

    public void setRequestedSessionIdFromCookie(boolean requestedSessionIdFromCookie) {
        this.requestedSessionIdFromCookie = requestedSessionIdFromCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return this.requestedSessionIdFromCookie;
    }

    public void setRequestedSessionIdFromURL(boolean requestedSessionIdFromURL) {
        this.requestedSessionIdFromURL = requestedSessionIdFromURL;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return this.requestedSessionIdFromURL;
    }

    @Override
    @Deprecated
    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() throws ServletException {
        this.userPrincipal = null;
        this.remoteUser = null;
        this.authType = null;
    }

    public void addPart(Part part) {
        this.parts.add(part.getName(), part);
    }

    @Override
    @Nullable
    public Part getPart(String name) throws IOException, ServletException {
        return this.parts.getFirst(name);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        List<Part> result = new LinkedList<>();
        for (List<Part> list : this.parts.values()) {
            result.addAll(list);
        }
        return result;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

}
