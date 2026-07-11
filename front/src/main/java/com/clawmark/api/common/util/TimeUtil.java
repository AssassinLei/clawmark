package com.clawmark.api.common.util;

import com.clawmark.api.common.constant.BizCode;
import com.clawmark.api.common.exception.BizException;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class TimeUtil {

    private static final DateTimeFormatter ISO_OFFSET = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    private TimeUtil() {
    }

    public static String toIsoUtc(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .format(ISO_OFFSET);
    }

    public static LocalDateTime parseIsoDateTime(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        try {
            OffsetDateTime odt = OffsetDateTime.parse(text.trim());
            return odt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (Exception ex) {
            throw new BizException(BizCode.BAD_REQUEST, fieldName + "格式错误，应为ISO 8601");
        }
    }
}
