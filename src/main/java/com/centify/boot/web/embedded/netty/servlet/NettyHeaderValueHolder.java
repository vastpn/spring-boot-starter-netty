package com.centify.boot.web.embedded.netty.servlet;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/6/16 21:16]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/6/16 21:16        tanlin            new file.
 * <pre>
 */
public class NettyHeaderValueHolder {
    private final List<Object> values = new LinkedList<>();

    public void setValue(@Nullable Object value) {
        this.values.clear();
        if (value != null) {
            this.values.add(value);
        }
    }

    public void addValue(Object value) {
        this.values.add(value);
    }

    public void addValues(Collection<?> values) {
        this.values.addAll(values);
    }

    public void addValueArray(Object values) {
        CollectionUtils.mergeArrayIntoCollection(values, this.values);
    }

    public List<Object> getValues() {
        return Collections.unmodifiableList(this.values);
    }

    public List<String> getStringValues() {
        List<String> stringList = new ArrayList<>(this.values.size());
        for (Object value : this.values) {
            stringList.add(value.toString());
        }
        return Collections.unmodifiableList(stringList);
    }

    @Nullable
    public Object getValue() {
        return (!this.values.isEmpty() ? this.values.get(0) : null);
    }

    @Nullable
    public String getStringValue() {
        return (!this.values.isEmpty() ? String.valueOf(this.values.get(0)) : null);
    }

    @Override
    public String toString() {
        return this.values.toString();
    }


    /**
     * Find a HeaderValueHolder by name, ignoring casing.
     * @param headers the Map of header names to HeaderValueHolders
     * @param name the name of the desired header
     * @return the corresponding HeaderValueHolder, or {@code null} if none found
     * @deprecated as of 5.1.10 in favor of using
     * {@link org.springframework.util.LinkedCaseInsensitiveMap}.
     */
    @Nullable
    @Deprecated
    public static NettyHeaderValueHolder getByName(Map<String, NettyHeaderValueHolder> headers, String name) {
        Assert.notNull(name, "Header name must not be null");
        for (String headerName : headers.keySet()) {
            if (headerName.equalsIgnoreCase(name)) {
                return headers.get(headerName);
            }
        }
        return null;
    }

}
