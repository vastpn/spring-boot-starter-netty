package com.centify.boot.web.embedded.netty.config;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * <pre>
 * <b>TODO</b>
 * <b>Describe:TODO</b>
 *
 * <b>Author: tanlin [2020/8/31 19:39]</b>
 * <b>Copyright:</b> Copyright 2008-2026 http://www.jinvovo.com Technology Co., Ltd. All rights reserved.
 * <b>Changelog:</b>
 *   Ver   Date                  Author           Detail
 *   ----------------------------------------------------------------------------
 *   1.0   2020/8/31 19:39        tanlin            new file.
 * <pre>
 */
public class DateSerializer implements ObjectSerializer {
    public static final DateSerializer instance = new DateSerializer();
    private static final String defaultPattern = "yyyy-MM-dd HH:mm:ss";

    public DateSerializer() {
    }

    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
        } else {
            Date result = (Date) object;
            out.writeString((new SimpleDateFormat(defaultPattern).format(result)));
        }
    }

}


