package com.centify.boot.web.embedded.netty.servlet;

import com.centify.boot.web.embedded.netty.factory.NettyServletWebServerFactory;
import io.netty.buffer.ByteBufUtil;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import io.netty.util.CharsetUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.MockAsyncContext;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.Principal;
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

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");


    /**
     * Date formats as specified in the HTTP RFC.
     * @see <a href="https://tools.ietf.org/html/rfc7231#section-7.1.1.1">Section 7.1.1.1 of RFC 7231</a>
     */
    private static final String[] DATE_FORMATS = new String[] {
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy"
    };

    /**
     * The default protocol: 'HTTP/1.1'.
     * @since 4.3.7
     */
    public static final String DEFAULT_PROTOCOL = "HTTP/1.1";

    private UriComponents uriComponents;

    private boolean active = true;

    private final Map<String, Object> attributes = new LinkedHashMap<>(4);

    @Nullable
    private String characterEncoding;

    private FullHttpRequest fullHttpRequest;

    private InetSocketAddress remoteInetSocketAddress;

    @Nullable
    private NettyServletInputStream inputStream = new NettyServletInputStream();

    @Nullable
    private BufferedReader reader;

    private final Map<String, String[]> parameters = new LinkedHashMap<>(16);

    private String protocol = DEFAULT_PROTOCOL;

    /** List of locales in descending order. */
    private final LinkedList<Locale> locales = new LinkedList<>();

    private boolean secure = false;

    private boolean asyncStarted = false;

    private boolean asyncSupported = false;

    @Nullable
    private MockAsyncContext asyncContext;

    private DispatcherType dispatcherType = DispatcherType.REQUEST;

    private String contextPath = "";

    private final Set<String> userRoles = new HashSet<>(4);

    private String servletPath = "";

    private HttpSession session;

    private boolean requestedSessionIdValid = true;

    private boolean requestedSessionIdFromCookie = true;

    private boolean requestedSessionIdFromURL = false;

    private final MultiValueMap<String, Part> parts = new LinkedMultiValueMap<>(4);

    public NettyHttpServletRequest( FullHttpRequest fullHttpRequest, InetSocketAddress remoteInetSocketAddress) {
        this.fullHttpRequest = fullHttpRequest;
        this.remoteInetSocketAddress = remoteInetSocketAddress;
        this.uriComponents = UriComponentsBuilder.fromUriString(fullHttpRequest.uri()).build();
        this.locales.add(Locale.ENGLISH);
        this.inputStream.wrap(fullHttpRequest.content());
        setRequestParams();
    }
    private void setRequestParams() {
        /*URL 转码 */
//        if (uriComponents.getQuery() != null) {
//            this.queryString = UriUtils.decode(uriComponents.getQuery(), CharsetUtil.UTF_8);
//        }
        if (HttpMethod.GET.equals(fullHttpRequest.method())) {
            innerGetParams();
        } else if (HttpMethod.POST.equals(fullHttpRequest.method())) {
            innerPostParams();
        } else if (HttpMethod.DELETE.equals(fullHttpRequest.method())) {
        }
    }

    private void innerPostParams() {
        Optional.ofNullable(fullHttpRequest.headers().get(HttpHeaderNames.CONTENT_TYPE.toString()))
                .ifPresent(item -> {
                    /*JSON、文件无需再次设置，在创建Request时 InputStream 已处理*/
                    /*Form 表单参数设置*/
                    if (item.contains(MediaType.MULTIPART_FORM_DATA_VALUE) || item.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE)) {
                        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
                        this.setParameters(decoder.getBodyHttpDatas().parallelStream()
                                .filter((data) -> data.getHttpDataType().equals(InterfaceHttpData.HttpDataType.Attribute))
                                .map((convData) -> (MemoryAttribute) convData)
                                .collect(Collectors.toMap(
                                        MemoryAttribute::getName,
                                        MemoryAttribute::getValue,
                                        (key1, key2) -> key2)));

                    }
                    /*JSON无需再次获取数据 ，已通过 流获取*/
//                    else if (item.contains(MediaType.APPLICATION_JSON_VALUE)) {
//                    }
                });
    }

    private void innerGetParams() {
        Optional.ofNullable(uriComponents.getQueryParams().entrySet())
                .ifPresent((entrys) -> {
                    entrys.parallelStream().forEach((entry) -> {
                        entry.getValue().parallelStream().forEach((item) -> {
                            this.addParameter(
                                    UriUtils.decode(entry.getKey(), CharsetUtil.UTF_8),
                                    UriUtils.decode(item, CharsetUtil.UTF_8));
                        });
                    });
                });
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
        return NettyServletWebServerFactory.servletContext;
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
        return Collections.enumeration(this.attributes.keySet());
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
        return getHeader(HttpHeaders.CONTENT_TYPE);
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

    @Override
    public String getScheme() {
        return uriComponents.getScheme();
    }


    @Override
    public String getServerName() {
        //TODO 需要验证有服务前缀名称的功能
        return NettyServletWebServerFactory.serverAddress.getHostString();
    }

    @Override
    public int getServerPort() {
        return NettyServletWebServerFactory.serverAddress.getPort();
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

    @Override
    public String getRemoteAddr() {
        return this.remoteInetSocketAddress.getAddress().getHostAddress();
    }

    @Override
    public String getRemoteHost() {

        /**getHostString 不反向查找，能提升部分性能*/
        return this.remoteInetSocketAddress.getHostString();
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
     * Return {@code true} if the  flag has been set
     * to {@code true} or if the {@link #getScheme scheme} is {@code https}.
     * @see javax.servlet.ServletRequest#isSecure()
     */
    @Override
    public boolean isSecure() {
        return (this.secure || HTTPS.equalsIgnoreCase(uriComponents.getScheme()));
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return NettyServletWebServerFactory.servletContext.getRequestDispatcher(path);
    }

    @Override
    @Deprecated
    public String getRealPath(String path) {
        return NettyServletWebServerFactory.servletContext.getRealPath(path);
    }

    @Override
    public int getRemotePort() {
        return this.remoteInetSocketAddress.getPort();
    }


    @Override
    public String getLocalName() {
        return NettyServletWebServerFactory.serverAddress.getHostString();
    }
    @Override
    public String getLocalAddr() {
        return NettyServletWebServerFactory.serverAddress.getAddress().getHostAddress();
    }

    @Override
    public int getLocalPort() {
        return NettyServletWebServerFactory.serverAddress.getPort();
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

    /**
     * 返回用于保护servlet的认证方案的名称。所有Servlet容器都支持基本，表单和客户端证书身份验证，并且可能还支持摘要身份验证。如果servlet未通过身份验证，null则返回。
     * 与CGI变量AUTH_TYPE的值相同。
     *
     * 返回值：
     * 静态成员BASIC_AUTH，FORM_AUTH，CLIENT_CERT_AUTH，DIGEST_AUTH（适用于==比较）之一或表示身份验证方案的特定于容器的字符串，或者 null是否未对请求进行身份验证。
     * */
    @Override
    public String getAuthType() {
        //TODO 获取HTTP身份认证类型
        return null;
    }


    private static String encodeCookies(@NonNull Cookie... cookies) {
        return Arrays.stream(cookies)
                .map(c -> c.getName() + '=' + (c.getValue() == null ? "" : c.getValue()))
                .collect(Collectors.joining("; "));
    }

    @Override
    public Cookie[] getCookies() {

        Set<io.netty.handler.codec.http.cookie.Cookie> cookies;
        String value = fullHttpRequest.headers().get(HttpHeaderNames.COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.STRICT.decode(value);
        }

        if (cookies.isEmpty()) {
            return null;
        }

        return  cookies.toArray(new Cookie[cookies.size()]);
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
        return Collections.enumeration(this.fullHttpRequest.headers().getAll((CharSequence)name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.fullHttpRequest.headers().names());

    }

    @Override
    public int getIntHeader(String name) {
        String headerStringValue = getHeader(name);
        if (headerStringValue == null) {
            return -1;
        }
        return Integer.parseInt(headerStringValue);
    }

    @Override
    @Nullable
    public String getMethod() {
        return this.fullHttpRequest.method().name();
    }

    @Override
    @Nullable
    public String getPathInfo() {
        return uriComponents.getPath();
    }

    @Override
    @Nullable
    public String getPathTranslated() {
        return (uriComponents.getPath() != null ? getRealPath(uriComponents.getPath()) : null);
    }

    @Override
    public String getContextPath() {
        return this.contextPath;
    }

    @Override
    @Nullable
    public String getQueryString() {
        if (StringUtils.isEmpty(uriComponents.getQuery())){
            return null;
        }
        return UriUtils.decode(uriComponents.getQuery(), CharsetUtil.UTF_8);
    }

    @Override
    @Nullable
    public String getRemoteUser() {
        //TODO 暂未实现RemoteUser
        return null;
    }
    @Override
    public boolean isUserInRole(String role) {
        return (this.userRoles.contains(role) || NettyServletWebServerFactory.servletContext.getDeclaredRoles().contains(role));
    }
    @Override
    public Principal getUserPrincipal() {
        //TODO 暂未实现Principal
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        //TODO 暂未实现Session
        return null;
    }

    @Override
    @Nullable
    public String getRequestURI() {
        return uriComponents.getPath();
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

    @Override
    public String getServletPath() {
        return this.servletPath;
    }


    @Override
    public HttpSession getSession(boolean create) {
        checkActive();
        // Reset session if invalidated.
        if (this.session instanceof MockHttpSession && ((MockHttpSession) this.session).isInvalid()) {
            this.session = null;
        }
        // Create new session if necessary.
        if (this.session == null && create) {
            this.session = new MockHttpSession(NettyServletWebServerFactory.servletContext);
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


    @Override
    public boolean isRequestedSessionIdValid() {
        return this.requestedSessionIdValid;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return this.requestedSessionIdFromCookie;
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
