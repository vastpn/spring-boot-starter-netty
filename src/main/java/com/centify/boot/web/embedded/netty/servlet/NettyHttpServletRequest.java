package com.centify.boot.web.embedded.netty.servlet;

import com.centify.boot.web.embedded.netty.context.NettyServletContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.mock.web.DelegatingServletInputStream;
import org.springframework.util.Assert;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/6/16 18:57]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/6/16 18:57        tanlin            new file.
 * <pre>
 */
public class NettyHttpServletRequest implements HttpServletRequest {
    public static final String DISPATCHER_TYPE = NettyRequestDispatcher.class.getName() + ".DISPATCHER_TYPE";
    private static final String CHARSET_PREFIX = "charset=";
    private static final ServletInputStream EMPTY_SERVLET_INPUT_STREAM =
            new DelegatingServletInputStream(StreamUtils.emptyInput());
    private static final BufferedReader EMPTY_BUFFERED_READER =
            new BufferedReader(new StringReader(""));
    private static final String[] DATE_FORMATS = new String[] {
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy",
            "yyyy-MM-dd HH:mm:ss",
    };
    public static final int DEFAULT_SERVER_PORT = 80;
    private int serverPort = DEFAULT_SERVER_PORT;
    public static final String DEFAULT_SERVER_NAME = "localhost";
    private String serverName = DEFAULT_SERVER_NAME;
    private static final String HTTP = "http";
    public static final String DEFAULT_SCHEME = HTTP;
    private String scheme = DEFAULT_SCHEME;
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private final ChannelHandlerContext ctx;
    private final NettyServletContext servletContext;
    private final Map<String, String[]> parameters = new LinkedHashMap<>(16);
    private final LinkedList<Locale> locales = new LinkedList<>();
    private final HttpRequest request;
    private final Map<String, Object> attributes;
    private ServletInputStream inputStream;
    private String pathInfo;
    private String requestURI;
    private String queryString;
    private boolean asyncSupported = true;
    private NettyAsyncContext asyncContext;
    private byte[] content;
    private String characterEncoding;
    private String contentType;
    private final Map<String, NettyHeaderValueHolder> headers = new LinkedCaseInsensitiveMap<>();
    private BufferedReader reader;

    public NettyHttpServletRequest(ChannelHandlerContext ctx, NettyServletContext servletContext, HttpRequest request) {
        this.ctx = ctx;
        this.servletContext = servletContext;
        this.request = request;
        this.attributes = new HashMap<>();
    }

    HttpRequest getNettyRequest() {
        return request;
    }


    public byte[] getContentAsByteArray() {
        return this.content;
    }
    public void setContent(@Nullable byte[] content) {
        this.content = content;
        this.inputStream = null;
        this.reader = null;
    }
    public String getContentAsString() throws IllegalStateException, UnsupportedEncodingException {
        Assert.state(this.characterEncoding != null,
                "Cannot get content as a String for a null character encoding. " +
                        "Consider setting the characterEncoding in the request.");

        if (this.content == null) {
            return null;
        }
        return new String(this.content, this.characterEncoding);
    }
    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String name) {
        NettyHeaderValueHolder header = this.headers.get(name);
        Object value = (header != null ? header.getValue() : null);
        if (value instanceof Date) {
            return ((Date) value).getTime();
        }
        else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        else if (value instanceof String) {
            return parseDateHeader(name, (String) value);
        }
        else if (value != null) {
            throw new IllegalArgumentException(
                    "Value for header '" + name + "' is not a Date, Number, or String: " + value);
        }
        else {
            return -1L;
        }
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
    public void setRequestURI(@Nullable String requestURI) {
        this.requestURI = requestURI;
    }
    @Override
    public String getHeader(String name) {
        NettyHeaderValueHolder header = this.headers.get(name);
        return (header != null ? header.getStringValue() : null);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        NettyHeaderValueHolder header = this.headers.get(name);
        return Collections.enumeration(header != null ? header.getStringValues() : new LinkedList<>());
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.headers.keySet());
    }

    @Override
    public int getIntHeader(String name) {
        NettyHeaderValueHolder header = this.headers.get(name);
        Object value = (header != null ? header.getValue() : null);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        else if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        else if (value != null) {
            throw new NumberFormatException("Value for header '" + name + "' is not a Number: " + value);
        }
        else {
            return -1;
        }
    }
    public void addHeader(String name, Object value) {
        if (HttpHeaders.CONTENT_TYPE.equalsIgnoreCase(name) &&
                !this.headers.containsKey(HttpHeaders.CONTENT_TYPE)) {
            setContentType(value.toString());
        }
        else if (HttpHeaders.ACCEPT_LANGUAGE.equalsIgnoreCase(name) &&
                !this.headers.containsKey(HttpHeaders.ACCEPT_LANGUAGE)) {
            try {
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.ACCEPT_LANGUAGE, value.toString());
                List<Locale> locales = headers.getAcceptLanguageAsLocales();
                this.locales.clear();
                this.locales.addAll(locales);
                if (this.locales.isEmpty()) {
                    this.locales.add(Locale.ENGLISH);
                }
            }
            catch (IllegalArgumentException ex) {
                // Invalid Accept-Language format -> just store plain header
            }
            doAddHeaderValue(name, value, true);
        }
        else {
            doAddHeaderValue(name, value, false);
        }
    }
    public void setPathInfo(@Nullable String pathInfo) {
        this.pathInfo = pathInfo;
    }
    @Override
    public String getMethod() {
        return request.method().name();
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        String requestURI = getRequestURI();
        // FIXME implement properly
        return "/".equals(requestURI) ? "" : requestURI;
    }

    @Override
    public String getQueryString() {
        return this.queryString;
    }
    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return request.uri();
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return getRequestURI();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, IllegalStateException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        synchronized (attributes) {
            return attributes.get(name);
        }
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        synchronized (attributes) {
            return Collections.enumeration(attributes.keySet());
        }
    }

    @Override
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.characterEncoding = characterEncoding;
        updateContentTypeHeader();
    }
    private void updateContentTypeHeader() {
        if (StringUtils.hasLength(this.contentType)) {
            String value = this.contentType;
            if (StringUtils.hasLength(this.characterEncoding) && !this.contentType.toLowerCase().contains(CHARSET_PREFIX)) {
                value += ';' + CHARSET_PREFIX + this.characterEncoding;
            }
            doAddHeaderValue(HttpHeaders.CONTENT_TYPE, value, true);
        }
    }
    private void doAddHeaderValue(String name, @Nullable Object value, boolean replace) {
        NettyHeaderValueHolder header = this.headers.get(name);
        Assert.notNull(value, "Header value must not be null");
        if (header == null || replace) {
            header = new NettyHeaderValueHolder();
            this.headers.put(name, header);
        }
        if (value instanceof Collection) {
            header.addValues((Collection<?>) value);
        }
        else if (value.getClass().isArray()) {
            header.addValueArray(value);
        }
        else {
            header.addValue(value);
        }
    }

    @Override
    public int getContentLength() {
        return (this.content != null ? this.content.length : -1);
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }
    public void setContentType(@Nullable String contentType) {
        this.contentType = contentType;
        if (contentType != null) {
            try {
                MediaType mediaType = MediaType.parseMediaType(contentType);
                if (mediaType.getCharset() != null) {
                    this.characterEncoding = mediaType.getCharset().name();
                }
            }
            catch (IllegalArgumentException ex) {
                // Try to get charset value anyway
                int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
                if (charsetIndex != -1) {
                    this.characterEncoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
                }
            }
            updateContentTypeHeader();
        }
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        if (this.inputStream != null) {
            return this.inputStream;
        }
        else if (this.reader != null) {
            throw new IllegalStateException(
                    "Cannot call getInputStream() after getReader() has already been called for the current request")			;
        }

        this.inputStream = (this.content != null ?
                new DelegatingServletInputStream(new ByteArrayInputStream(this.content)) :
                EMPTY_SERVLET_INPUT_STREAM);
        return this.inputStream;
    }

    @Override
    public String getParameter(String name) {
        String[] arr = this.parameters.get(name);
        return (arr != null && arr.length > 0 ? arr[0] : null);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(this.parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return this.parameters.get(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return Collections.unmodifiableMap(this.parameters);
    }
    public void setParameter(String name, String value) {
        setParameter(name, new String[] {value});
    }
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
    public void addParameter(String name, @Nullable String value) {
        addParameter(name, new String[] {value});
    }
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
    public void removeParameter(String name) {
        Assert.notNull(name, "Parameter name must not be null");
        this.parameters.remove(name);
    }
    public void removeAllParameters() {
        this.parameters.clear();
    }

    public void setParameter(String name, String... values) {
        Assert.notNull(name, "Parameter name must not be null");
        this.parameters.put(name, values);
    }
    @Override
    public String getProtocol() {
        return request.protocolVersion().protocolName();
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
    public BufferedReader getReader() throws IOException {
        if (this.reader != null) {
            return this.reader;
        }
        else if (this.inputStream != null) {
            throw new IllegalStateException(
                    "Cannot call getReader() after getInputStream() has already been called for the current request")			;
        }

        if (this.content != null) {
            InputStream sourceStream = new ByteArrayInputStream(this.content);
            Reader sourceReader = (this.characterEncoding != null) ?
                    new InputStreamReader(sourceStream, this.characterEncoding) :
                    new InputStreamReader(sourceStream);
            this.reader = new BufferedReader(sourceReader);
        }
        else {
            this.reader = EMPTY_BUFFERED_READER;
        }
        return this.reader;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {
        synchronized (attributes) {
            attributes.put(name, o);
        }
    }

    @Override
    public void removeAttribute(String name) {
        synchronized (attributes) {
            attributes.remove(name);
        }
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public AsyncContext startAsync() {
        return startAsync(this, null);
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        return ((NettyAsyncContext) getAsyncContext()).startAsync(servletRequest, servletResponse);
    }

    @Override
    public boolean isAsyncStarted() {
        return null != asyncContext && asyncContext.isAsyncStarted();
    }

    void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    @Override
    public boolean isAsyncSupported() {
        return asyncSupported;
    }

    @Override
    public AsyncContext getAsyncContext() {
        if (null == asyncContext) {
            asyncContext = new NettyAsyncContext(this, ctx);
        }
        return asyncContext;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return attributes.containsKey(DISPATCHER_TYPE) ? (DispatcherType) attributes.get(DISPATCHER_TYPE) : DispatcherType.REQUEST;
    }

}

