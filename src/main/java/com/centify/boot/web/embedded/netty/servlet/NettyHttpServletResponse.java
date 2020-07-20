package com.centify.boot.web.embedded.netty.servlet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.CombinedHttpHeaders;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.lang.Nullable;
import org.springframework.web.util.WebUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/7/14 11:04]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/7/14 11:04        tanlin            new file.
 * <pre>
 */
public class NettyHttpServletResponse implements HttpServletResponse {

    private static final String CHARSET_PREFIX = "charset=";

    @Nullable
    private String characterEncoding = WebUtils.DEFAULT_CHARACTER_ENCODING;

    private boolean charset = false;

    private final NettyServletOutputStream outputStream;

    private final HttpHeaders headers = new CombinedHttpHeaders(false);

    @Nullable
    private String contentType;

    private int bufferSize = 4096;

    private boolean committed;

    private Locale locale = Locale.getDefault();

    private int status = HttpServletResponse.SC_OK;

    public NettyHttpServletResponse(NettyServletOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) {
        this.characterEncoding = characterEncoding;
        this.charset = true;
        updateContentTypeHeader();
    }

    private void updateContentTypeHeader() {
        if (this.contentType != null) {
            StringBuilder sb = new StringBuilder(this.contentType);
            if (!this.contentType.toLowerCase().contains(CHARSET_PREFIX) && this.charset) {
                sb.append(";").append(CHARSET_PREFIX).append(this.characterEncoding);
            }
            doAddHeaderValue(HttpHeaderNames.CONTENT_TYPE.toString(), sb.toString(), true);
        }
    }

    @Override
    @Nullable
    public String getCharacterEncoding() {
        return this.characterEncoding;
    }

    @Override
    public ServletOutputStream getOutputStream() {
        return this.outputStream;
    }

    @Override
    public PrintWriter getWriter() throws UnsupportedEncodingException {
        return null;
    }


    @Override
    public void setContentLength(int contentLength) {
        doAddHeaderValue(HttpHeaderNames.CONTENT_LENGTH.toString(), contentLength, true);
    }
    @Override
    public void setContentLengthLong(long contentLength) {
        doAddHeaderValue(HttpHeaderNames.CONTENT_LENGTH.toString(), contentLength, true);
    }

    @Override
    public void setContentType(@Nullable String contentType) {
        this.contentType = contentType;
        if (contentType != null) {
            try {
                MediaType mediaType = MediaType.parseMediaType(contentType);
                if (mediaType.getCharset() != null) {
                    this.characterEncoding = mediaType.getCharset().name();
                    this.charset = true;
                }
            }
            catch (Exception ex) {
                // Try to get charset value anyway
                int charsetIndex = contentType.toLowerCase().indexOf(CHARSET_PREFIX);
                if (charsetIndex != -1) {
                    this.characterEncoding = contentType.substring(charsetIndex + CHARSET_PREFIX.length());
                    this.charset = true;
                }
            }
            updateContentTypeHeader();
        }
    }

    @Override
    @Nullable
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    @Override
    public int getBufferSize() {
        return this.bufferSize;
    }

    @Override
    public void flushBuffer() {
        setCommitted(true);
    }

    @Override
    public void resetBuffer() {

    }

    public void setCommitted(boolean committed) {
        this.committed = committed;
    }

    @Override
    public boolean isCommitted() {
        return this.committed;
    }

    @Override
    public void reset() {
        resetBuffer();
        this.characterEncoding = null;
        this.contentType = null;
        this.locale = Locale.getDefault();
        this.headers.clear();
        this.status = HttpServletResponse.SC_OK;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
        doAddHeaderValue(HttpHeaderNames.CONTENT_LANGUAGE.toString(), locale.toLanguageTag(), true);
    }

    @Override
    public Locale getLocale() {
        return this.locale;
    }


    //---------------------------------------------------------------------
    // HttpServletResponse interface
    //---------------------------------------------------------------------

    @Override
    public void addCookie(Cookie cookie) {
        this.headers.add(HttpHeaderNames.SET_COOKIE.toString(),cookie.getValue());
    }

    @Override
    public boolean containsHeader(String name) {
        return (this.headers.get(name) != null);
    }

    /**
     * Return the names of all specified headers as a Set of Strings.
     * <p>As of Servlet 3.0, this method is also defined HttpServletResponse.
     * @return the {@code Set} of header name {@code Strings}, or an empty {@code Set} if none
     */
    @Override
    public Collection<String> getHeaderNames() {
        return this.headers.names();
    }

    /**
     * Return the primary value for the given header as a String, if any.
     * Will return the first value in case of multiple values.
     * <p>As of Servlet 3.0, this method is also defined in HttpServletResponse.
     * As of Spring 3.1, it returns a stringified value for Servlet 3.0 compatibility.
     * Consider using  for raw Object access.
     * @param name the name of the header
     * @return the associated header value, or {@code null} if none
     */
    @Override
    @Nullable
    public String getHeader(String name) {
        return this.headers.get(name);
    }

    /**
     * Return all values for the given header as a List of Strings.
     * <p>As of Servlet 3.0, this method is also defined in HttpServletResponse.
     * As of Spring 3.1, it returns a List of stringified values for Servlet 3.0 compatibility.
     * Consider using  for raw Object access.
     * @param name the name of the header
     * @return the associated header values, or an empty List if none
     */
    @Override
    public List<String> getHeaders(String name) {
        return this.headers.getAll(name);
    }


    /**
     * The default implementation returns the given URL String as-is.
     * <p>Can be overridden in subclasses, appending a session id or the like.
     */
    @Override
    public String encodeURL(String url) {
        return url;
    }

    /**
     * The default implementation delegates to {@link #encodeURL},
     * returning the given URL String as-is.
     * <p>Can be overridden in subclasses, appending a session id or the like
     * in a redirect-specific fashion. For general URL encoding rules,
     * override the common {@link #encodeURL} method instead, applying
     * to redirect URLs as well as to general URLs.
     */
    @Override
    public String encodeRedirectURL(String url) {
        return encodeURL(url);
    }

    @Override
    @Deprecated
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override
    @Deprecated
    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    @Override
    public void sendError(int status, String errorMessage) throws IOException {
        this.status = status;
        this.headers.set("errorMessage",errorMessage);
        setCommitted(true);
    }

    @Override
    public void sendError(int status) throws IOException {
        this.status = status;
        setCommitted(true);
    }

    @Override
    public void sendRedirect(String url) throws IOException {
        setHeader(HttpHeaderNames.LOCATION.toString(), url);
        setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        setCommitted(true);
    }
    @Override
    public void setDateHeader(String name, long value) {
        this.headers.set(name,value);
    }

    @Override
    public void addDateHeader(String name, long value) {
        this.headers.add(name,value);
    }

    @Override
    public void setHeader(String name, String value) {
        this.headers.set(name,value);
    }

    @Override
    public void addHeader(String name, String value) {
        this.headers.add(name,value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.headers.setInt(name,value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.headers.addInt(name,value);
    }

    private void doAddHeaderValue(String name, Object value, boolean replace) {
        if (replace) {
            headers.set(name,value);
        }else {
            headers.add(name,value);
        }
    }

    @Override
    public void setStatus(int status) {
        if (!this.isCommitted()) {
            this.status = status;
        }
    }

    @Override
    @Deprecated
    public void setStatus(int status, String errorMessage) {
        if (!this.isCommitted()) {
            this.status = status;
            this.headers.set("errorMessage",errorMessage);
        }
    }

    @Override
    public int getStatus() {
        return this.status;
    }

}